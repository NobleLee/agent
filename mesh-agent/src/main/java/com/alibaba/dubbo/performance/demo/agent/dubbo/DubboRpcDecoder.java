package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.ByteBufUtils;
import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerRpcHandler;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Bytes;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Arrays;


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

    private RpcResponse decode2(byte[] data) {

        byte[] subArray = Arrays.copyOfRange(data, HEADER_LENGTH + 1, data.length);

        byte[] requestIdBytes = Arrays.copyOfRange(data, 4, 12);
        long requestId = Bytes.bytes2long(requestIdBytes, 0);

        RpcResponse response = new RpcResponse();
        response.setRequestId(String.valueOf(requestId));
        response.setBytes(subArray);

        return response;
    }

    private ByteBuf decode3(ByteBuf byteBuf) {
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.directBuffer(byteBuf.readableBytes() - HEADER_LENGTH + 7);

        buffer.writeBytes(byteBuf, 4, 8);
        buffer.writeBytes(byteBuf, HEADER_LENGTH + 1, byteBuf.readableBytes() - HEADER_LENGTH - 1);

        return buffer;
    }
}
