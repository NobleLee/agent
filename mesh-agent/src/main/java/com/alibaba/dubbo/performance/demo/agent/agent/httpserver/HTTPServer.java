package com.alibaba.dubbo.performance.demo.agent.agent.httpserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-09 上午12:30
 */
public class HTTPServer {
    // 开启服务
    public void start(final int port) {
        EventLoopGroup bossGroup = new EpollEventLoopGroup(1);
        EventLoopGroup workGroup = new EpollEventLoopGroup(8);
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(bossGroup, workGroup)
                    .channel(EpollServerSocketChannel.class)
                    .childHandler(new HttpChannelInitializer());

            ChannelFuture future = bootstrap.bind("localhost", port).sync();

            System.out.println("HTTP Server startup.");

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
