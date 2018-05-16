package com.alibaba.dubbo.performance.demo.agent.dubbo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

public class RpcClientInitializer extends ChannelInitializer<EpollSocketChannel> {
    @Override
    protected void initChannel(EpollSocketChannel socketChannel) {
        ByteBuf delimiter = Unpooled.copiedBuffer(new byte[]{-38, -69});
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new DelimiterBasedFrameDecoder(2048, delimiter));
        pipeline.addLast(new DubboRpcEncoder());
        pipeline.addLast(new DubboRpcDecoder());
    }
}
