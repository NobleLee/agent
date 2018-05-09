package com.alibaba.dubbo.performance.demo.agent.agent.client;

import com.alibaba.dubbo.performance.demo.agent.tools.Bytes;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcFuture;
import com.alibaba.dubbo.performance.demo.agent.tools.RpcRequestHolder;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Arrays;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-07 下午4:00
 */
public class AgentClientResponseDecoder extends ChannelInboundHandlerAdapter {


    private RpcRequestHolder<RpcFuture> requestHolder = RpcRequestHolder.getRpcRequestHolderByName("agentClient");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        try {
            if (in.readableBytes() < 8) return;
            byte[] rev = new byte[in.readableBytes()];

            in.readBytes(rev);
            long requestId = Bytes.bytes2long(Arrays.copyOfRange(rev, 0, 8), 0);
            byte[] subArray = Arrays.copyOfRange(rev, 8, rev.length);
            RpcResponse response = new RpcResponse();
            response.setBytes(subArray);
            response.setRequestId(String.valueOf(requestId));

//            RpcFuture future = requestHolder.get(response.getRequestId());
//            if (null != future) {
//                requestHolder.remove(response.getRequestId());
//                future.done(response);
//            }
        } finally {
            in.release();
        }

    }
}
