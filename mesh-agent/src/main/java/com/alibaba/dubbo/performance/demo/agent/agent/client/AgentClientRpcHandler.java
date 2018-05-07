package com.alibaba.dubbo.performance.demo.agent.agent.client;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcFuture;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcRequestHolder;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 描述: Agent client rpc process handler
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-05 下午8:22
 */
public class AgentClientRpcHandler extends SimpleChannelInboundHandler<RpcResponse> {


    private RpcRequestHolder<RpcFuture> requestHolder = RpcRequestHolder.getRpcRequestHolderByName("agentClient");


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        String requestId = response.getRequestId();
        RpcFuture future = requestHolder.get(requestId);
        if (null != future) {
            requestHolder.remove(requestId);
            future.done(response);
        }
    }
}
