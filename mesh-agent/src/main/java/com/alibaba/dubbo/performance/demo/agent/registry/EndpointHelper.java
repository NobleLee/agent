package com.alibaba.dubbo.performance.demo.agent.registry;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.exception.NotFindServiceException;
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

    private static IRegistry registry = EtcdRegistry.etcdFactory(System.getProperty("etcd.url"));

    private static EndpointHelper instance;

    public static double[] weights;

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
        try {
            getBalancePoint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 对节点设置watcher监控rpc节点的变更
    private void watch() {
    }


    // 采用负载均衡算法对结果进行处理
    public Endpoint getBalancePoint() throws Exception {
        if (endpoints == null) {
            synchronized (EndpointHelper.class) {
                if (endpoints == null) {
                    endpoints = registry.find(COMMON.ServiceName);
                    getWeights();
                }
            }
        }

        return getBlancePoint();
    }

    // 负载均衡算法，最好选择轮转算法，如果采用概率选择算法性能应该会受限
    private Endpoint getBlancePoint() {
        if (endpoints.size() == 1)
            return endpoints.get(0);
        else {
            double random = Math.random();
            for (int i = 0; i < weights.length; i++) {
                if (random < weights[i]) return endpoints.get(i);
            }
            return endpoints.get(weights.length - 1);
        }
    }


    public List<Endpoint> getEndpoints() {
        return endpoints;
    }


    // 获取概率分配请求函数
    private void getWeights() {
        if (endpoints.size() == 0) throw new NotFindServiceException();
        weights = new double[endpoints.size()];
        if (weights.length < 2) weights[0] = 1;
        else {
            double sum = 1;
            // 更新权重大小
            long refer = endpoints.get(0).getMemary();
            for (int i = 1; i < weights.length; i++) {
                double tmp = endpoints.get(i).getMemary() * 1.0 / refer;
                weights[i] = 2 - Math.exp(-tmp * 8);
                sum += weights[i];
            }
            for (int i = 0; i < weights.length; i++) {
                weights[i] /= sum;
            }
            for (int i = 1; i < weights.length; i++) {
                weights[i] += weights[i - 1];
            }
        }
    }

}
