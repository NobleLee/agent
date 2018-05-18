package com.alibaba.dubbo.performance.demo.agent.agent.server;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-05 下午8:44
 */
public class AgentServerConnectPool {

    // 接收client连接的线程
    EventLoopGroup bossGroup = new NioEventLoopGroup(COMMON.AGENTSERVER_BOSS_THREAD);
    // 工作处理线程
    EventLoopGroup workerGroup = new NioEventLoopGroup(COMMON.AGENTSERVER_WORK_THREAD);
    // 辅助对象
    ServerBootstrap serverBootstrap = new ServerBootstrap();

    int port;

    public AgentServerConnectPool(int port) {
        this.port = port;
    }

    // server init
    public void init() {
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    ByteBuf delimiter = Unpooled.copyShort(COMMON.MAGIC);

                    @Override
                    protected void initChannel(NioSocketChannel sc) throws Exception {
                        ChannelPipeline pipeline = sc.pipeline();
                        pipeline.addLast(new DelimiterBasedFrameDecoder(2048, delimiter));
                        pipeline.addLast(new AgentServerRpcHandler());
                    }
                });

        try {

            ChannelFuture f = serverBootstrap.bind(port).sync();


            f.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }


}
