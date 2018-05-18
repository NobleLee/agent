package com.alibaba.dubbo.performance.demo.agent.registry;

public interface IRegistry {

    // 注册服务
    void register(String serviceName, int port) throws Exception;

    void find(String serviceName) throws Exception;
}
