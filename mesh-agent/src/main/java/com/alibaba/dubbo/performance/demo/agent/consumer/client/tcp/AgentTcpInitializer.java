package com.alibaba.dubbo.performance.demo.agent.consumer.client.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;

public class AgentTcpInitializer extends ChannelInitializer<NioSocketChannel> {

    public AgentTcpClient agentTcpClient;

    @Override
    protected void initChannel(NioSocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LineBasedFrameDecoder(1024, true, true));
        pipeline.addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                super.channelActive(ctx);
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                ByteBuf in = (ByteBuf) msg;

                try {
                    agentTcpClient.response(in);
                } finally {
                    in.release();
                }
            }
        });
    }

    public AgentTcpInitializer(AgentTcpClient agentTcpClient) {
        this.agentTcpClient = agentTcpClient;
    }
}