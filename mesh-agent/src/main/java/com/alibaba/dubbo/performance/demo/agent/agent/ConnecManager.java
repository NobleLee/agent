package com.alibaba.dubbo.performance.demo.agent.agent;

import com.alibaba.dubbo.performance.demo.agent.agent.server.udp.ServerUdpHandler;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

// 一个ConnecManager对应于一个连接
public class ConnecManager {
    private static Logger logger = LoggerFactory.getLogger(ConnecManager.class);

    private Bootstrap bootstrap;
    private EventLoop loop;


    public ConnecManager(EventLoop loop, ServerUdpHandler udpHandler, Class<? extends ChannelInitializer<NioSocketChannel>> initializer) {
        this.loop = loop;
        ChannelInitializer<NioSocketChannel> channelInitializer = null;
        Class<?>[] parTypes = new Class<?>[1];
        parTypes[0] = ServerUdpHandler.class;
        try {
            Constructor<?> constructor = initializer.getConstructor(parTypes);
            Object[] pars = new Object[1];
            pars[0] = udpHandler;
            channelInitializer = (ChannelInitializer<NioSocketChannel>) constructor.newInstance(pars);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        bootstrap = initBootstrap(channelInitializer);
    }

    public ConnecManager(EventLoop loop, Class<? extends ChannelInitializer<NioSocketChannel>> initializer) {
        this.loop = loop;
        ChannelInitializer<NioSocketChannel> channelInitializer = null;
        try {
            channelInitializer = initializer.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        bootstrap = initBootstrap(channelInitializer);
    }

    /**
     * 绑定一个Endpoint，并指定通道数目
     *
     * @param endpoint
     * @return
     */
    public Channel bind(Endpoint endpoint) {
        logger.info(" new connect to " + endpoint);

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
     * @param initializer
     * @return
     */
    public Bootstrap initBootstrap(ChannelInitializer<NioSocketChannel> initializer) {

        return new Bootstrap()
                .group(loop)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP, true)
                .option(ChannelOption.SO_SNDBUF, 25600000)
                .channel(NioSocketChannel.class)
                .handler(initializer);
    }
}
