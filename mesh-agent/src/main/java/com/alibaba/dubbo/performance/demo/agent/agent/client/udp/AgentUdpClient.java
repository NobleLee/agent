package com.alibaba.dubbo.performance.demo.agent.agent.client.udp;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

/**
 * 描述: UDP客户端
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-26 下午9:50
 */
public class AgentUdpClient {

    private static Logger logger = LoggerFactory.getLogger(AgentUdpClient.class);
    Random r = new Random(1);

    private Channel channel;

    private static AgentUdpClient instance;

    private static List<HashMap<Long, Channel>> requestList = new ArrayList<>(COMMON.HTTPSERVER_WORK_THREAD);

    static {
        for (int i = 0; i < COMMON.HTTPSERVER_WORK_THREAD; i++) {
            requestList.add(new HashMap<>());
        }
    }

    public static AgentUdpClient getInstance() {
        if (instance == null) {
            synchronized (AgentUdpClient.class) {
                if (instance == null)
                    instance = new AgentUdpClient();
            }
        }
        return instance;
    }

    private AgentUdpClient() {
        bind();
    }


    private void bind() {
        NioEventLoopGroup workers = new NioEventLoopGroup(COMMON.AgentClient_THREAD);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workers)
                .option(ChannelOption.SO_BROADCAST, false)
                .channel(NioDatagramChannel.class)
                .handler(new SimpleChannelInboundHandler<DatagramPacket>() {

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
                        response(msg);
                    }
                });
        try {
            channel = bootstrap.bind(0).sync().channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 给指定的server发送消息
     *
     * @param endpoint
     * @param buf
     */
    public void send(Endpoint endpoint, ByteBuf buf) {
        if (buf.readableBytes() < 136) {
            logger.error("message length less 136 ");
            return;
        }
        byte index = (byte) (Thread.currentThread().getId() % COMMON.HTTPSERVER_WORK_THREAD);
        long id = System.currentTimeMillis() << 35 | ((long) r.nextInt(Integer.MAX_VALUE)) << 3 | index;
        buf.skipBytes(128);
        buf.setLong(buf.readerIndex(), id);
        buf.writeShort(COMMON.MAGIC);
        // 因为是请求是HTTP连接，因此需要存储id的连接通道
        // TODO 更改成数组 hashmap 避免锁竞争
        // requestHolderMap.put(Long.valueOf(id), channel);
        requestList.get(index).put(Long.valueOf(id), channel);
        // 根据负载均衡算法，选择一个节点发送数据
        buf.retain();
        channel.writeAndFlush(new DatagramPacket(buf, new InetSocketAddress(endpoint.getHost(), endpoint.getPort())));
    }

    /**
     * 将接收到的agent response返回
     *
     * @param msg
     */
    public void response(DatagramPacket msg) {
        ByteBuf content = msg.content();
        // 获取请求id
        long requestId = content.readLong();
        // 封装返回response
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        response.headers().set(CONTENT_LENGTH, content.readableBytes());
        response.content().writeBytes(content);
        int index = (int) (requestId & 0x7);
        Channel remove = requestList.get(index).remove(requestId);
        if (remove != null && remove.isActive()) {
            remove.writeAndFlush(response);
        }
    }


}
