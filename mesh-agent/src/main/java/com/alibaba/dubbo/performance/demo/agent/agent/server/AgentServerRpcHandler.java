package com.alibaba.dubbo.performance.demo.agent.agent.server;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
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

    public static Channel channel;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        // TCP 拆包问题  //将ctx上下文放到一个map中，最后根据id，返回给相应的客户端
        String message = msg.toString(Charset.defaultCharset());
        String[] msgs = message.split("\\" + COMMON.AttributeSeparator);
        if (msgs.length == 4) {
            String[] copy = new String[5];
            for (int i = 0; i < 4; i++)
                copy[i] = msgs[i];
            copy[4] = "";
            msgs = copy;
        }
        rpcClient.invoke(msgs);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channel = ctx.channel();
    }
}
