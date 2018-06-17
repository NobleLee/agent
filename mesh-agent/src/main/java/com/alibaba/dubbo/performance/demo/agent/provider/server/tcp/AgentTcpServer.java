package com.alibaba.dubbo.performance.demo.agent.provider.server.tcp;

import com.alibaba.dubbo.performance.demo.agent.COMMON;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-05 下午8:44
 */
public class AgentTcpServer {

    // 接收client连接的线程
    private EventLoopGroup bossGroup = new NioEventLoopGroup(COMMON.AGENTSERVER_BOSS_THREAD);
    // 工作处理线程
    private EventLoopGroup workerGroup = new NioEventLoopGroup(COMMON.AGENTSERVER_WORK_THREAD);
    // 辅助对象
    private ServerBootstrap serverBootstrap = new ServerBootstrap();

    int port;
    private ChannelFuture channelFuture;

    public AgentTcpServer(int port) {
        this.port = port;
    }

    // server init
    public AgentTcpServer init() {
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_BACKLOG, COMMON.BACK_LOG)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP, true)
                .childOption(ChannelOption.SO_SNDBUF, 256 * 1024)
                .option(ChannelOption.SO_RCVBUF, 256 * 1024)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel sc) {
                        ChannelPipeline pipeline = sc.pipeline();
                        pipeline.addLast(new ServerTcpHandler());
                    }
                });

        try {
            channelFuture = serverBootstrap.bind(port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 监听事件关闭
     */
    public void sync() {
        try {
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


}
