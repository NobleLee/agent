package com.alibaba.dubbo.performance.demo.agent.agent.server;

import com.alibaba.dubbo.performance.demo.agent.agent.dubbo.RpcClient;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-05 下午8:44
 */
public class AgentServerRpcHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(AgentServerRpcHandler.class);

   // private static RpcClient rpcClient = RpcClient.getInstance();

    public static Channel channel;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf bufer = (ByteBuf) msg;
        //  ByteBufUtils.printStringln(msg,8,"send:");
        // TCP 拆包问题  //将ctx上下文放到一个map中，最后根据id，返回给相应的客户端
        if (bufer.readableBytes() < 9) return;
        // System.err.println(msg.copy().toString(Charsets.UTF_8));
        //rpcClient.sendDubbo(msg);
        // logger.info("有效信息数目 : " + msgVCount.getAndIncrement());
        //ByteBufUtils.printStringln(msg, "get agent msg:");
      //  rpcClient.sendDubboDirect(bufer);
        // rpcClient.sendBackTest(msg);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channel = ctx.channel();
        logger.info("agent server channel active! " + ctx.channel());
    }
}
