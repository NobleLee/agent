package com.alibaba.dubbo.performance.demo.agent.agent.httpserver;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.agent.client.udp.AgentUdpClient;
import com.alibaba.dubbo.performance.demo.agent.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerRpcHandler;
import com.alibaba.dubbo.performance.demo.agent.tools.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

public class HttpSimpleHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(AgentServerRpcHandler.class);

    private boolean isover = true;
    private int length;
    private CompositeByteBuf groubleBuf;

    private static final int CONTENT_INDEX = 143;

    private static final int left = -136 + 16 + COMMON.Request.dubbo_msg_first.length;

    private static AtomicInteger classCount = new AtomicInteger(0);
    private static AtomicInteger msgCount = new AtomicInteger(0);

    public static ThreadLocal<AgentUdpClient> udpClientContext = new ThreadLocal<>();

    public static ThreadLocal<ByteBuf> headerThreadLocal = new ThreadLocal<>();

    public long index = 0;

    public AgentUdpClient agentUdpClient;

    public ByteBuf header;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        /**
         * 每一个线程绑定一个udp
         */
        if (udpClientContext.get() == null) {
            AgentUdpClient agentUdpClient = new AgentUdpClient(ctx.channel().eventLoop());
            this.agentUdpClient = agentUdpClient;
            index = agentUdpClient.putChannel(ctx.channel());
            udpClientContext.set(agentUdpClient);
        } else {
            this.agentUdpClient = udpClientContext.get();
            index = agentUdpClient.putChannel(ctx.channel());
        }
        /**
         * 每一个线程绑定一个headf
         */
        if (headerThreadLocal.get() == null) {
            ByteBuf header = PooledByteBufAllocator.DEFAULT.directBuffer(16);
            header.writeByte(-38);
            header.writeByte(-69);
            header.writeByte(-58);
            header.writeByte(0);
            header.writeLong(0);
            header.writeInt(0);
            this.header = header;
            headerThreadLocal.set(header);
        } else {
            header = headerThreadLocal.get();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        ByteBufUtils.printStringln(buf, "----------------------\n");
        if (getBody(buf)) {
            //logger.info("msgCount " + msgCount.incrementAndGet());
            agentUdpClient.send(groubleBuf);
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
            ctx.writeAndFlush(response);
        }

    }


    public HttpSimpleHandler() {
        super();
        logger.info("new HttpServerHandler count: " + classCount.incrementAndGet());
    }

    public boolean getBody(ByteBuf buf) {
        if (isover) {
            /**
             * 获取包体长度
             */
            length = getBodyLength(buf);
            int begin = CONTENT_INDEX;
            for (; begin < buf.readableBytes(); begin++) {
                if (buf.getByte(begin) == 105 && buf.getByte(begin + 1) == 110 && buf.getByte(begin + 2) == 116) {
                    break;
                }
            }
            /**
             * 初始化一个消息
             */
            groubleBuf = PooledByteBufAllocator.DEFAULT.compositeBuffer();
            /**
             * 拼接dubbo请求体
             */
            /** 加入请求头 */
            header.setLong(4, index);
            header.setInt(12, COMMON.Request.dubbo_msg_first.length + COMMON.Request.dubbo_msg_last.length + length - 136);
            groubleBuf.addComponent(true, header);
            header.retain();
            /** 加入参数头 */
            groubleBuf.addComponent(true, COMMON.Request.dubbo_msg_first_buffer);
            COMMON.Request.dubbo_msg_first_buffer.retain();
            /** 加入buffer头 */
            buf.readerIndex(begin + 136);
            groubleBuf.addComponent(true, buf);
            buf.retain();
            /**
             * 是否结束判断
             */
            if (groubleBuf.readableBytes() == length + left) {
                /** 加入消息尾 */
                groubleBuf.addComponent(true, COMMON.Request.dubbo_msg_last_buffer);
                COMMON.Request.dubbo_msg_last_buffer.retain();
                return true;
            }
            isover = false;
            return false;
        } else {
            groubleBuf.addComponent(true, buf);
            buf.retain();
            if (groubleBuf.readableBytes() == length + left) {
                /** 加入消息尾 */
                groubleBuf.addComponent(true, COMMON.Request.dubbo_msg_last_buffer);
                COMMON.Request.dubbo_msg_last_buffer.retain();
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
