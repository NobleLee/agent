package com.alibaba.dubbo.performance.demo.agent.agent;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 一个ConnecManager对应于一个连接
public class ConnecManager {
    private static Logger logger = LoggerFactory.getLogger(ConnecManager.class);

    private Bootstrap bootstrap;
    private int nThread;

    public ConnecManager(int nThred, Class<? extends ChannelInitializer<NioSocketChannel>> initializer) {
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
        logger.info(" new connect to " + endpoint + "connected thread number: " + nThread);
        Channel channel = null;
        for (int i = 0; i < 100; i++) {
            try {
                channel = bootstrap.connect(endpoint.getHost(), endpoint.getPort()).sync().channel();
                logger.info("get channel: " + channel.toString());
                break;
            } catch (Exception e) {
                logger.error(e.toString());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    logger.error(e1.toString());
                }
                logger.info("try " + i + " times to connect " + endpoint);
            }
        }
        return channel;
    }


    /**
     * 初始化bootstrap
     *
     * @param initializerClass
     * @return
     */
    public Bootstrap initBootstrap(Class<? extends ChannelInitializer<NioSocketChannel>> initializerClass) {
        ChannelInitializer<NioSocketChannel> initializer = null;
        try {
            initializer = initializerClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(nThread);
        return new Bootstrap()
                .group(eventLoopGroup)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP, false)
                .option(ChannelOption.SO_SNDBUF, 25600000)
                .channel(NioSocketChannel.class)
                .handler(initializer);
    }
}
