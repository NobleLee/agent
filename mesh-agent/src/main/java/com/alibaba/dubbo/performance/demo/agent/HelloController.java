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

    private OkHttpClient httpClient = new OkHttpClient();

    private AgentServerConnectPool agentServerConnectPool = new AgentServerConnectPool();

    private AgentClientConnectPool agentClientConnectPool = new AgentClientConnectPool();

    @RequestMapping(value = "")
    public Object invoke(@RequestParam("interface") String interfaceName,
                         @RequestParam("method") String method,
                         @RequestParam("parameterTypesString") String parameterTypesString,
                         @RequestParam("parameter") String parameter) throws Exception {
        logger.info(interfaceName + method + parameterTypesString + parameter);
        String type = System.getProperty("type");   // 获取type参数
        if ("consumer".equals(type)) {
            return agentClientConnectPool.sendToServer(endpointHelper.getBalancePoint(),
                    new AgentClientRequest(interfaceName, method, parameterTypesString, parameter));
        } else {
            return "Environment variable type is needed to set to provider or consumer.";
        }
    }


//    public Integer consumer(String interfaceName, String method, String parameterTypesString, String parameter) throws Exception {
//
//        String url = endpointHelper.getBalancePointUrl();
//
//        logger.info("chose server: " + url + "all host: " + Arrays.toString(endpointHelper.getEndpoints().toArray()));
//
//
//        RequestBody requestBody = new FormBody.Builder()
//                .add("interface", interfaceName)
//                .add("method", method)
//                .add("parameterTypesString", parameterTypesString)
//                .add("parameter", parameter)
//                .build();
//
//        Request request = new Request.Builder()
//                .url(url)
//                .post(requestBody)
//                .build();
//
//        try (Response response = httpClient.newCall(request).execute()) {
//            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//            byte[] bytes = response.body().bytes();
//            return JSON.parseObject(bytes, Integer.class);
//        }
//    }


}
