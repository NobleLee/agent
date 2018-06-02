package com.alibaba.dubbo.performance.demo.agent.agent.server.udp;

import com.alibaba.dubbo.performance.demo.agent.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.agent.httpserver.HTTPServer;
import com.alibaba.dubbo.performance.demo.agent.tools.ByteBufUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-28 下午11:28
 */
public class ServerUdpHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static Logger logger = LoggerFactory.getLogger(HTTPServer.class);

    private static AtomicLong channelCount = new AtomicLong(0);

    private static AtomicLong objectCount = new AtomicLong(0);

    private static AtomicLong msgCount = new AtomicLong(0);

    private static RpcClient rpcClient = RpcClient.getInstance();

    public static Channel channel;

    public static InetSocketAddress socketAddress;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
//        ByteBufUtils.printStringln(msg.content(), 8, "udp get massage: ");
        if (socketAddress == null) {
            synchronized (this) {
                if (socketAddress == null) {
                    socketAddress = new InetSocketAddress(msg.sender().getAddress(), msg.sender().getPort());
                }
            }
        }
        logger.info("udp server get msg count: " + msgCount.incrementAndGet());
        rpcClient.sendDubboDirect(msg.content());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channel = ctx.channel();
        logger.info("udp server channel active count: " + channelCount.getAndIncrement());
    }


    public ServerUdpHandler() {
        logger.info("udp object count: " + objectCount.getAndIncrement());
    }
}

