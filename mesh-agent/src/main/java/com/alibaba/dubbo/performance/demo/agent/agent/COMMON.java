package com.alibaba.dubbo.performance.demo.agent.agent;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-06 下午8:26
 */
public class COMMON {

    public static final String ServiceName = "com.alibaba.dubbo.performance.demo.provider.IHelloServiceGGL";
    // 消息分割符号
    public static final short MAGIC = (short) 0xdabb;

    /**
     * 一个请求中公共的字段
     */
    public static class Request {
        public static final String interfacename = "com.alibaba.dubbo.performance.demo.provider.IHelloService";
        public static final String method = "hash";
        public static final String parameterTypesString = "Ljava/lang/String";
    }


}
