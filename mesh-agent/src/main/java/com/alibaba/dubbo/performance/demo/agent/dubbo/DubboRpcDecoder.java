package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerRpcHandler;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Bytes;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Arrays;


public class DubboRpcDecoder extends ChannelInboundHandlerAdapter {
    // header length.
    protected static final int HEADER_LENGTH = 16;

    // 接收dubbo消息，并将消息传送给client
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf data = (ByteBuf) msg;
        try {
            ByteBuf byteBuf = decode3(data);
            AgentServerRpcHandler.channel.writeAndFlush(byteBuf);
        } finally {
          //  data.release();
        }

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

    private ByteBuf decode3(ByteBuf byteBuf) {
        //ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(byteBuf.readableBytes() - HEADER_LENGTH + 7);
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        byte[] resByte = new byte[bytes.length - HEADER_LENGTH + 7];
        Bytes.copy(0, resByte, bytes, 4, 8);
        Bytes.copy(8, resByte, bytes, HEADER_LENGTH + 1, bytes.length - HEADER_LENGTH - 1);
        System.err.println(Arrays.toString(resByte));
        //buffer.writeBytes(resByte);
        //Unpooled.copiedBuffer(resByte);
//        buffer.writeBytes(byteBuf, 4, 8);
//        buffer.writeBytes(byteBuf, HEADER_LENGTH + 1, byteBuf.readableBytes());

        return Unpooled.copiedBuffer(resByte);
    }
}
