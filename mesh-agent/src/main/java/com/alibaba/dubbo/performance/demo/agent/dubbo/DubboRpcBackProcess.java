package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerRpcHandler;
import com.alibaba.dubbo.performance.demo.agent.tools.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

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

        long id = byteBuf.getLong(2);
        byteBuf.skipBytes(8);
        byteBuf.setLong(8, id);
        byteBuf.retain();
        byteBuf.writerIndex(byteBuf.writerIndex() - 3);
        byteBuf.writeShort(COMMON.MAGIC);
        AgentServerRpcHandler.channel.writeAndFlush(byteBuf);
    }


}
