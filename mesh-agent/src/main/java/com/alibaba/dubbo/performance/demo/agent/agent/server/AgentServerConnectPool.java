package com.alibaba.dubbo.performance.demo.agent.agent.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-05 下午8:44
 */
public class AgentServerConnectPool {

    // 接收client连接的线程
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    // 工作处理线程
    EventLoopGroup workerGroup = new NioEventLoopGroup(4);
    // 辅助对象
    ServerBootstrap serverBootstrap = new ServerBootstrap();

    public AgentServerConnectPool() {
        init();
    }

    // server init
    public void init() {
        serverBootstrap.group(bossGroup, workerGroup)
                //我要指定使用NioServerSocketChannel这种类型的通道
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                //一定要使用 childHandler 去绑定具体的 事件处理器
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        ChannelPipeline pipeline = sc.pipeline();
                        pipeline.addLast(new LineBasedFrameDecoder(2048));
                        pipeline.addLast(new AgentServerRpcHandler());
                    }
                });

        try {

            ChannelFuture f = serverBootstrap.bind(Integer.parseInt(System.getProperty("nio.port"))).sync();
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
