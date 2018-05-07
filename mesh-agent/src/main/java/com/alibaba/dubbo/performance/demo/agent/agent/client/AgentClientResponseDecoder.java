package com.alibaba.dubbo.performance.demo.agent.agent.client;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Bytes;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcFuture;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcRequestHolder;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.Arrays;
import java.util.List;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-07 下午4:00
 */
public class AgentClientResponseDecoder extends ByteToMessageDecoder {


    private RpcRequestHolder<RpcFuture> requestHolder = RpcRequestHolder.getRpcRequestHolderByName("agentClient");

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        byte[] data = new byte[in.readableBytes()];
        in.readBytes(data);

        byte[] requestIdBytes = Arrays.copyOfRange(data, 0, 8);
        long requestId = Bytes.bytes2long(requestIdBytes, 0);
        byte[] subArray = Arrays.copyOfRange(data, 0, 8);

        RpcResponse response = new RpcResponse();
        response.setBytes(subArray);
        response.setRequestId("" + requestId);

        RpcFuture future = requestHolder.get("" + requestId);
        if (null != future) {
            requestHolder.remove("" + requestId);
            future.done(response);
        }
        in.release();
    }
}
