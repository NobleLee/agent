package com.alibaba.dubbo.performance.demo.agent.agent.server;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Arrays;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-05 下午8:44
 */
public class AgentServerRpcHandler extends SimpleChannelInboundHandler<String> {

    private IRegistry registry = EtcdRegistry.etcdFactory(System.getProperty("etcd.url"));

    private RpcClient rpcClient = new RpcClient(registry);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        // TCP 拆包问题
        String[] msgs = msg.split(COMMON.AttributeSeparator);

        System.out.println(Arrays.toString(msgs));
    }

}
