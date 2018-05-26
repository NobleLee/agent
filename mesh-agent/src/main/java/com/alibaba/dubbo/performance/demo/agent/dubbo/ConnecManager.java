package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerRpcHandler;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// 一个ConnecManager对应于一个连接
public class ConnecManager {
    private static Logger logger = LoggerFactory.getLogger(ConnecManager.class);

    private Bootstrap bootstrap;
    private int nThread;

    public ConnecManager(int nThred, Class<? extends ChannelInitializer<EpollSocketChannel>> initializer) {
        this.nThread = nThred;
        bootstrap = initBootstrap(initializer);
    }

    /**
     * 绑定一个Endpoint，并指定通道数目
     *
     * @param endpoint
     * @return
     */
    public Channel bind(Endpoint endpoint) {
        logger.info("connected number:" + COMMON.HTTPSERVER_WORK_THREAD + "new connect to " + endpoint.getHost() + ":" + endpoint.getPort());
        Channel channel = null;
        try {
            channel = bootstrap.connect(endpoint.getHost(), endpoint.getPort()).sync().channel();
            logger.info("get channel!" + channel.toString());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return channel;
    }


    /**
     * 初始化bootstrap
     *
     * @param initializerClass
     * @return
     */
    public Bootstrap initBootstrap(Class<? extends ChannelInitializer<EpollSocketChannel>> initializerClass) {
        ChannelInitializer<EpollSocketChannel> initializer = null;
        try {
            initializer = initializerClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        EventLoopGroup eventLoopGroup = new EpollEventLoopGroup(nThread);
        return new Bootstrap()
                .group(eventLoopGroup)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channel(EpollSocketChannel.class)
                .handler(initializer);
    }
}
