package com.alibaba.dubbo.performance.demo.agent.provider.server.udp;

import com.alibaba.dubbo.performance.demo.agent.provider.dubbo.DubboClient;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-28 下午11:28
 */
public class ServerUdpHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static Logger logger = LoggerFactory.getLogger(ServerUdpHandler.class);

    private static AtomicLong channelCount = new AtomicLong(0);
    private static AtomicLong msgCount = new AtomicLong(0);

    private DubboClient dubboClient;

    public Channel channel;

    public InetSocketAddress address;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
        if (address == null) {
            synchronized (this) {
                if (address == null) {
                    address = msg.sender();
                }
            }
        }
        dubboClient.sendDubboDirect(msg.content());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channel = ctx.channel();
        dubboClient = new DubboClient(channel.eventLoop(), this);
        logger.info("udp server channel active count: " + channelCount.incrementAndGet());
    }


}

