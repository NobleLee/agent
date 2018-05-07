package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerRpcHandler;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class DubboRpcDecoder extends ChannelInboundHandlerAdapter {
    // header length.
    protected static final int HEADER_LENGTH = 17;

    // 接收dubbo消息，并将消息传送给client
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf data = (ByteBuf) msg;
        try {
            ByteBuf byteBuf = decode3(data);
            AgentServerRpcHandler.channel.writeAndFlush(byteBuf);
        } finally {
            data.release();
        }

    }

    private ByteBuf decode3(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        byte[] resByte = new byte[bytes.length - HEADER_LENGTH + 7];
        Bytes.copy(0, resByte, bytes, 4, 8);
        Bytes.copy(8, resByte, bytes, HEADER_LENGTH + 1, bytes.length - HEADER_LENGTH - 1);


        return Unpooled.copiedBuffer(resByte);
    }
}
