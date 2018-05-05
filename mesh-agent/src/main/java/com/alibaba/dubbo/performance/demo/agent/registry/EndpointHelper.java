package com.alibaba.dubbo.performance.demo.agent.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * 描述: 做节点的负载均衡算法，用于选取即节点
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-05 上午9:40
 */
public class EndpointHelper {

    private Logger logger = LoggerFactory.getLogger(EndpointHelper.class);

    private List<Endpoint> endpoints;
    private Random random = new Random();

    private IRegistry registry = EtcdRegistry.etcdFactory(System.getProperty("etcd.url"));

    public String getBalancePointUrl() throws Exception {
        if (endpoints == null) {
            synchronized (EndpointHelper.class) {
                if (endpoints == null) {
                    endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
                }
            }
        }

        Endpoint endpoint = endpoints.get(random.nextInt(endpoints.size()));

        return "http://127.0.0.1:" + endpoint.getPort();
    }


    public List<Endpoint> getEndpoints() {
        return endpoints;
    }


}
