package com.alibaba.dubbo.performance.demo.agent.dubbo.LoadBalance;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyInBoundHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(MyInBoundHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        /*ByteBuf result = ((ByteBuf) msg).copy();
        byte[] result1 = new byte[result.readableBytes()];
        result.readBytes(result1);
        String resultStr = new String(result1);
        System.out.println("qxc inbound:" + resultStr);*/
        ByteBuf byteBuf = BufferQueue.inBoundRequestFilter();
        if (byteBuf != null) {
            ctx.writeAndFlush(byteBuf);
        }else{
            // logger.info("the requestCount:"+BufferQueue.requestCount);
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
