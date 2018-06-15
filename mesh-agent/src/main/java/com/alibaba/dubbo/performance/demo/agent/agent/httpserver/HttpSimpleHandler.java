package com.alibaba.dubbo.performance.demo.agent.agent.httpserver;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.agent.client.udp.AgentUdpClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class HttpSimpleHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpSimpleHandler.class);

    private boolean isOver = true;
    private int length;
    private CompositeByteBuf globalBuf;


    private static final int CONTENT_INDEX = 145;

    private static final int left = -136 + 16 + COMMON.Request.dubbo_msg_first.length;

    private static AtomicInteger classCount = new AtomicInteger(0);
    private static AtomicInteger msgCount = new AtomicInteger(0);

    public static ThreadLocal<AgentUdpClient> udpClientContext = new ThreadLocal<>();

    public static ThreadLocal<ByteBuf> headerThreadLocal = new ThreadLocal<>();

    public int index = 0;

    public AgentUdpClient agentUdpClient;

    public ByteBuf header;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        /**
         * 每一个线程绑定一个udp
         */
        boolean isInitThread = (udpClientContext.get() == null);
        if (isInitThread) {
            AgentUdpClient agentUdpClient = new AgentUdpClient(ctx.channel().eventLoop());
            this.agentUdpClient = agentUdpClient;
            index = agentUdpClient.putChannel(ctx.channel());
            udpClientContext.set(agentUdpClient);
        } else {
            this.agentUdpClient = udpClientContext.get();
            index = agentUdpClient.putChannel(ctx.channel());
        }
        /**
         * 每一个线程都对groubleBuf进行初始化
         */
        if (isInitThread) {
            /** 加入请求头 */
            this.header = PooledByteBufAllocator.DEFAULT.directBuffer(16 + COMMON.Request.dubbo_msg_first.length);
            header.writeByte(-38);
            header.writeByte(-69);
            header.writeByte(-58);
            header.writeByte(0);
            header.writeLong(0);
            header.writeInt(0);
            header.writeBytes(COMMON.Request.dubbo_msg_first_buffer.copy());
            header.retain();
            /** 加入尾号参数*/
           // logger.info("init " + header.readerIndex() + "   " + header.writerIndex());
            headerThreadLocal.set(header);
        } else {
            this.header = headerThreadLocal.get();
        }

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        // ByteBufUtils.printStringln(buf, "--------------------------------------\n");
        if (getBody(buf)) {
            // logger.info("msgCount " + msgCount.incrementAndGet());
            // ByteBufUtils.printDubboMsg(globalBuf);
            //ByteBufUtils.println(globalBuf, " send ");
            agentUdpClient.send(globalBuf);
        }

    }


    public HttpSimpleHandler() {
        super();
          logger.info("new HttpServerHandler count: " + classCount.incrementAndGet());
    }

    public boolean getBody(ByteBuf buf) {
        if (isOver) {
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
            globalBuf = PooledByteBufAllocator.DEFAULT.compositeBuffer();
            /**
             * 拼接dubbo请求体
             */
            /** 加入请求头 */
            header.setInt(4, index);
            header.setInt(12, COMMON.Request.dubbo_msg_first.length + COMMON.Request.dubbo_msg_last.length + length - 136);
            globalBuf.addComponent(true, header);

            header.retain();
            /** 加入参数头 */
            buf.readerIndex(begin + 136);
            globalBuf.addComponent(true, buf);
            /**
             * 是否结束判断
             */
            if (globalBuf.readableBytes() == length + left) {
                /** 加入消息尾 */
                globalBuf.addComponent(true, COMMON.Request.dubbo_msg_last_buffer);
                COMMON.Request.dubbo_msg_last_buffer.retain();
                return true;
            }
            isOver = false;
            return false;
        } else {
            globalBuf.addComponent(true, buf);
            if (globalBuf.readableBytes() == length + left) {
                /** 加入消息尾 */
                globalBuf.addComponent(true, COMMON.Request.dubbo_msg_last_buffer);
                COMMON.Request.dubbo_msg_last_buffer.retain();
                isOver = true;
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
