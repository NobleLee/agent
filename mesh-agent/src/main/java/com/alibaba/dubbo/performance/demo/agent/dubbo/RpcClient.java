package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.DubboRequest;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.JsonUtils;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcInvocation;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * 描述: Dubbo 和 agent 的调用RPCß
 * ${DESCRIPTION}
 */

public class RpcClient {
    private Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private ConnecManager connectManager;


    public RpcClient() {
        this.connectManager = new ConnecManager("127.0.0.1", Integer.valueOf(System.getProperty("dubbo.protocol.port")),
                4, new RpcClientInitializer());
    }

    public void invoke(String[] msgs) {

        Channel channel = null;
        try {
            channel = connectManager.getChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DubboRequest request = getDubboRequest(msgs);
        channel.writeAndFlush(request);

    }

    // 获取Dubbo请求数据
    private DubboRequest getDubboRequest(String[] msgs) {
        if (msgs == null || msgs.length != 5) {
            return new DubboRequest(0);
        }

        RpcInvocation invocation = new RpcInvocation();

        invocation.setAttachment("path", msgs[1]);
        invocation.setMethodName(msgs[2]);
        invocation.setParameterTypes(msgs[3]);    // Dubbo内部用"Ljava/lang/String"来表示参数类型是String

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        try {
            JsonUtils.writeObject(msgs[4], writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        invocation.setArguments(out.toByteArray());

        DubboRequest request = new DubboRequest(Long.parseLong(msgs[0]));
        request.setVersion("2.0.0");
        request.setTwoWay(true);
        request.setData(invocation);

        return request;

    }

}
