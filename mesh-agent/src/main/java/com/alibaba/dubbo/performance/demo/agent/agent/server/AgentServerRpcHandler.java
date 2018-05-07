package com.alibaba.dubbo.performance.demo.agent.agent.server;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcRequestHolder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-05 下午8:44
 */
public class AgentServerRpcHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private RpcClient rpcClient = new RpcClient();

    private RpcRequestHolder<ChannelHandlerContext> requestHolder = RpcRequestHolder.getRpcRequestHolderByName("agentServerResponse");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        // TCP 拆包问题  //将ctx上下文放到一个map中，最后根据id，返回给相应的客户端

        String message = msg.toString(Charset.defaultCharset());
        int idIndex = message.indexOf(COMMON.AttributeSeparatorChar);
        String idStr = message.substring(0, idIndex + 1);

        String[] msgs = message.split(COMMON.AttributeSeparator);

        requestHolder.put(idStr, ctx);
        rpcClient.invoke(msgs);
        System.out.println(message);
    }


}
