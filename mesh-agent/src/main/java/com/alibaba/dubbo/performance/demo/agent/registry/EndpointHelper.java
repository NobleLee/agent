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

    private List<Endpoint> endpoints; //之后可以考虑把初始化的过程放到前面
    private Random random = new Random();

    private IRegistry registry = EtcdRegistry.etcdFactory(System.getProperty("etcd.url"));

    // 对节点设置watcher监控rpc节点的变更
    private void watch() {}

    public String getBalancePointUrl() throws Exception {
        if (endpoints == null) {
            synchronized (EndpointHelper.class) {
                if (endpoints == null) {
                    endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
                }
            }
        }

        Endpoint endpoint = getBlancePoint();

        return "http://" + endpoint.getHost() + ":" + endpoint.getPort();
    }

    // 负载均衡算法，最好选择轮转算法，如果采用概率选择算法性能应该会受限
    private Endpoint getBlancePoint() {
        return endpoints.get(random.nextInt(endpoints.size()));
    }




    public List<Endpoint> getEndpoints() {
        return endpoints;
    }
}
