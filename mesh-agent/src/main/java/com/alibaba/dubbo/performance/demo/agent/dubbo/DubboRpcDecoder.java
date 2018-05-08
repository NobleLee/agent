package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.ByteBufUtils;
import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerRpcHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class DubboRpcDecoder extends ChannelInboundHandlerAdapter {


    protected final static byte[] MAGIC = new byte[]{-38, -69};

    // 接收dubbo消息，并将消息传送给client
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        try {
            if (byteBuf.readableBytes() < 6) return;
            ByteBufUtils.println(byteBuf, "received: ");

            int readlength = byteBuf.readableBytes();
            ByteBuf buffer = PooledByteBufAllocator.DEFAULT.directBuffer(readlength - 5);
            buffer.writeBytes(MAGIC);
            buffer.writeBytes(byteBuf, 2, 8);
            buffer.writeBytes(byteBuf, 15, readlength - 15);

            ByteBufUtils.println(buffer, "send: ");
            AgentServerRpcHandler.channel.writeAndFlush(buffer);
        } finally {
            byteBuf.release();
        }

    }


}
