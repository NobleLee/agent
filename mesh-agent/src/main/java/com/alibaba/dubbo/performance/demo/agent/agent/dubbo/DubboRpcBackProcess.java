package com.alibaba.dubbo.performance.demo.agent.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.server.udp.ServerUdpHandler;
import com.alibaba.dubbo.performance.demo.agent.tools.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-24 下午3:37
 */
public class DubboRpcBackProcess extends ChannelInboundHandlerAdapter {


    // 接收dubbo消息，并将消息传送给client
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        if (byteBuf.readableBytes() < 6) return;

        long id = byteBuf.getLong(4);
        byteBuf.skipBytes(8);
        byteBuf.setLong(8, id);
        byteBuf.retain();
        byteBuf.writerIndex(byteBuf.writerIndex() - 1);
        //ByteBufUtils.printStringln(byteBuf, 8, "dubbo get massage back: ");
        ServerUdpHandler.channel.writeAndFlush(new DatagramPacket(byteBuf, ServerUdpHandler.socketAddress));

    }


}
