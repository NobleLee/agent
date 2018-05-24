package com.alibaba.dubbo.performance.demo.agent.agent;

/**
 * 描述: 公共配置信息
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-06 下午8:26
 */
public class COMMON {

    public static final String ServiceName = "com.alibaba.dubbo.performance.demo.provider.IHelloService_GGL";
    // 消息分割符号
    public static final short MAGIC = (short) 0xdabb;

    // 作为HTTP server的线程配置
    public static final int HTTPSERVER_BOSS_THREAD = 2;
    public static final int HTTPSERVER_WORK_THREAD = 8;
    // agent server
    public static final int AGENTSERVER_BOSS_THREAD = 1;
    public static final int AGENTSERVER_WORK_THREAD = 8;

    // socket
    public static final int BACK_LOG = 1024;


    // 连接配置
    public static final int DubboClient_NUM = HTTPSERVER_WORK_THREAD;
    public static final int DUBBO_CLIENT_THREAD = 2;

    public static final int AgentClient_NUM = HTTPSERVER_WORK_THREAD;
    public static final int AgentClient_THREAD = 2;


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
    }


}
