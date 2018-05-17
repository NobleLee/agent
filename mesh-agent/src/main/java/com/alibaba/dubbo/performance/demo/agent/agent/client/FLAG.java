package com.alibaba.dubbo.performance.demo.agent.agent.client;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 描述: 共享锁
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-05 下午6:20
 */
public final class FLAG {

    public static AtomicBoolean etcdLock = new AtomicBoolean(false);



}
