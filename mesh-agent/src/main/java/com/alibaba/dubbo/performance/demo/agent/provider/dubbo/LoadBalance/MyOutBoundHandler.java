package com.alibaba.dubbo.performance.demo.agent.provider.dubbo.LoadBalance;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyOutBoundHandler extends ChannelOutboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(MyOutBoundHandler.class);

    @Override
    // 向client发送消息
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        if (!BufferQueue.outBoundRequestFilter((ByteBuf) msg)) {
            // 执行下一个OutboundHandler
            super.write(ctx, msg, promise);
        }

    }

}
