package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-23 下午10:56
 */
public class DubboClientInitializer extends ChannelInitializer<NioSocketChannel> {
    ByteBuf delimiter = Unpooled.copyShort(COMMON.MAGIC);

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // pipeline.addLast(new DubboRpcEncoder());

        if(COMMON.DUBBO_REQUEST_CONTROL_FLAG){
            pipeline.addLast(new MyOutBoundHandler());
        }
        pipeline.addLast(new DelimiterBasedFrameDecoder(2048, delimiter));
        if(COMMON.DUBBO_REQUEST_CONTROL_FLAG)
            pipeline.addLast(new MyInBoundHandler());
        pipeline.addLast(new DubbRpcBackProcess());
    }
}
