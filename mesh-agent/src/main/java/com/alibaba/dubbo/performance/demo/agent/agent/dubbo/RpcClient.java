package com.alibaba.dubbo.performance.demo.agent.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.agent.ConnecManager;
import com.alibaba.dubbo.performance.demo.agent.agent.dubbo.model.DubboRequest;
import com.alibaba.dubbo.performance.demo.agent.agent.dubbo.model.RpcInvocation;
import com.alibaba.dubbo.performance.demo.agent.agent.server.AgentServerRpcHandler;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.tools.ByteBufUtils;
import com.alibaba.dubbo.performance.demo.agent.tools.JsonUtils;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 描述: Dubbo 和 agent 的调用RPCß
 * ${DESCRIPTION}
 */

public class RpcClient {
    private Logger logger = LoggerFactory.getLogger(RpcClient.class);


    private Channel channel;

    private ConnecManager connecManager;

    private RpcClient() {
        connecManager = new ConnecManager(COMMON.DubboClient_THREAD, DubboClientInitializer.class);
        Endpoint endpoint = new Endpoint("127.0.0.1", Integer.valueOf(System.getProperty("dubbo.protocol.port")));
        channel = connecManager.bind(endpoint);
    }

    private static RpcClient instance;

    public static RpcClient getInstance() {
        if (instance == null) {
            synchronized (RpcClient.class) {
                if (instance == null) {
                    instance = new RpcClient();
                }
            }
        }
        return instance;
    }


    // 将数据封装之后发送给dubbo
    public void sendDubbo(ByteBuf buf) {
        // 前8个字节是请求id
        long id = buf.readLong();
        String bodyString = buf.toString(Charsets.UTF_8);
//        // 获取哈希参数
//        String parameter = bodyString.substring(bodyString.indexOf("&parameter=") + 11);
        // 封装请求对象
        DubboRequest dubboRequest = getDubboRequest(bodyString, id);
        // 发送数据
        send(dubboRequest);
    }

    public void send(DubboRequest request) {
        this.channel.writeAndFlush(request);
    }

    // 获取Dubbo请求数据
    private DubboRequest getDubboRequest(String parameter, long id) {

        RpcInvocation invocation = new RpcInvocation();
        invocation.setAttachment("path", COMMON.Request.interfacename);
        invocation.setMethodName(COMMON.Request.method);
        invocation.setParameterTypes(COMMON.Request.parameterTypesString);    // Dubbo内部用"Ljava/lang/String"来表示参数类型是String

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        try {
            JsonUtils.writeObject(parameter, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        invocation.setArguments(out.toByteArray());

        DubboRequest request = new DubboRequest(id);
        request.setVersion("2.0.0");
        request.setTwoWay(true);
        request.setData(invocation);
        return request;
    }

    /**
     *  直接将请求发送Dubbo
     *
     * @param buf
     */
    public void sendDubboDirect(ByteBuf buf) {
        try {
            ByteBuf byteBuf = DubboRpcEncoder.directSend(buf);
            this.channel.writeAndFlush(byteBuf);
        } catch (Exception e) {
            ByteBufUtils.println(buf, "agent server byte:");
            ByteBufUtils.printStringln(buf, "agent server str:");
            e.printStackTrace();
        }

    }


    /**
     * 测试直接将结果返回
     *
     * @param buf
     * @param channel
     */
    private ScheduledExecutorService executorService;

    public void sendBackTest(ByteBuf buf) {
        if (executorService == null) {
            synchronized (this) {
                if (executorService == null)
                    executorService = Executors.newScheduledThreadPool(256);
            }
        }


        long reqid = buf.readLong();
        byte[] hashcode = String.valueOf(buf.toString(Charsets.UTF_8).hashCode()).getBytes();
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.directBuffer(hashcode.length + 8);
        buffer.writeLong(reqid);
        buffer.writeBytes(hashcode);
        executorService.schedule(() -> {
            int index = (int) (reqid & 0x7);
            buffer.writeShort(COMMON.MAGIC);
            AgentServerRpcHandler.channel.writeAndFlush(buffer);
        }, 50, TimeUnit.MILLISECONDS);
    }

}
