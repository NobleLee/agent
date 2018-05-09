package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.agent.client.AgentClientConnectPool;
import com.alibaba.dubbo.performance.demo.agent.agent.httpserver.HTTPServer;
import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerConnectPool;
import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerRpcHandler;
import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.registry.EndpointHelper;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


public class AgentApp {
    // agent会作为sidecar，部署在每一个Provider和Consumer机器上
    // 在Provider端启动agent时，添加JVM参数-Dtype=provider -Dserver.port=30000 -Ddubbo.protocol.port=20889
    // 在Consumer端启动agent时，添加JVM参数-Dtype=consumer -Dserver.port=20000
    // 添加日志保存目录: -Dlogs.dir=/path/to/your/logs/dir。请安装自己的环境来设置日志目录。

    //    public static void main(String[] args) {
//        SpringApplication.run(AgentApp.class,args);
//    }

    public static void main(String[] args) {

        if (System.getProperty("type").equals("consumer")) {
            EndpointHelper.getInstance();
            new HTTPServer().start(Integer.parseInt(System.getProperty("server.port")));
        } else {
            new AgentServerConnectPool(Integer.parseInt(System.getProperty("server.port"))).init();
            EtcdRegistry.etcdFactory(System.getProperty("etcd.url"));
        }
    }
}
