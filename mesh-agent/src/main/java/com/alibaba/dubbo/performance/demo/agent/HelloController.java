package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.agent.client.AgentClientConnectPool;
import com.alibaba.dubbo.performance.demo.agent.agent.client.AgentClientRequest;
import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerConnectPool;
import com.alibaba.dubbo.performance.demo.agent.registry.EndpointHelper;
import com.alibaba.fastjson.JSON;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;

@RestController
public class HelloController {

    private Logger logger = LoggerFactory.getLogger(HelloController.class);


    // 生成这个对象的时候，已经产生获取了服务注册地址
    private EndpointHelper endpointHelper = EndpointHelper.getInstance();

    public HelloController() {
        init();
    }

    private AgentServerConnectPool agentServerConnectPool;

    private AgentClientConnectPool agentClientConnectPool;

    @RequestMapping(value = "")
    public Object invoke(@RequestParam("interface") String interfaceName,
                         @RequestParam("method") String method,
                         @RequestParam("parameterTypesString") String parameterTypesString,
                         @RequestParam("parameter") String parameter) throws Exception {
        String type = System.getProperty("type");   // 获取type参数
        if ("consumer".equals(type)) {

            System.err.println(interfaceName);
            System.err.println(method);
            System.err.println(parameterTypesString);
            System.err.println(parameter);

            return agentClientConnectPool.sendToServer(endpointHelper.getBalancePoint(),
                    new AgentClientRequest(interfaceName, method, parameterTypesString, parameter));
        } else {
            return "Environment variable type is needed to set to provider or consumer.";
        }
    }


    void init() {
        if (System.getProperty("type").equals("consumer"))
            agentClientConnectPool = new AgentClientConnectPool();
        else
            agentServerConnectPool = new AgentServerConnectPool();
    }



}
