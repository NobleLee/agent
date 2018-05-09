package com.alibaba.dubbo.performance.demo.agent.agent;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-06 下午8:26
 */
public class COMMON {
    // 消息之间的分割符号
    public static final String MessageSeparator = "\n";
    // 消息内部属性的分割符号
    public static final String AttributeSeparator = "+";
    public static final char AttributeSeparatorChar = '+';

    public static final String ServiceName = "com.alibaba.dubbo.performance.demo.provider.IHelloServiceGGL";

    public static final short MAGIC = (short) 0xdabb;

    /**
     * 一个请求中公共的字段
     */
    public static class Request {
        public static final String interfacename = "com.alibaba.dubbo.performance.demo.provider.IHelloService";
        public static final String method = "hash";
        public static final String parameterTypesString = "Ljava/lang/String;";
    }


}
