package com.alibaba.dubbo.performance.demo.agent.agent.httpserver;

import com.alibaba.dubbo.performance.demo.agent.agent.client.AgentClientConnectPool;
import com.alibaba.dubbo.performance.demo.agent.agent.client.udp.AgentUdpClient;
import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerRpcHandler;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-09 上午12:43
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

    private static AgentUdpClient agentUdpClient = AgentUdpClient.getInstance();

    private static AtomicInteger classCount = new AtomicInteger(0);

    public static List<Channel> channelList = new ArrayList<>(900);

    public static List<FullHttpResponse> reqList = new ArrayList<>(512);

    static {
        for (int i = 0; i < 512; i++) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
            reqList.add(response);
        }
    }

    private int channelIndex = 0;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        synchronized (HttpServerHandler.class) {
            channelIndex = channelList.size();
            channelList.add(ctx.channel());
            logger.info("add channel, channel size: " + channelList.size());
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        agentUdpClient.send(request.content(), channelIndex);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("remove channel, channel size: " + channelList.size());
        channelList.remove(ctx.channel());
    }

    public HttpServerHandler() {
        super();
        logger.info("new HttpServerHandler count: " + classCount.incrementAndGet());
    }
}
