package com.alibaba.dubbo.performance.demo.agent.agent;

import com.alibaba.dubbo.performance.demo.agent.agent.server.udp.ServerUdpHandler;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.*;

// 一个ConnecManager对应于一个连接
public class ConnecManager {
    private static Logger logger = LoggerFactory.getLogger(ConnecManager.class);

    private Bootstrap bootstrap;


    public ConnecManager(EventLoop loop, ServerUdpHandler udpHandler, Class<? extends ChannelInitializer<NioSocketChannel>> initializer) {
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
        bootstrap = initBootstrap(channelInitializer, loop);
    }

    public ConnecManager(EventLoopGroup loop, Class<? extends ChannelInitializer<NioSocketChannel>> initializer) {
        ChannelInitializer<NioSocketChannel> channelInitializer = null;
        try {
            channelInitializer = initializer.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        bootstrap = initBootstrap(channelInitializer, loop);
    }

    /**
     * 绑定一个Endpoint，并指定通道数目
     *
     * @param host
     * @param port
     * @return
     */
    public FutureTask<Channel> bind(String host, int port) {
        logger.info(" new connect to " + host + ":" + port);
        Callable<Channel> callable = () -> {
            int i = 1;
            Channel channel = null;
            while (true) {
                try {
                    channel = bootstrap.connect(host, port).sync().channel();
                    i++;
                    if (channel.isActive()) {
                        logger.info("get channel: " + channel.localAddress() + "->" + channel.remoteAddress());
                        break;
                    }
                } catch (Exception e) {
                    logger.info("try " + (i++) + " times to connect " + host + ":" + port + JSON.toJSONString(channel));
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
            return channel;
        };

        FutureTask<Channel> callableFutureTask = new FutureTask<>(callable);
        Thread thread = new Thread(callableFutureTask);
        thread.start();

        return callableFutureTask;
    }


    /**
     * 初始化bootstrap
     *
     * @param initializer
     * @return
     */
    public Bootstrap initBootstrap(ChannelInitializer<NioSocketChannel> initializer, EventLoop loop) {

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

    public Bootstrap initBootstrap(ChannelInitializer<NioSocketChannel> initializer, EventLoopGroup loopGroup) {

        return new Bootstrap()
                .group(loopGroup)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP, true)
                .option(ChannelOption.SO_SNDBUF, 25600000)
                .channel(NioSocketChannel.class)
                .handler(initializer);
    }
}
