package com.alibaba.dubbo.performance.demo.agent.agent.server;

import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-05 下午8:44
 */
public class AgentServerRpcHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static RpcClient rpcClient = RpcClient.getInstance();

    public static Channel channel;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

        if (msg.readableBytes() < 9) return;
        rpcClient.sendDubbo(msg);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channel = ctx.channel();
    }
}
