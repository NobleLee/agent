package com.alibaba.dubbo.performance.demo.agent.agent.client.udp;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.agent.ConnecManager;
import com.alibaba.dubbo.performance.demo.agent.agent.client.AgentClientConnectPool;
import com.alibaba.dubbo.performance.demo.agent.agent.client.AgentClientInitializer;
import com.alibaba.dubbo.performance.demo.agent.agent.httpserver.HttpServerHandler;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EndpointHelper;
import com.alibaba.dubbo.performance.demo.agent.tools.ByteBufUtils;
import com.alibaba.dubbo.performance.demo.agent.tools.LOCK;
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

    private Channel channel;

    private static AgentUdpClient instance;

    private static List<Endpoint> endpointList = new ArrayList<>();

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
                        ByteBufUtils.printStringln(msg.content(),8,"client udp response:  ");
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
     * @param buf
     */
    public void send(ByteBuf buf, long id) {
        if (buf.readableBytes() < 136) {
            logger.error("message length less 136 ");
            return;
        }
        buf.skipBytes(128);
        buf.setLong(buf.readerIndex(), id);
        // 因为是请求是HTTP连接，因此需要存储id的连接通道
        // TODO 更改成数组 hashmap 避免锁竞争
        Endpoint endpoint = EndpointHelper.getBalancePoint(endpointList);
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

        Channel rchannel = HttpServerHandler.channelList.get((int) requestId);
        if (rchannel != null && rchannel.isActive()) {
            rchannel.writeAndFlush(response);
        }
    }


    /**
     * 对每一个endpoint节点都创建一个通道
     *
     * @param endpoints
     * @return
     * @throws Exception
     */
    public static boolean putServers(List<Endpoint> endpoints) {
        // TODO  应该加锁
        LOCK.AgentChannelLock = true;
        for (Endpoint endpoint : endpoints) {
            if (!endpointList.contains(endpoint)) {
                endpointList.add(endpoint);
            }
            logger.info("udp get endpoint: " + endpoint.toString());
        }
        LOCK.AgentChannelLock = false;
        return true;
    }

    /**
     * 对每一个endpoint节点删除通道
     *
     * @param endpoints
     * @return
     * @throws Exception
     */
    public static boolean deleteServers(List<Endpoint> endpoints) {
        // TODO 应该加锁
        LOCK.AgentChannelLock = true;
        for (Endpoint endpoint : endpoints) {
            if (endpointList.contains(endpoint)) {
                endpointList.remove(endpoint);
                logger.info("close channel; endpoint: " + endpoint.toString());
            }
        }
        LOCK.AgentChannelLock = false;
        return true;
    }


}
