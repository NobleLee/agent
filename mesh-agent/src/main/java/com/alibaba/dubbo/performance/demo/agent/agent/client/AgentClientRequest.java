package com.alibaba.dubbo.performance.demo.agent.agent.client;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-06 上午12:14
 */
public class AgentClientRequest {
    private static AtomicLong atomicLong = new AtomicLong();
    private long id;


    private String interfaceName;
    private String method;
    private String parameterTypesString;
    private String parameter;

    public AgentClientRequest(String interfaceName, String method, String parameterTypesString, String parameter) {
        id = atomicLong.getAndIncrement();
        this.interfaceName = interfaceName;
        this.method = method;
        this.parameterTypesString = parameterTypesString;
        this.parameter = parameter;
    }


    // 获取要发送的消息
    ByteBuf getBuyteBuff() {
        String msg = String.valueOf(id) + COMMON.AttributeSeparator +
                interfaceName + COMMON.AttributeSeparator +
                method + COMMON.AttributeSeparator +
                parameterTypesString + COMMON.AttributeSeparator +
                parameter + COMMON.MessageSeparator;
        return Unpooled.copiedBuffer(msg.getBytes());
    }

    public Long getId() {
        return id;
    }

    public String getInterfaceNAme() {
        return interfaceName;
    }

    public void setInterfaceNAme(String interfaceNAme) {
        this.interfaceName = interfaceNAme;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getParameterTypesString() {
        return parameterTypesString;
    }

    public void setParameterTypesString(String parameterTypesString) {
        this.parameterTypesString = parameterTypesString;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
}
