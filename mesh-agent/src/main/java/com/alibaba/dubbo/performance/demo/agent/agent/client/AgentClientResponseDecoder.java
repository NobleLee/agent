package com.alibaba.dubbo.performance.demo.agent.agent.client;

import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerRpcHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-07 下午4:00
 */
public class AgentClientResponseDecoder extends ChannelInboundHandlerAdapter {
    private static Logger logger = LoggerFactory.getLogger(AgentClientResponseDecoder.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.info("connect channel!" + ctx.name());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        try {
            if (in.readableBytes() < 8) return;
            // 获取请求id
            long requestId = in.readLong();
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
            response.content().writeBytes(in);
            // 发送请求数据
            ChannelFuture channelFuture = AgentClientConnectPool
                    .requestHolderMap
                    .remove(requestId)
                    .writeAndFlush(response);
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        } finally {
            in.release();
        }
    }
}
