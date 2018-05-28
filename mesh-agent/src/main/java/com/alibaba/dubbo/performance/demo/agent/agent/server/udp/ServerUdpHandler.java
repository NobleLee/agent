package com.alibaba.dubbo.performance.demo.agent.agent.server.udp;

import com.alibaba.dubbo.performance.demo.agent.agent.httpserver.HTTPServer;
import com.alibaba.dubbo.performance.demo.agent.tools.ByteBufUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static AtomicLong objectCount = new AtomicLong(0);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
        ByteBufUtils.println(msg.content(), "udp get massage: ");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.info("udp server channel active count: " + channelCount.getAndIncrement());
    }

    public ServerUdpHandler() {
        logger.info("udp object count: " + objectCount.getAndIncrement());
    }
}

