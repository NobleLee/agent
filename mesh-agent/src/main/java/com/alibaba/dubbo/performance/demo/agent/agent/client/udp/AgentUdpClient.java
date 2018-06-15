package com.alibaba.dubbo.performance.demo.agent.agent.client.udp;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EndpointHelper;
import com.alibaba.dubbo.performance.demo.agent.tools.LOCK;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
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
import java.util.concurrent.atomic.AtomicInteger;

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

    /**
     * 发送通道
     */
    private Channel sendChannel;

    /**
     * localhost port
     */
    private int localPort;

    /**
     * 存放本Client绑定的 http Channel
     */
    private List<Channel> responseChannelList = new ArrayList<>();

    /**
     * Http channel 在本Client中的编号
     */
    private int responseChannelCount = 0;

    /**
     * 存放所有的节点数目
     */
    private static List<Endpoint> endpointList = new ArrayList<>();
    /**
     * 需要发送的address地址
     */
    private static List<List<InetSocketAddress>> interList = new ArrayList<>();

    /**
     * 为防止每次都new，在初始化预先分配一个对象
     */
    private FullHttpResponse response;

    /**
     * 用于给Client进行编号
     */
    private static AtomicInteger agentUdpClientCount = new AtomicInteger(0);

    /**
     * 本Client的编号值
     */
    private int clientIndex;

    /**
     * 负载均衡相关
     */
    private int round = 0;


    public AgentUdpClient(EventLoop loop) {
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
        localPort = ((InetSocketAddress) sendChannel.localAddress()).getPort();

        response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        clientIndex = agentUdpClientCount.getAndIncrement() % interList.get(0).size();
        logger.info("client udp channel: " + sendChannel.localAddress() + "->");
    }

    public int putChannel(Channel channel) {
        responseChannelList.add(channel);
        return responseChannelCount++;
    }

    /**
     * 给指定的server发送消息
     *
     * @param buf
     */
    public void send(ByteBuf buf) {
        // 根据负载均衡算法，选择一个节点发送数据
        InetSocketAddress host = EndpointHelper.getBalancePoint(interList, endpointList, clientIndex, round);
        if (round >= 7)
            round = 0;
        else round++;
        buf.setInt(8, localPort);
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
        int id = content.readInt();
        Channel responseChannel = responseChannelList.get(id);
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
//        for (Endpoint endpoint : endpoints) {
//            if (endpointList.contains(endpoint)) {
//                int index = endpointList.indexOf(endpoint);
//                endpointList.remove(endpoint);
//                interList.remove(index);
//                logger.info("close channel; endpoint: " + endpoint.toString());
//            }
//        }
        return true;
    }

}
