package com.alibaba.dubbo.performance.demo.agent.registry;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
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

    public static EndpointHelper INSTANCE;

    public static final int COUNT = COMMON.HTTPSERVER_WORK_THREAD;

    /**
     * false -- sync
     * true  -- not sync
     */
    boolean sync;

    private EndpointHelper(boolean sync) {
        this.sync = sync;
    }

    public static synchronized EndpointHelper getINSTANCE(boolean sync) {
        if (INSTANCE == null) {
            INSTANCE = new EndpointHelper(sync);
        }
        return INSTANCE;
    }

    // 设置门限值
    private static final double up_gate = 0.38;
    private static final double medium_gate = 0.76;

    /**
     * 每台机器限制请求数量
     */
    private static final int limit = 190;

    /**
     * 存放所有的节点数目
     */
    private List<Endpoint> endpoints = new ArrayList<>();

    /**
     * 需要发送的address地址
     */
    private List<List<InetSocketAddress>> interList = new ArrayList<>();

    /**
     * 存储每个endpoint的请求数量
     */
    private volatile int[][] totalReqList = new int[COUNT][];

    int size = 0;

    public synchronized void addEndpointReq(int index) {
        totalReqList[index] = new int[3];
        size++;
    }

    public int[] rounds = new int[COUNT];

    /**
     * 给server增加一个endpoint
     *
     * @param endpoint
     */
    public void addEndpoint(Endpoint endpoint) {
        if (!endpoints.contains(endpoint)) {
            endpoints.add(endpoint);
            List<InetSocketAddress> singleHost = new ArrayList<>();
            for (Integer integer : endpoint.getPort()) {
                singleHost.add(new InetSocketAddress(endpoint.getHost(), integer));
            }
            interList.add(singleHost);
            logger.info("udp get endpoint: " + endpoint.toString());
        }
    }

    /**
     * response对维持的请求数目减1
     *
     * @param index
     * @param host
     */
    public void decrease(int index, String host) {
        if (sync) {
            for (Endpoint endpoint : endpoints) {
                if (endpoint.getHost().equals(host)) {
                    endpoint.reqNum.decrementAndGet();
                    break;
                }
            }
        } else {
            for (int i = 0; i < endpoints.size(); i++) {
                if (endpoints.get(i).getHost().equals(host)) {
                    totalReqList[index][i]--;
                    break;
                }
            }
        }
    }


    /**
     * 负载均衡算法，最好选择轮转算法，如果采用概率选择算法性能应该会受限
     *
     * @param index
     * @return
     */
    public InetSocketAddress getBalancePoint(final int index) {
        /**
         * 随机负载均衡
         */
//        int hostIndex = random.nextInt(interList.size());
//        return interList.get(hostIndex).get(index);


        /**
         * 尽可能的将请求打到最大的机器--乐观锁的形式
         */
        if (sync) {
            if (endpoints.get(2).reqNum.get() < limit) {
                endpoints.get(2).reqNum.getAndIncrement();
                return interList.get(2).get(index);
            } else if (endpoints.get(1).reqNum.get() < limit) {
                endpoints.get(1).reqNum.getAndIncrement();
                return interList.get(1).get(index);
            }
            endpoints.get(0).reqNum.getAndIncrement();
            return interList.get(0).get(index);
        } else {
            if (getEndpointReq(2) < limit) {
                totalReqList[index][2]++;
                return interList.get(2).get(index);
            } else if (getEndpointReq(1) < limit) {
                totalReqList[index][1]++;
                return interList.get(1).get(index);
            }
            totalReqList[index][0]++;
            return interList.get(0).get(index);
        }

        /**
         * 三台机器按照4：3：1的比例进行轮询
         */
//        if (rounds[index] < 4) {
//            roundControl(index);
//            if (endpoints.get(2).reqNum.get() < limit) {
//                endpoints.get(2).reqNum.getAndIncrement();
//                return interList.get(2).get(index);
//            } else if (endpoints.get(1).reqNum.get() < limit) {
//                endpoints.get(1).reqNum.getAndIncrement();
//                rounds[index]++;
//                return interList.get(1).get(index);
//            }
//            endpoints.get(0).reqNum.getAndIncrement();
//            return interList.get(0).get(index);
//        } else if (rounds[index] < 7) {
//            roundControl(index);
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
//            roundControl(index);
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

    }


    /**
     *  获取某一个endpoint的目前维持的请求数目
     *
     * @param index
     * @return
     */
    private int getEndpointReq(int index) {
        int res = 0;
        for (int i = 0; i < size; i++) {
            res += totalReqList[i][index];
        }
        return res;
    }

    private void roundControl(int index) {
        if (rounds[index] < 7)
            rounds[index]++;
        else
            rounds[index] = 0;
    }

}
