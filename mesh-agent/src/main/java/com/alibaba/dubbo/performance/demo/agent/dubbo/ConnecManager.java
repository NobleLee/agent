package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

// 一个ConnecManager对应于一个连接
public class ConnecManager {
    private EventLoopGroup eventLoopGroup;

    private Bootstrap bootstrap;

    private Channel channel;
    private Object lock = new Object();

    private Endpoint endpoint;


    public ConnecManager(String host, int port, int nThread, ChannelInitializer<NioSocketChannel> initializer) {
        eventLoopGroup = new NioEventLoopGroup(nThread);
        endpoint = new Endpoint(host, port);
        initBootstrap(initializer);
    }

    public Channel getChannel() throws Exception {
        if (null != channel) {
            return channel;
        }

        if (null == channel) {
            synchronized (lock) {
                if (null == channel) {
                    channel = bootstrap.connect(endpoint.getHost(), endpoint.getPort()).sync().channel();
                }
            }
        }

        return channel;
    }

    public void initBootstrap(ChannelInitializer<NioSocketChannel> initializer) {

        bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                .channel(NioSocketChannel.class)
                .handler(initializer);
    }
}
