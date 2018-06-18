package com.alibaba.dubbo.performance.demo.agent.consumer.httpserver;

import com.alibaba.dubbo.performance.demo.agent.COMMON;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-09 上午12:30
 */
public class HTTPServer {
    private static Logger logger = LoggerFactory.getLogger(HTTPServer.class);

    EventLoopGroup bossGroup = new NioEventLoopGroup(COMMON.HTTPSERVER_BOSS_THREAD);
    public static EventLoopGroup workGroup = new NioEventLoopGroup(COMMON.HTTPSERVER_WORK_THREAD);

    // 开启服务
    public void start(final int port) {

        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, COMMON.BACK_LOG)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_SNDBUF, 256 * 1024)
                    .option(ChannelOption.SO_RCVBUF, 256 * 1024)
                    .childHandler(new HttpServerInitializer());

            ChannelFuture future = bootstrap.bind("localhost", port).sync();

            logger.info("HTTP Server startup.");

            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
