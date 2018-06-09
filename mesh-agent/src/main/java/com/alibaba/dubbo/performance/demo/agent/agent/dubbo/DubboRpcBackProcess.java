package com.alibaba.dubbo.performance.demo.agent.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.dubbo.LoadBalance.BufferQueue;
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
        ByteBufUtils.println(byteBuf, "get dubbo byte");
        ByteBufUtils.printBinln(byteBuf, "get dubbo byte");
        if (byteBuf.getByte(2) != 6) return;

        /***
         *  对消息进行封装
         */
        long id = byteBuf.getLong(4);
        byteBuf.skipBytes(10);
        byteBuf.setLong(10, id);
        byteBuf.retain();
        byteBuf.writerIndex(byteBuf.writerIndex() - 1);
        ServerUdpHandler.channel.writeAndFlush(new DatagramPacket(byteBuf, ServerUdpHandler.socketAddress));

        byteBuf.release();
//        long id = byteBuf.getLong(4);
//        byteBuf.skipBytes(6);
//        byteBuf.setLong(6, id);
//        /**
//         * 报文中增加正在处理的消息数目
//         */
//        int count = BufferQueue.bufferqueue.isEmpty() ? BufferQueue.requestCount.get() : BufferQueue.requestMaxCount;
//        byteBuf.setInt(14, count);
//        byteBuf.retain();
//        byteBuf.writerIndex(byteBuf.writerIndex() - 1);
//        ServerUdpHandler.channel.writeAndFlush(new DatagramPacket(byteBuf, ServerUdpHandler.socketAddress));


    }


}
