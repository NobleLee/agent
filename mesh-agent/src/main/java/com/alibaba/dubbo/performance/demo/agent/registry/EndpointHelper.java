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

    private static final int limit = 190;


    // 负载均衡算法，最好选择轮转算法，如果采用概率选择算法性能应该会受限
    public static InetSocketAddress getBalancePoint(List<List<InetSocketAddress>> interList, List<Endpoint> endpoints, int index, int round) {
        /**
         * 随机负载均衡
         */
//        int hostIndex = random.nextInt(interList.size());
//        return interList.get(hostIndex).get(index);

        /**
         * 按照1：1：0的方式
         */
//        if (Math.random() <= 0.5) {
//            return interList.get(2).get(index);
//        }
//        return interList.get(1).get(index);

        /**
         * 尽可能的将请求打到最大的机器
         */
        if (endpoints.get(2).reqNum.get() < limit) {
            endpoints.get(2).reqNum.getAndIncrement();
            return interList.get(2).get(index);
        } else if (endpoints.get(1).reqNum.get() < limit) {
            endpoints.get(1).reqNum.getAndIncrement();
            return interList.get(1).get(index);
        }
        endpoints.get(0).reqNum.getAndIncrement();
        return interList.get(0).get(index);


        /**
         * 三台机器按照4：3：1的比例进行轮询
         */
//        if (round < 4) {
//            if (endpoints.get(2).reqNum.get() < limit) {
//                endpoints.get(2).reqNum.getAndIncrement();
//                return interList.get(2).get(index);
//            } else if (endpoints.get(1).reqNum.get() < limit) {
//                endpoints.get(1).reqNum.getAndIncrement();
//                return interList.get(1).get(index);
//            }
//            endpoints.get(0).reqNum.getAndIncrement();
//            return interList.get(0).get(index);
//        } else if (round < 7) {
//            if (endpoints.get(1).reqNum.get() < limit) {
//                endpoints.get(1).reqNum.getAndIncrement();
//                return interList.get(1).get(index);
//            } else if (endpoints.get(2).reqNum.get() < limit) {
//                endpoints.get(2).reqNum.getAndIncrement();
//                return interList.get(2).get(index);
//            }
//            endpoints.get(0).reqNum.getAndIncrement();
//            return interList.get(0).get(index);
//        } else {
//            if (endpoints.get(0).reqNum.get() < limit) {
//                endpoints.get(0).reqNum.getAndIncrement();
//                return interList.get(0).get(index);
//            } else if (endpoints.get(2).reqNum.get() < limit) {
//                endpoints.get(2).reqNum.getAndIncrement();
//                return interList.get(2).get(index);
//            }
//            endpoints.get(1).reqNum.getAndIncrement();
//            return interList.get(1).get(index);
//        }

        /**
         * 按照200：200：112的比例进行请求
         * 不靠谱
         */
//        double r = Math.random();
//
//        if (r < up_gate) {
//            return interList.get(2).get(index);
//        } else if (r < medium_gate) {
//            return interList.get(1).get(index);
//        }
//        return interList.get(0).get(index);

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
