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

    private static Random random = new Random();

    private EndpointHelper() {
    }

    // 负载均衡算法，最好选择轮转算法，如果采用概率选择算法性能应该会受限
    public static Endpoint getBalancePoint(List<Endpoint> endpoints) throws Exception {
        return endpoints.get(random.nextInt(endpoints.size()));
    }


}
