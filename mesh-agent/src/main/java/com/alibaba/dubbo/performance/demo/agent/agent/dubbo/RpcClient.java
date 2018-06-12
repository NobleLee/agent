package com.alibaba.dubbo.performance.demo.agent.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.agent.ConnecManager;
import com.alibaba.dubbo.performance.demo.agent.agent.dubbo.model.DubboRequest;
import com.alibaba.dubbo.performance.demo.agent.agent.dubbo.model.RpcInvocation;
import com.alibaba.dubbo.performance.demo.agent.agent.server.udp.ServerUdpHandler;
import com.alibaba.dubbo.performance.demo.agent.tools.ByteBufUtils;
import com.alibaba.dubbo.performance.demo.agent.tools.JsonUtils;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.FutureTask;

/**
 * 描述: Dubbo 和 agent 的调用RPCß
 * ${DESCRIPTION}
 */

public class RpcClient {
    private Logger logger = LoggerFactory.getLogger(RpcClient.class);


    private Channel channel;

    private ConnecManager connecManager;
    FutureTask<Channel> bind;

    private static RpcClient rpcClientInstance = new RpcClient();

    private RpcClient() {
        NioEventLoopGroup loop = new NioEventLoopGroup(1);
        connecManager = new ConnecManager(loop, DubboClientInitializer.class);
        bind = connecManager.bind("127.0.0.1", Integer.valueOf(System.getProperty("dubbo.protocol.port")));
    }

    public static RpcClient getInstance() {
        return rpcClientInstance;
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
    public void sendDubboDirect(ByteBuf buf, int index) {
        try {
            int id = buf.readInt();
            ByteBuf byteBuf = DubboRpcEncoder.directSend(buf, id,index);
            while (channel == null && bind.get() != null) {
                channel = bind.get();
            }
            this.channel.writeAndFlush(byteBuf);
        } catch (Exception e) {
            ByteBufUtils.println(buf, "agent server byte:");
            ByteBufUtils.printStringln(buf, "agent server str:");
            e.printStackTrace();
        }

    }

}
