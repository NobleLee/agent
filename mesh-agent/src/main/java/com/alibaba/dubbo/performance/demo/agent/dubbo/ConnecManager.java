package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerRpcHandler;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
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
    private static Logger logger = LoggerFactory.getLogger(AgentServerRpcHandler.class);

    private EventLoopGroup eventLoopGroup;

    private Bootstrap bootstrap;

    private Channel channel;

    private Endpoint endpoint;


    public ConnecManager(String host, int port, int nThread, ChannelInitializer<NioSocketChannel> initializer) {
        logger.info("new connect to " + host + ":" + port);
        eventLoopGroup = new NioEventLoopGroup(nThread);
        endpoint = new Endpoint(host, port);
        bootstrap = initBootstrap(initializer);
        try {
            logger.info("get channel!");
            channel = bootstrap.connect(endpoint.getHost(), endpoint.getPort()).sync().channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("connected " + host + ":" + port);
    }

    public Channel getChannel() {
        return channel;
    }

    public Bootstrap initBootstrap(ChannelInitializer<NioSocketChannel> initializer) {
        logger.info("init bootstrap....");
        return bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channel(NioSocketChannel.class)
                .handler(initializer);
    }
}
