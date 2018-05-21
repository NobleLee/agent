package com.alibaba.dubbo.performance.demo.agent.agent.httpserver;

import com.alibaba.dubbo.performance.demo.agent.agent.client.AgentClientConnectPool;
import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerRpcHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-09 上午12:43
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);
    private static AgentClientConnectPool agentClientConnectPool = AgentClientConnectPool.getInstance();
    private static AtomicInteger connectCount = new AtomicInteger(0);
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.error(connectCount.getAndIncrement() + " get consumer http connected!!!");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        //400
        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        // System.err.println(request.content().copy().toString(Charsets.UTF_8));
        //agentClientConnectPool.sendToServer(request.content(), ctx.channel());
        logger.info("the channel is "+ctx.channel().toString() +" the ctx name is "+ctx.name());
        agentClientConnectPool.sendToServer(request.content(), ctx.channel());
    }

    private static void sendError(ChannelHandlerContext ctx,
                                  HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                status, Unpooled.copiedBuffer("Failure: " + status.toString()
                + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


}
