package com.alibaba.dubbo.performance.demo.agent.agent.dubbo.LoadBalance;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


public class BufferQueue {

    private static Logger logger = LoggerFactory.getLogger(BufferQueue.class);

    // 当Dubbo服务器端处理请求数量到达上限的缓冲队列
    public static LinkedBlockingQueue<ByteBuf> bufferqueue = new LinkedBlockingQueue<>(COMMON.DUBBO_CLIENT_BUFFER_SIZE);

    // 标记到达dubbo服务器端的请求数量
    public static AtomicInteger requestCount = new AtomicInteger(0);

    // dubbo服务器端请求的上限
    public static final int requestMaxCount = COMMON.DUBBO_SERVER_HANDLE_THRESHOLD;

    public static boolean outBoundRequestFilter(ByteBuf byteBuf) {
        if (requestCount.get() >= requestMaxCount) {
            boolean flag = bufferqueue.offer(byteBuf);
            if (!flag) {
               logger.error("offer the Queue is fail: the size is " + bufferqueue.size());
            }
            return flag;
        } else {
            requestCount.incrementAndGet();
            return false;
        }
    }

    public static ByteBuf inBoundRequestFilter() {

        if (requestCount.decrementAndGet() < requestMaxCount && !bufferqueue.isEmpty()) {
            return bufferqueue.poll();
        }
        return null;
    }
}
