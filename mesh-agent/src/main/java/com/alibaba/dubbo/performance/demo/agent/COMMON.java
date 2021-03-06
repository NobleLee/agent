package com.alibaba.dubbo.performance.demo.agent;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述: 公共配置信息
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-06 下午8:26
 */
public class COMMON {

    public static final String ServiceName = "com.alibaba.dubbo.performance.demo.provider.IHelloServiceGGL";

    // 作为HTTP server的线程配置
    public static final int HTTPSERVER_BOSS_THREAD = 1;
    public static final int HTTPSERVER_WORK_THREAD = 16;
    // consumer server
    public static final int AGENTSERVER_BOSS_THREAD = 1;
    public static final int AGENTSERVER_WORK_THREAD = HTTPSERVER_WORK_THREAD;

    public static final boolean isUdp = true;

    // socket
    public static final int BACK_LOG = 1024;


    // 连接配置
    public static final int DubboClient_Num = HTTPSERVER_WORK_THREAD;


    /**
     * 一个请求中公共的字段
     */
    public static class Request {
        public static final String interfacename = "com.alibaba.dubbo.performance.demo.provider.IHelloService";
        public static final String method = "hash";
        public static final String parameterTypesString = "Ljava/lang/String;";
        // 定死消息体
        public static final byte[] dubbo_msg_first = "\"2.0.1\"\n\"com.alibaba.dubbo.performance.demo.provider.IHelloService\"\nnull\n\"hash\"\n\"Ljava/lang/String;\"\n\"".getBytes();
        public static final byte[] dubbo_msg_last = "\"\n{\"path\":\"com.alibaba.dubbo.performance.demo.provider.IHelloService\"}".getBytes();
        public static final ByteBuf dubbo_msg_first_buffer = PooledByteBufAllocator.DEFAULT.directBuffer(dubbo_msg_first.length);
        public static final ByteBuf dubbo_msg_last_buffer = PooledByteBufAllocator.DEFAULT.directBuffer(dubbo_msg_last.length);

        static {
            dubbo_msg_first_buffer.writeBytes(dubbo_msg_first);
            dubbo_msg_last_buffer.writeBytes(dubbo_msg_last);
            dubbo_msg_first_buffer.retain();
            dubbo_msg_last_buffer.retain();
        }
    }

    /**
     * @author qiuxiaochen
     */
    // 是否开启在provider-agent端的请求控制
    public static final boolean DUBBO_REQUEST_CONTROL_FLAG = false;
    // Dubbo客户端缓冲队列的大小
    public static final int DUBBO_CLIENT_BUFFER_SIZE = 512;
    // Dubbo服务器端请求的上限
    public static final int DUBBO_SERVER_HANDLE_THRESHOLD = 200;

    /**
     * 对agent进行编号
     */
    public static Map<String, Short> CLIENT_ORDER_MAP =
            new ImmutableMap.Builder<String, Short>().
                    put("consumer", (short) 3).
                    put("provider-large", (short) 2).
                    put("provider-medium", (short) 1).
                    put("provider-small", (short) 0).
                    build();

}
