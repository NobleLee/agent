package com.alibaba.dubbo.performance.demo.agent.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
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

    // 设置门限值
    private static final double up_gate = 0.38;
    private static final double medium_gate = 0.76;

    private static final int limit = 195;


    // 负载均衡算法，最好选择轮转算法，如果采用概率选择算法性能应该会受限
    public static int getBalancePoint(List<Endpoint> endpoints) {
        /**
         * 随机负载均衡
         */
        return random.nextInt(endpoints.size());

        /**
         * 按照1：1：0的方式
         */
//        if (Math.random() < 0.5) {
//            return endpoints.get(2);
//        }
//        return endpoints.get(1);

        /**
         * 尽可能的将请求打到最大的机器
         */
//        if (endpoints.get(2).reqNum.get() < limit) {
//            endpoints.get(2).reqNum.incrementAndGet();
//            return 2;
//        } else if (endpoints.get(1).reqNum.get() < limit) {
//            endpoints.get(1).reqNum.incrementAndGet();
//            return 1;
//        }
//        endpoints.get(0).reqNum.incrementAndGet();
//        return 0;

        /**
         * 按照200：200：112的比例进行请求
         */
//        double r = Math.random();
//
//        if (r < up_gate) {
//            return endpoints.get(2);
//        } else if (r < medium_gate) {
//            return endpoints.get(1);
//        }
//        return endpoints.get(0);

        /**
         * 统计请求数目分布
         */
//        long min = endpoints.get(0).reqNum.get() + 10;
//        Endpoint res = endpoints.get(0);
//        if (endpoints.get(1).reqNum.get() <= min) {
//            min = endpoints.get(1).reqNum.get();
//            res = endpoints.get(1);
//        }
//        if (endpoints.get(2).reqNum.get() <= min) {
//            res = endpoints.get(2);
//        }
//        res.reqNum.incrementAndGet();
//        return res;
    }

}
