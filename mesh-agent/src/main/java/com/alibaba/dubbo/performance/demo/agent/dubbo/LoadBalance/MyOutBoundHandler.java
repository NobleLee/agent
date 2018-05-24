package com.alibaba.dubbo.performance.demo.agent.dubbo.LoadBalance;

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
        /*ByteBuf result=((ByteBuf)msg).copy();
        byte[] result1 = new byte[result.readableBytes()];
        result.readBytes(result1);
        String resultStr = new String(result1);
        logger.info("qxc outbound:" + resultStr);*/
        if(BufferQueue.outBoundRequestFilter((ByteBuf)msg)){
        }else{
            // 执行下一个OutboundHandler
            super.write(ctx, msg, promise);
        }

    }

}
