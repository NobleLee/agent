package com.alibaba.dubbo.performance.demo.agent.provider.dubbo;

import com.alibaba.dubbo.performance.demo.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.provider.dubbo.LoadBalance.MyInBoundHandler;
import com.alibaba.dubbo.performance.demo.agent.provider.dubbo.LoadBalance.MyOutBoundHandler;
import com.alibaba.dubbo.performance.demo.agent.provider.server.tcp.ServerTcpHandler;
import com.alibaba.dubbo.performance.demo.agent.provider.server.udp.ServerUdpHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-23 下午10:56
 */
public class DubboClientInitializer extends ChannelInitializer<NioSocketChannel> {

    private ServerUdpHandler udpHandler = null;

    private ServerTcpHandler tcpHandler = null;

    @Override
    protected void initChannel(NioSocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        if (COMMON.DUBBO_REQUEST_CONTROL_FLAG) {
            pipeline.addLast(new MyOutBoundHandler());
        }
        pipeline.addLast(new LengthFieldBasedFrameDecoder(2048, 12, 4, 0, 0));
        if (COMMON.DUBBO_REQUEST_CONTROL_FLAG)
            pipeline.addLast(new MyInBoundHandler());

        if (COMMON.isUdp) {
            pipeline.addLast(new DubboClientHandler(udpHandler));
        } else {
            pipeline.addLast(new DubboClientHandler(tcpHandler));
        }
    }

    public DubboClientInitializer(ServerUdpHandler handler) {
        this.udpHandler = handler;
    }

    public DubboClientInitializer(ServerTcpHandler handler) {
        this.tcpHandler = handler;
    }

    public DubboClientInitializer() {
    }
}
