package com.alibaba.dubbo.performance.demo.agent.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.server.udp.ServerUdpHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-24 下午3:37
 */
public class DubboRpcBackProcess extends ChannelInboundHandlerAdapter {

    private ServerUdpHandler handler = null;

    // 接收dubbo消息，并将消息传送给client
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        if (byteBuf.getByte(2) != 6) return;

        /***
         *  对消息进行封装
         */
        int id = (int) byteBuf.getLong(4);
        byteBuf.skipBytes(18);
        byteBuf.retain();
        byteBuf.writerIndex(byteBuf.writerIndex() - 1);
        handler.channel.writeAndFlush(new DatagramPacket(byteBuf, handler.socketAddress.get(id)));

        byteBuf.release();

    }

    public DubboRpcBackProcess(ServerUdpHandler handler) {
        this.handler = handler;
    }


}
