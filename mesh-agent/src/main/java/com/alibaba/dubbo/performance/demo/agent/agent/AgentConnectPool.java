package com.alibaba.dubbo.performance.demo.agent.agent;

import com.alibaba.dubbo.performance.demo.agent.dubbo.ConnecManager;
import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClientHandler;
import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClientInitializer;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.List;

/**
 * 描述: 用于管理agent之间的连接
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-05 下午5:41
 */
@Component
public class AgentConnectPool {

    private Bootstrap bootstrap;

    private HashMap<Endpoint, Channel> channelMap = new HashMap<>();

    public AgentConnectPool() {

    }

    public Channel getChannel(String server) throws Exception {

        while (FLAG.etcdLock.get()) ;
        return channelMap.get(server);
    }

    public boolean putServers(List<Endpoint> endpoints) throws Exception {
        for (Endpoint endpoint : endpoints) {
            ConnecManager connecManager = new ConnecManager(endpoint.getHost(), endpoint.getPort(), 4,
                    new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new AgentRpcHandler());
                        }
                    });  // 创建单个服务器的连接通道
            channelMap.put(endpoint, connecManager.getChannel());
        }
        return true;
    }


}
