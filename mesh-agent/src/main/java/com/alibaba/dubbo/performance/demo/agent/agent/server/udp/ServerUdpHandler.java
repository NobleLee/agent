package com.alibaba.dubbo.performance.demo.agent.agent.server.udp;

import com.alibaba.dubbo.performance.demo.agent.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.agent.httpserver.HTTPServer;
import com.alibaba.dubbo.performance.demo.agent.tools.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-28 下午11:28
 */
public class ServerUdpHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static Logger logger = LoggerFactory.getLogger(HTTPServer.class);

    private static AtomicLong channelCount = new AtomicLong(0);

    private RpcClient rpcClient;

    public Channel channel;

    public List<InetSocketAddress> socketAddress = new ArrayList<>();

    public HashMap<InetSocketAddress, Integer> addressHashMap = new HashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
        if (!addressHashMap.containsKey(msg.sender())) {
            synchronized (this) {
                if (!addressHashMap.containsKey(msg.sender())) {
                    addressHashMap.put(msg.sender(), socketAddress.size());
                    socketAddress.add(msg.sender());
                    logger.info("port count " + socketAddress.size() + " " + addressHashMap.size() + " " + msg.sender());
                }
            }
        }
        rpcClient.sendDubboDirect(msg.content(), addressHashMap.get(msg.sender()));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channel = ctx.channel();
        rpcClient = new RpcClient(channel.eventLoop(), this);
        logger.info("udp server channel active count: " + channelCount.incrementAndGet());
    }


}

