package com.alibaba.dubbo.performance.demo.agent.provider.server.tcp;

import com.alibaba.dubbo.performance.demo.agent.provider.dubbo.DubboClient;
import com.alibaba.dubbo.performance.demo.agent.provider.server.udp.ServerUdpHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-05 下午8:44
 */
public class ServerTcpHandler extends ChannelInboundHandlerAdapter {


    private static Logger logger = LoggerFactory.getLogger(ServerUdpHandler.class);

    private static AtomicLong channelCount = new AtomicLong(0);

    private DubboClient dubboClient;

    public Channel channel;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        dubboClient.sendDubboDirect((ByteBuf) msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channel = ctx.channel();
        dubboClient = new DubboClient(channel.eventLoop(), this);
        logger.info("tcp server channel active count: " + channelCount.incrementAndGet());
    }
}
