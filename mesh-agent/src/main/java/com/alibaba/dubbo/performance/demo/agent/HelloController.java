package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.agent.client.AgentClientConnectPool;
import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerConnectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HelloController {

    private Logger logger = LoggerFactory.getLogger(HelloController.class);


    public HelloController() {
        init();
    }

    private AgentServerConnectPool agentServerConnectPool;

    private AgentClientConnectPool agentClientConnectPool;

//    @RequestMapping(value = "")
//    public Object invoke(@RequestParam("interface") String interfaceName,
//                         @RequestParam("method") String method,
//                         @RequestParam("parameterTypesString") String parameterTypesString,
//                         @RequestParam("parameter") String parameter) throws Exception {
//        String type = System.getProperty("type");   // 获取type参数
//        if ("consumer".equals(type)) {
//            return agentClientConnectPool.sendToServer(new AgentClientRequest(interfaceName, method, parameterTypesString, parameter));
//        } else {
//            return "Environment variable type is needed to set to provider or consumer.";
//        }
//    }


    void init() {
        if (System.getProperty("type").equals("consumer"))
            agentClientConnectPool = new AgentClientConnectPool();
        else
            agentServerConnectPool = new AgentServerConnectPool();
    }


}
