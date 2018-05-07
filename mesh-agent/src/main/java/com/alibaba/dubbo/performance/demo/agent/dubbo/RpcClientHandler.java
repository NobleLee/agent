package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcRequestHolder;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

       private RpcRequestHolder<ChannelHandlerContext> requestHolder = RpcRequestHolder.getRpcRequestHolderByName("agentServerResponse");

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) {
        String requestId = response.getRequestId();
        ChannelHandlerContext agentServerCtx = requestHolder.get(requestId);
        agentServerCtx.writeAndFlush(response);
        requestHolder.remove(requestId);
    }

}
