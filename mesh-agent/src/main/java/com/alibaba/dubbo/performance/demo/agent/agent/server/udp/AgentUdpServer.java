package com.alibaba.dubbo.performance.demo.agent.agent.server.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-26 下午11:42
 */
public class AgentUdpServer {

    private int port;
    private ChannelFuture channelFuture;
    private EventLoopGroup group = new NioEventLoopGroup();

    public AgentUdpServer bind(int port) {
        this.port = port;
        return this;
    }


    public AgentUdpServer start() {
        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, false)
                .handler(new ServerUdpHandler());
        channelFuture = b.bind(port);
        return this;
    }

    public void sync() {
        try {
            channelFuture.sync().channel().closeFuture().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }


}
