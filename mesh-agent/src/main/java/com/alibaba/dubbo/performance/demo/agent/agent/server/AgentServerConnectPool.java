package com.alibaba.dubbo.performance.demo.agent.agent.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

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
    ServerBootstrap bootstrap = new ServerBootstrap();

    public AgentServerConnectPool() {
        try {
            init();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void init() throws InterruptedException {
        bootstrap.group(bossGroup, workerGroup)
                //我要指定使用NioServerSocketChannel这种类型的通道
                .channel(NioServerSocketChannel.class)
                //一定要使用 childHandler 去绑定具体的 事件处理器
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        sc.pipeline().addLast(new AgentServerRpcHandler());
                    }
                });
        ChannelFuture f = bootstrap.bind(Integer.parseInt(System.getProperty("server.port"))).sync();
        f.channel().closeFuture().sync();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }


}
