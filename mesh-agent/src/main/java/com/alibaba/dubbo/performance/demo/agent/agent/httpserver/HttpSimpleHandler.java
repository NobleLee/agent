package com.alibaba.dubbo.performance.demo.agent.agent.httpserver;

import com.alibaba.dubbo.performance.demo.agent.agent.client.udp.AgentUdpClient;
import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerRpcHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

public class HttpSimpleHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(AgentServerRpcHandler.class);

    private boolean isover = true;
    private int length;
    private ByteBuf groubleBuf = PooledByteBufAllocator.DEFAULT.directBuffer(1536);

    private static final int CONTENT_INDEX = 143;

    private static AgentUdpClient agentUdpClient = AgentUdpClient.getInstance();

    private static AtomicInteger classCount = new AtomicInteger(0);

    public static List<Channel> channelList = new ArrayList<>(600);

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
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("remove channel, channel size: " + channelList.size());
        channelList.remove(ctx.channel());
        groubleBuf.release();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        if (getBody(buf)) {
            agentUdpClient.send(groubleBuf, channelIndex);
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
            groubleBuf.retain();
            groubleBuf.resetWriterIndex();
            groubleBuf.resetReaderIndex();
            buf.readerIndex(begin);
            buf.readBytes(groubleBuf, buf.readableBytes());
            if (groubleBuf.readableBytes() == length) {
                return true;
            }
            isover = false;
            return false;
        } else {
            groubleBuf.writeBytes(buf);
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
