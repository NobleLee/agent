package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerRpcHandler;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Bytes;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.Arrays;
import java.util.List;

public class DubboRpcDecoder extends ChannelInboundHandlerAdapter {
    // header length.
    protected static final int HEADER_LENGTH = 16;

    // 接收dubbo消息，并将消息传送给client
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = decode3(msg);
        AgentServerRpcHandler.channel.writeAndFlush(byteBuf);
        byteBuf.release();
    }

    private Object decode2(ByteBuf byteBuf) {
        byte[] data = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(data);

        byte[] subArray = Arrays.copyOfRange(data, HEADER_LENGTH + 1, data.length);

        String s = new String(subArray);

        byte[] requestIdBytes = Arrays.copyOfRange(data, 4, 12);
        long requestId = Bytes.bytes2long(requestIdBytes, 0);

        RpcResponse response = new RpcResponse();
        response.setRequestId(String.valueOf(requestId));
        response.setBytes(subArray);


        return response;
    }

    private ByteBuf decode3(Object byteBuf) {
        ByteBuf result = (ByteBuf) byteBuf;
        ByteBuf heapBuf = Unpooled.buffer(result.readableBytes() - HEADER_LENGTH + 7);
        heapBuf.setBytes(0, result, 4, 8);
        heapBuf.setBytes(8, result, HEADER_LENGTH, result.readableBytes());
        return heapBuf;
    }
}
