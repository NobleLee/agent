package com.alibaba.dubbo.performance.demo.agent.agent.server.udp;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-26 下午11:42
 */
public class AgentUdpServer {

    private int portNum;

    public static List<Integer> portList = new ArrayList<>();

    public static List<ChannelFuture> futureList = new ArrayList<>();

    public AgentUdpServer bind(int portNum) {
        this.portNum = portNum;
        return this;
    }

    public AgentUdpServer start() {
        /**
         * 启动多个channel
         */
        for (int i = 0; i < portNum; i++) {
            EventLoopGroup group = new NioEventLoopGroup(1);
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, false)
                    .handler(new ServerUdpHandler());

            Channel channel = b.bind(0).channel();
            while (true) {
                if (channel.localAddress() != null) break;
            }
            InetSocketAddress socketAddress = (InetSocketAddress) channel.localAddress();
            portList.add(socketAddress.getPort());
            futureList.add(channel.closeFuture());
        }
        return this;
    }

    public void sync() {
        try {
            for (ChannelFuture channelFuture : futureList) {
                channelFuture.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
