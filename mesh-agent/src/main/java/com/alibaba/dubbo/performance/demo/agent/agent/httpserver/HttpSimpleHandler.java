package com.alibaba.dubbo.performance.demo.agent.agent.httpserver;

import com.alibaba.dubbo.performance.demo.agent.agent.client.udp.AgentUdpClient;
import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerRpcHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class HttpSimpleHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(AgentServerRpcHandler.class);

    private boolean isover = true;
    private int length;
    private CompositeByteBuf groubleBuf;

    private static final int CONTENT_INDEX = 143;

    private static AtomicInteger classCount = new AtomicInteger(0);

    public static ThreadLocal<AgentUdpClient> udpClientContext = new ThreadLocal<>();

    public int index = 0;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        if (udpClientContext.get() == null) {
            AgentUdpClient agentUdpClient = new AgentUdpClient(ctx.channel().eventLoop());
            index = agentUdpClient.putChannel(ctx.channel());
            udpClientContext.set(agentUdpClient);
        } else {
            AgentUdpClient agentUdpClient = udpClientContext.get();
            index = agentUdpClient.putChannel(ctx.channel());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        if (getBody(buf)) {
            udpClientContext.get().send(groubleBuf, index);
        }

    }


    public HttpSimpleHandler() {
        super();
        logger.info("new HttpServerHandler count: " + classCount.incrementAndGet());
    }

    public boolean getBody(ByteBuf buf) {
        if (isover) {
            length = getBodyLength(buf);
            int begin = CONTENT_INDEX;
            for (; begin < buf.readableBytes(); begin++) {
                if (buf.getByte(begin) == 105 && buf.getByte(begin + 1) == 110 && buf.getByte(begin + 2) == 116) {
                    break;
                }
            }
            groubleBuf = PooledByteBufAllocator.DEFAULT.compositeBuffer();
            buf.readerIndex(begin);
            groubleBuf.addComponent(true, buf);
            if (groubleBuf.readableBytes() == length) {
                return true;
            }
            isover = false;
            return false;
        } else {
            groubleBuf.addComponent(true, buf);
            if (groubleBuf.readableBytes() == length) {
                isover = true;
                return true;
            }
            return false;
        }
    }

    /**
     * get length of body
     *
     * @param buf
     * @return
     */
    public int getBodyLength(ByteBuf buf) {

        int temp;
        int len = buf.getByte(33) - '0';
        for (int i = 34; ; i++) {
            temp = buf.getByte(i);
            if (temp < 48 || temp > 57) break;
            len = len * 10 + temp - '0';
        }

        //  logger.info("http get body length:" + len);
        return len;
    }
}
