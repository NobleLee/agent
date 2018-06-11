package com.alibaba.dubbo.performance.demo.agent.agent.client.udp;

import com.alibaba.dubbo.performance.demo.agent.agent.httpserver.HttpSimpleHandler;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EndpointHelper;
import com.alibaba.dubbo.performance.demo.agent.tools.ByteBufUtils;
import com.alibaba.dubbo.performance.demo.agent.tools.LOCK;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
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

    private Channel sendChannel;

    private Channel responseChannel;

    private static List<Endpoint> endpointList = new ArrayList<>();

    private static List<List<InetSocketAddress>> interList = new ArrayList<>();

    private FullHttpResponse response;


    public AgentUdpClient(EventLoop loop, Channel responseChannel) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(loop)
                .option(ChannelOption.SO_BROADCAST, false)
                .channel(NioDatagramChannel.class)
                .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
                        response(msg);
                    }
                });


        ChannelFuture bind = bootstrap.bind(0);
        sendChannel = bind.channel();

        response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");

        this.responseChannel = responseChannel;
    }


    /**
     * 给指定的server发送消息
     *
     * @param buf
     */
    public void send(ByteBuf buf) {
        if (buf.readableBytes() < 136) {
            logger.error("message length less 136 ");
            return;
        }
        buf.skipBytes(136);
        // 根据负载均衡算法，选择一个节点发送数据
        InetSocketAddress host = EndpointHelper.getBalancePoint(interList);
        ByteBufUtils.printStringln(buf, "send:");
        sendChannel.writeAndFlush(new DatagramPacket(buf, host));
    }

    /**
     * 将接收到的agent response返回
     *
     * @param msg
     */
    public void response(DatagramPacket msg) {
        ByteBuf content = msg.content();
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
        response.retain();
        response.headers().set(CONTENT_LENGTH, content.readableBytes());
        response.content().writeBytes(content);

        if (responseChannel != null && responseChannel.isActive()) {
            responseChannel.writeAndFlush(response);
        }
    }


    /**
     * 对每一个endpoint节点都创建一个通道
     *
     * @param endpoints
     * @return
     * @throws Exception
     */
    public synchronized static boolean putServers(List<Endpoint> endpoints) {
        // TODO  应该加锁
        LOCK.AgentChannelLock = true;
        for (Endpoint endpoint : endpoints) {
            if (!endpointList.contains(endpoint)) {
                endpointList.add(endpoint);
                List<InetSocketAddress> singleHost = new ArrayList<>();
                for (Integer integer : endpoint.getPort()) {
                    singleHost.add(new InetSocketAddress(endpoint.getHost(), integer));
                }
                interList.add(singleHost);
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
    public synchronized static boolean deleteServers(List<Endpoint> endpoints) {
        // TODO 应该加锁
        for (Endpoint endpoint : endpoints) {
            if (endpointList.contains(endpoint)) {
                int index = endpointList.indexOf(endpoint);
                endpointList.remove(endpoint);
                interList.remove(index);
                logger.info("close channel; endpoint: " + endpoint.toString());
            }
        }
        return true;
    }

}
