package com.alibaba.dubbo.performance.demo.agent.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.client.udp.AgentUdpClient;
import com.alibaba.dubbo.performance.demo.agent.agent.server.udp.ServerUdpHandler;
import com.alibaba.dubbo.performance.demo.agent.tools.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-24 下午3:37
 */
public class DubboRpcBackProcess extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(DubboRpcBackProcess.class);

    private ServerUdpHandler handler = null;

    // 接收dubbo消息，并将消息传送给client
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        if (byteBuf.getByte(3) != 20) {
            logger.error(ByteBufUtils.getDubboMsg(byteBuf));
            return;
        }

        /***
         *  对消息进行封装
         */
        int id = byteBuf.getInt(4);
        int port = byteBuf.getInt(8);
        byteBuf.skipBytes(14);
        byteBuf.setInt(14, id);
        byteBuf.retain();
        byteBuf.writerIndex(byteBuf.writerIndex() - 1);
        handler.channel.writeAndFlush(new DatagramPacket(byteBuf, new InetSocketAddress(handler.address.getAddress(), port)));

        byteBuf.release();

    }

    public DubboRpcBackProcess(ServerUdpHandler handler) {
        this.handler = handler;
    }

    public DubboRpcBackProcess() {
    }
}
