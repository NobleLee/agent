package com.alibaba.dubbo.performance.demo.agent.agent.httpserver;

import com.alibaba.dubbo.performance.demo.agent.agent.client.AgentClientConnectPool;
import com.google.common.base.Charsets;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.Arrays;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-09 上午12:43
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static AgentClientConnectPool agentClientConnectPool =  AgentClientConnectPool.getInstance();


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        //400
        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        String bodyString = request.content().toString(Charsets.UTF_8);
        System.err.println(bodyString);

        agentClientConnectPool.sendToServer(request.content(), ctx.channel());


        //       String bodyString = buf.toString(Charsets.UTF_8);

//            System.out.println("body: " + bodyString);
//
//
//
//
//        Map<String, String> parmMap = new RequestParser(request).parse();
//        System.err.println(parmMap);

        //     System.err.println(ctx.channel());

//        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK);
//
//
//       ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
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