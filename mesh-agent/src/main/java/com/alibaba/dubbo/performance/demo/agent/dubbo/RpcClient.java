package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.*;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * 描述: Dubbo 和 agent 的调用RPCß
 * ${DESCRIPTION}
 */
public class RpcClient {
    private Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private ConnecManager connectManager;

    private RpcRequestHolder requestHolder = RpcRequestHolder.getRpcRequestHolderByName("dubboClient");

    public RpcClient(IRegistry registry) {
        this.connectManager = new ConnecManager("127.0.0.1", Integer.valueOf(System.getProperty("dubbo.protocol.port")),
                4, new RpcClientInitializer());
    }

    public Object invoke(String interfaceName, String method, String parameterTypesString, String parameter) throws Exception {

        Channel channel = connectManager.getChannel();

        RpcInvocation invocation = new RpcInvocation();
        invocation.setMethodName(method);
        invocation.setAttachment("path", interfaceName);
        invocation.setParameterTypes(parameterTypesString);    // Dubbo内部用"Ljava/lang/String"来表示参数类型是String

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        JsonUtils.writeObject(parameter, writer);
        invocation.setArguments(out.toByteArray());

        DubboRequest request = new DubboRequest();
        request.setVersion("2.0.0");
        request.setTwoWay(true);
        request.setData(invocation);

        logger.info("requestId=" + request.getId());

        RpcFuture future = new RpcFuture();
        requestHolder.put(String.valueOf(request.getId()), future);

        channel.writeAndFlush(request);

        Object result = null;
        try {
            result = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
