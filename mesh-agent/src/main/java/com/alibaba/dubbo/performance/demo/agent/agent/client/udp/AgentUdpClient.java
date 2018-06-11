package com.alibaba.dubbo.performance.demo.agent.agent.client.udp;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.agent.httpserver.HTTPServer;
import com.alibaba.dubbo.performance.demo.agent.agent.httpserver.HttpServerHandler;
import com.alibaba.dubbo.performance.demo.agent.agent.httpserver.HttpSimpleHandler;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EndpointHelper;
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
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

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

    private static List<InetSocketAddress> interList = new ArrayList<>();

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
                    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
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
        // 根据负载均衡算法，选择一个节点发送数据
        int index = EndpointHelper.getBalancePoint(endpointList);
//        DatagramPacket datagramPacket = HttpServerHandler.udpReqList.get(index).get((int) id);
//        datagramPacket.retain();
//        datagramPacket.content().resetWriterIndex();
//        datagramPacket.content().resetReaderIndex();
//        datagramPacket.content().writeBytes(buf);
        channel.writeAndFlush(new DatagramPacket(buf, interList.get(index)));
    }

    /**
     * 将接收到的agent response返回
     *
     * @param msg
     */
    public void response(DatagramPacket msg) {
        ByteBuf content = msg.content();
        // 获取请求id
        int requestId = (int) content.readLong();
        /**
         * 设置正在处理的数目
         */
        String hostAddress = msg.sender().getAddress().getHostAddress();
        for (Endpoint endpoint : endpointList) {
            if (endpoint.getHost().equals(hostAddress)) {
                endpoint.reqNum.decrementAndGet();
                break;
            }
        }
        // 封装返回response
        FullHttpResponse response = HttpSimpleHandler.reqList.get(requestId);
        response.retain();
        response.headers().set(CONTENT_LENGTH, content.readableBytes());
        response.content().writeBytes(content);

        Channel rchannel = HttpSimpleHandler.channelList.get(requestId);
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
                interList.add(new InetSocketAddress(endpoint.getHost(), endpoint.getPort()));
                // HttpServerHandler.udpReqList.add(new ArrayList<>());
                logger.info("udp get endpoint: " + endpoint.toString());
            }
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
                interList.remove(new InetSocketAddress(endpoint.getHost(), endpoint.getPort()));
                logger.info("close channel; endpoint: " + endpoint.toString());
            }
        }
        LOCK.AgentChannelLock = false;
        return true;
    }

    public static List<Endpoint> getEndpointList() {
        return endpointList;
    }

    public static List<InetSocketAddress> getInterList() {
        return interList;
    }
}
