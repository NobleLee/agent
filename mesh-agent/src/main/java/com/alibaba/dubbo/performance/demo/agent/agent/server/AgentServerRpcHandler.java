package com.alibaba.dubbo.performance.demo.agent.agent.server;

import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.tools.ByteBufUtils;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-05 下午8:44
 */
public class AgentServerRpcHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static Logger logger = LoggerFactory.getLogger(AgentServerRpcHandler.class);

    private static RpcClient rpcClient = RpcClient.getInstance();

    public static Channel channel;


    public static AtomicLong msgVCount = new AtomicLong(1);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
         ByteBufUtils.printStringln(msg,8,"send:");
        // TCP 拆包问题  //将ctx上下文放到一个map中，最后根据id，返回给相应的客户端
        if (msg.readableBytes() < 9) return;
        // System.err.println(msg.copy().toString(Charsets.UTF_8));
        //rpcClient.sendDubbo(msg);
        // logger.info("有效信息数目 : " + msgVCount.getAndIncrement());
        //ByteBufUtils.printStringln(msg, "get agent msg:");
        rpcClient.sendDubboDirect(msg);
        // rpcClient.sendBackTest(msg);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channel = ctx.channel();
        logger.info("agent server channel active! " + ctx.channel());
    }
}
