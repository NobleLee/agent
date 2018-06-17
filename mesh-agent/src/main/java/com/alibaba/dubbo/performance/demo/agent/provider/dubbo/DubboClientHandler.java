package com.alibaba.dubbo.performance.demo.agent.provider.dubbo;

import com.alibaba.dubbo.performance.demo.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.provider.server.tcp.ServerTcpHandler;
import com.alibaba.dubbo.performance.demo.agent.provider.server.udp.ServerUdpHandler;
import com.alibaba.dubbo.performance.demo.agent.tools.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.alibaba.dubbo.performance.demo.agent.COMMON.CLIENT_ORDER_MAP;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-24 下午3:37
 */
public class DubboClientHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(DubboClientHandler.class);

    private ServerUdpHandler udpHandler = null;

    private ServerTcpHandler tcpHandler = null;

    private static final short CLIENT_INDEX = CLIENT_ORDER_MAP.get(System.getProperty("label"));

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
        if (COMMON.isUdp) {
            handleUdp(byteBuf);
            udpHandler.channel.writeAndFlush(new DatagramPacket(byteBuf, udpHandler.address));
        } else {
            handleTcp(byteBuf);
            tcpHandler.channel.writeAndFlush(byteBuf);
        }


    }

    /**
     * udp 对返回进行封装
     *
     * @param byteBuf
     */
    private void handleUdp(ByteBuf byteBuf) {
        int id = (int) byteBuf.getLong(4);
        byteBuf.skipBytes(14);
        byteBuf.setInt(14, id);
        byteBuf.writerIndex(byteBuf.writerIndex() - 1);
    }

    /**
     * tcp 对返回进行封装
     *
     * @param byteBuf
     */
    private void handleTcp(ByteBuf byteBuf) {
        int id = (int) byteBuf.getLong(4);
        byteBuf.skipBytes(14);
        byteBuf.setInt(12, id);
        byteBuf.setShort(16, CLIENT_INDEX);
    }


    public DubboClientHandler(ServerUdpHandler handler) {
        this.udpHandler = handler;
    }

    public DubboClientHandler(ServerTcpHandler handler) {
        this.tcpHandler = handler;
    }

    public DubboClientHandler() {
    }
}
