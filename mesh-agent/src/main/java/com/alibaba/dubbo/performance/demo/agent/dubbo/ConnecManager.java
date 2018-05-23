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
import java.util.List;

// 一个ConnecManager对应于一个连接
public class ConnecManager {
    private static Logger logger = LoggerFactory.getLogger(AgentServerRpcHandler.class);

    private List<Channel> channels = new ArrayList<>();
    int nThread;
    private Endpoint endpoint;


    public ConnecManager(String host, int port, int nThread, ChannelInitializer<NioSocketChannel> initializer) {
        this.endpoint = new Endpoint(host, port);
        this.nThread = nThread;
        init(initializer);
    }


    /**
     * 初始化工作线程个数目的连接通道
     */
    private void init(ChannelInitializer<NioSocketChannel> initializer) {
        logger.info("connected number:" + COMMON.HTTPSERVER_WORK_THREAD + "new connect to " + endpoint.getHost() + ":" + endpoint.getPort());
        for (int i = 0; i < COMMON.HTTPSERVER_WORK_THREAD; i++) {
            Bootstrap bootstrap = initBootstrap(initializer);
            try {
                Channel channel = bootstrap.connect(endpoint.getHost(), endpoint.getPort()).sync().channel();
                logger.info("get channel!" + channel.toString());
                channels.add(channel);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public List<Channel> getChannel() {
        return channels;
    }

    /**
     * 初始化bootstrap
     *
     * @param initializer
     * @return
     */
    public Bootstrap initBootstrap(ChannelInitializer<NioSocketChannel> initializer) {
        logger.info("init bootstrap....");
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(nThread);
        return new Bootstrap()
                .group(eventLoopGroup)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channel(NioSocketChannel.class)
                .handler(initializer);
    }
}
