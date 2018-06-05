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

    // 设置门限值
    private static final double up_gate = 0.38;
    private static final double medium_gate = 0.76;

    // 负载均衡算法，最好选择轮转算法，如果采用概率选择算法性能应该会受限
    public static Endpoint getBalancePoint(List<Endpoint> endpoints) {
        /**
         * 随机负载均衡
         * */
       // return endpoints.get(random.nextInt(endpoints.size()));

        /***
         * 按照1：1：0的方式
         */
        if (Math.random() < 0.5) {
            return endpoints.get(2);
        }
        return endpoints.get(1);


        /**
         * 按照200：200：112的比例进行请求
         * */
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
         * */
//        int min = endpoints.get(2).questNum;
//        Endpoint res = endpoints.get(2);
//        for (int i = 1; i >= 0; i--) {
//            if (endpoints.get(i).questNum < min) {
//                res = endpoints.get(i);
//                min = res.questNum;
//            }
//        }
//        return res;
    }


}
