package com.alibaba.dubbo.performance.demo.agent.agent.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-07 下午4:00
 */
public class AgentClientResponseDecoder extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        try {
            if (in.readableBytes() < 8) return;
            // 获取请求id
            long requestId = in.readLong();
            // 发送请求数据
            ChannelFuture channelFuture = AgentClientConnectPool
                    .requestHolderMap
                    .remove(requestId)
                    .writeAndFlush(in);
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        } finally {
            in.release();
        }

    }
}
