package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.DubboRequest;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcInvocation;
import com.alibaba.dubbo.performance.demo.agent.tools.JsonUtils;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
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


    private RpcClient() {
        this.connectManager = new ConnecManager("127.0.0.1", Integer.valueOf(System.getProperty("dubbo.protocol.port")),
                COMMON.DUBBO_CLIENT_THREAD, new ChannelInitializer<NioSocketChannel>() {
            ByteBuf delimiter = Unpooled.copyShort(COMMON.MAGIC);

            @Override
            protected void initChannel(NioSocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new DelimiterBasedFrameDecoder(2048, delimiter));
                pipeline.addLast(new DubboRpcEncoder());
                pipeline.addLast(new DubboRpcDecoder());
            }
        });
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
        Channel channel = null;
        try {
            channel = connectManager.getChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        channel.writeAndFlush(request);
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

}
