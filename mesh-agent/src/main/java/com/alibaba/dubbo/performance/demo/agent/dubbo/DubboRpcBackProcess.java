package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerRpcHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-24 下午3:37
 */
public class DubboRpcBackProcess extends ChannelInboundHandlerAdapter {


    // 接收dubbo消息，并将消息传送给client
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        try {
            // ByteBufUtils.println(byteBuf, "dubbo total return byte:");

            if (byteBuf.readableBytes() < 6) return;
            int readlength = byteBuf.readableBytes();
            ByteBuf buffer = PooledByteBufAllocator.DEFAULT.directBuffer(readlength - 7);

            buffer.writeBytes(byteBuf, 2, 8);
            buffer.writeBytes(byteBuf, 16, readlength - 17);
            buffer.writeShort(COMMON.MAGIC);
            //  ByteBufUtils.println(buffer, "agent return byte:");
            AgentServerRpcHandler.channel.writeAndFlush(buffer);
        } finally {
            byteBuf.release();
        }
    }

}
