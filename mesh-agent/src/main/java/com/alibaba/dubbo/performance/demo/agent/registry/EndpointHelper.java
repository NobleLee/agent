package com.alibaba.dubbo.performance.demo.agent.registry;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
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

    private Random random = new Random();

    private static IRegistry registry = EtcdRegistry.etcdFactory(System.getProperty("etcd.url"));

    private static EndpointHelper instance;

    // 单例模式
    public static EndpointHelper getInstance() {
        if (instance == null) {
            synchronized (EndpointHelper.class) {
                if (instance == null) {
                    instance = new EndpointHelper();
                }
            }
        }
        return instance;
    }

    private EndpointHelper() {
    }

    // 负载均衡算法，最好选择轮转算法，如果采用概率选择算法性能应该会受限
    public Endpoint getBalancePoint(List<Endpoint> endpoints) throws Exception {
        return endpoints.get(random.nextInt(endpoints.size()));
    }


}
