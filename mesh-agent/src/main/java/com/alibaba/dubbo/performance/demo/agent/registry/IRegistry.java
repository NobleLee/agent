package com.alibaba.dubbo.performance.demo.agent.registry;

import java.util.List;

public interface IRegistry {

    // 注册服务
    void register(String serviceName, List<Integer> ports) throws Exception;

    void find(String serviceName) throws Exception;
}
