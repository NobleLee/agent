package com.alibaba.dubbo.performance.demo.agent.agent.client;

import com.alibaba.dubbo.performance.demo.agent.dubbo.ConnecManager;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.springframework.stereotype.Component;

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
public class AgentClientConnectPool {

    private HashMap<Endpoint, Channel> channelMap = new HashMap<>();

    public AgentClientConnectPool() {

    }


    // 发送消息
    public void sendToServer(Endpoint server, String msg) throws Exception {
        while (FLAG.etcdLock.get()) ;
        channelMap.get(server).writeAndFlush(msg);
    }

    public boolean putServers(List<Endpoint> endpoints) throws Exception {
        for (Endpoint endpoint : endpoints) {
            ConnecManager connecManager = new ConnecManager(endpoint.getHost(), endpoint.getPort(), 4,
                    new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new AgentClientRpcHandler());
                        }
                    });  // 创建单个服务器的连接通道
            channelMap.put(endpoint, connecManager.getChannel());
        }
        return true;
    }


}
