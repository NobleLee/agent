package com.alibaba.dubbo.performance.demo.agent.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.agent.dubbo.LoadBalance.MyInBoundHandler;
import com.alibaba.dubbo.performance.demo.agent.agent.dubbo.LoadBalance.MyOutBoundHandler;
import com.alibaba.dubbo.performance.demo.agent.agent.server.udp.ServerUdpHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-23 下午10:56
 */
public class DubboClientInitializer extends ChannelInitializer<NioSocketChannel> {

    private ServerUdpHandler handler = null;

    @Override
    protected void initChannel(NioSocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        if (COMMON.DUBBO_REQUEST_CONTROL_FLAG) {
            pipeline.addLast(new MyOutBoundHandler());
        }
        pipeline.addLast(new LengthFieldBasedFrameDecoder(2048, 12, 4, 0, 0));
        if (COMMON.DUBBO_REQUEST_CONTROL_FLAG)
            pipeline.addLast(new MyInBoundHandler());
        pipeline.addLast(new DubboRpcBackProcess(handler));
    }

    public DubboClientInitializer(ServerUdpHandler handler) {
        this.handler = handler;
    }
}
