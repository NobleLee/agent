package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.agent.httpserver.HTTPServer;
import com.alibaba.dubbo.performance.demo.agent.agent.server.udp.AgentUdpServer;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AgentApp {
    private static Logger logger = LoggerFactory.getLogger(AgentApp.class);
    // agent会作为sidecar，部署在每一个Provider和Consumer机器上
    // 在Provider端启动agent时，添加JVM参数-Dtype=provider -Dserver.port=30000 -Ddubbo.protocol.port=20889
    // 在Consumer端启动agent时，添加JVM参数-Dtype=consumer -Dserver.port=20000
    // 添加日志保存目录: -Dlogs.dir=/path/to/your/logs/dir。请安装自己的环境来设置日志目录。


//    public static void main(String[] args) {
//
//        if (System.getProperty("type").equals("consumer")) {
//            // 开启监听etcd服务
//            EtcdRegistry.etcdFactory(System.getProperty("etcd.url")).leaseOrWatch(COMMON.ServiceName);
//            // 开启http服务
//            new HTTPServer().start(Integer.parseInt(System.getProperty("server.port")));
//        } else {
//            // 开启provider agent服务
//            AgentServerConnectPool agentServer = new AgentServerConnectPool(Integer.parseInt(System.getProperty("server.port"))).init();
//            // 注册服务
//            EtcdRegistry.etcdFactory(System.getProperty("etcd.url")).leaseOrWatch(COMMON.ServiceName);
//            // 监听事件关闭
//            agentServer.bindSync();
//        }
//    }

    public static void main(String[] args) {

        logger.info("start type: " + System.getProperty("type") + " " + System.getProperty("etcd.url"));
//        Properties properties = System.getProperties();
//        String ps = "";
//        Set<Map.Entry<Object, Object>> entries = properties.entrySet();
//        for (Map.Entry<Object, Object> entry : entries) {
//            ps += entry.getKey() + "=" + entry.getValue() + "\n";
//        }
//        logger.info(ps);
        // 开启监听etcd服务

        if (System.getProperty("type").equals("consumer")) {
            EtcdRegistry.etcdFactory(System.getProperty("etcd.url")).leaseOrWatch(COMMON.ServiceName);
            // 开启http服务
            new HTTPServer().start(Integer.parseInt(System.getProperty("server.port")));
        } else {
            // 开启provider agent服务
            logger.info("start provider .....");
            AgentUdpServer agentServer = new AgentUdpServer().bind(COMMON.DubboClient_Num).start();
            EtcdRegistry.etcdFactory(System.getProperty("etcd.url")).leaseOrWatch(COMMON.ServiceName);
            // 监听事件关闭
            logger.info("start provider sync.");
            agentServer.sync();
        }
    }
}
