package com.alibaba.dubbo.performance.demo.agent.agent.client;

import com.alibaba.dubbo.performance.demo.agent.dubbo.ConnecManager;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcFuture;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcRequestHolder;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EndpointHelper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;

import java.util.HashMap;
import java.util.List;

/**
 * 描述: 用于管理agent之间的连接
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-05 下午5:41
 */
public class AgentClientConnectPool {

    // key 节点 value 通道
    private HashMap<Endpoint, Channel> channelMap = new HashMap<>();

    private RpcRequestHolder requestHolder = RpcRequestHolder.getRpcRequestHolderByName("agentClient");

    private EndpointHelper endpointHelper = EndpointHelper.getInstance();

    public AgentClientConnectPool() {
        try {
            putServers(endpointHelper.getEndpoints());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 发送消息 还是有多个连接
    public Object sendToServer(Endpoint server, AgentClientRequest request) {
        while (FLAG.etcdLock.get()) ;

        RpcFuture future = new RpcFuture();
        // 因为是请求是HTTP连接，因此需要存储id的连接请求
        requestHolder.put(String.valueOf(request.getId()), future);
        channelMap.get(server).writeAndFlush(request.getBuyteBuff());

        Object result = null;
        try {
            result = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    // 对每一个endpoint节点都创建一个通道
    public boolean putServers(List<Endpoint> endpoints) throws Exception {
        for (Endpoint endpoint : endpoints) {
            ConnecManager connecManager = new ConnecManager(endpoint.getHost(), endpoint.getPort(), 4,
                    new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LineBasedFrameDecoder(1024));
                            pipeline.addLast(new AgentClientResponseDecoder());
                            pipeline.addLast(new AgentClientRpcHandler());
                        }
                    });  // 创建单个服务器的连接通道
            channelMap.put(endpoint, connecManager.getChannel());
        }
        return true;
    }

}
