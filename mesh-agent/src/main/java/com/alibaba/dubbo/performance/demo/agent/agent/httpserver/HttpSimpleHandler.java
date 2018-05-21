package com.alibaba.dubbo.performance.demo.agent.agent.httpserver;

import com.alibaba.dubbo.performance.demo.agent.agent.client.AgentClientConnectPool;
import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerRpcHandler;
import com.alibaba.dubbo.performance.demo.agent.tools.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class HttpSimpleHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(AgentServerRpcHandler.class);
    private static AgentClientConnectPool agentClientConnectPool = AgentClientConnectPool.getInstance();
    private boolean isover = true;
    private ByteBuf groubleBuf;
    private int length;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.info("get consumer http connected!!!");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        ByteBufUtils.printStringln(buf, "get msg:\n");
        if (getBody(buf)) {
            ByteBufUtils.printStringln(groubleBuf, "get complete:\n");
            agentClientConnectPool.responseTest(groubleBuf, ctx.channel());
        }

    }


    public boolean getBody(ByteBuf buf) {
        if (isover) {
            length = getBodyLength(buf);
            groubleBuf = PooledByteBufAllocator.DEFAULT.directBuffer(length);
            int i = 84;
            for (; i < buf.readableBytes() - 3; i++) {
                if (buf.getByte(i) == 13 && buf.getByte(i + 1) == 10 && buf.getByte(i + 2) == 13 && buf.getByte(i + 3) == 10) {
                    break;
                }
            }
            buf.skipBytes(i + 4);

            buf.readBytes(groubleBuf,buf.readableBytes());
            if (groubleBuf.readableBytes() == length) {
                return true;
            }
            isover = false;
            return false;
        } else {
            buf.readBytes(groubleBuf);
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
        int len = buf.getByte(33);
        len -= '0';
        for (int i = 34; i < 38; i++) {
            if (buf.getByte(i) == 13 && buf.getByte(i + 1) == 10)
                return len;
            len = len * 10 + buf.getByte(i) - '0';
        }
        logger.error("http get body length error!");
        return 0;
    }
}
