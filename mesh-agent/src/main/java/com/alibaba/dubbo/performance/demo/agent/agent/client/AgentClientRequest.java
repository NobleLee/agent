package com.alibaba.dubbo.performance.demo.agent.agent.client;

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

    private String msg;

    public AgentClientRequest() {
        id = atomicLong.getAndIncrement();
    }

    public AgentClientRequest(String msg) {
        this.msg = msg;
    }

    public Long getId() {
        return id;
    }

    public String getMsg() {
        return msg;
    }
}
