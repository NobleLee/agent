package com.alibaba.dubbo.performance.demo.agent.provider.dubbo;

import com.alibaba.dubbo.performance.demo.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.consumer.ConnecManager;
import com.alibaba.dubbo.performance.demo.agent.provider.dubbo.model.DubboRequest;
import com.alibaba.dubbo.performance.demo.agent.provider.dubbo.model.RpcInvocation;
import com.alibaba.dubbo.performance.demo.agent.provider.server.tcp.ServerTcpHandler;
import com.alibaba.dubbo.performance.demo.agent.provider.server.udp.ServerUdpHandler;
import com.alibaba.dubbo.performance.demo.agent.tools.ByteBufUtils;
import com.alibaba.dubbo.performance.demo.agent.tools.JsonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.FutureTask;

/**
 * 描述: Dubbo 和 consumer 的调用RPCß
 * ${DESCRIPTION}
 */

public class DubboClient {
    private Logger logger = LoggerFactory.getLogger(DubboClient.class);

    private static final int HEADER_LENGTH = 16;

    private final ByteBuf header = PooledByteBufAllocator.DEFAULT.directBuffer(HEADER_LENGTH);

    private Channel channel;

    FutureTask<Channel> bind;

    public DubboClient(EventLoop loop, ServerUdpHandler udpHandler) {
        ConnecManager connecManager = new ConnecManager(loop, udpHandler, DubboClientInitializer.class);
        bind = connecManager.bind("127.0.0.1", Integer.valueOf(System.getProperty("dubbo.protocol.port")));

        header.writeByte(-38);
        header.writeByte(-69);
        header.writeByte(-58);
        header.writeByte(0);
        header.writeLong(0);
        header.writeInt(0);
    }

    public DubboClient(EventLoop loop, ServerTcpHandler tcpHandler) {
        ConnecManager connecManager = new ConnecManager(loop, tcpHandler, DubboClientInitializer.class);
        bind = connecManager.bind("127.0.0.1", Integer.valueOf(System.getProperty("dubbo.protocol.port")));

        header.writeByte(-38);
        header.writeByte(-69);
        header.writeByte(-58);
        header.writeByte(0);
        header.writeLong(0);
        header.writeInt(0);
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
            //ByteBuf byteBuf = DubboRpcEncoder.directSend(buf, header);
            while (channel == null && bind.get() != null) {
                channel = bind.get();
            }
            buf.retain();
            this.channel.writeAndFlush(buf);
        } catch (Exception e) {
            ByteBufUtils.println(buf, "consumer server byte:");
            ByteBufUtils.printStringln(buf, "consumer server str:");
            e.printStackTrace();
        }

    }

}
