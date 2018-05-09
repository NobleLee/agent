package com.alibaba.dubbo.performance.demo.agent.agent.client;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.dubbo.ConnecManager;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcFuture;
import com.alibaba.dubbo.performance.demo.agent.tools.RpcRequestHolder;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EndpointHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

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

    private static RpcRequestHolder<Channel> requestHolder = RpcRequestHolder.getRpcRequestHolderByName("agentClient");

    private static AtomicLong requestId = new AtomicLong(1);

    private EndpointHelper endpointHelper = EndpointHelper.getInstance();

    public AgentClientConnectPool() {
        try {
            putServers(endpointHelper.getEndpoints());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 发送消息 还是有多个连接
    public void sendToServer(ByteBuf buf, Channel channel) throws Exception {
        try {

            // 将请求的id写入ByteBuf
            ByteBuf buffer = PooledByteBufAllocator.DEFAULT.directBuffer(buf.readableBytes() + 8);
            long id = requestId.getAndIncrement();
            // 写入消息头标志符
            buffer.writeShort(COMMON.MAGIC);
            // 写入请求id
            buffer.writeLong(id);
            // 写入消息体
            buffer.writeBytes(buf);
            // 因为是请求是HTTP连接，因此需要存储id的连接通道
            requestHolder.put(Long.valueOf(id), channel);
            // 根据负载均衡算法，选择一个节点发送数据
            channelMap.get(endpointHelper.getBalancePoint()).writeAndFlush(buffer);
        } finally {
            buf.release();
        }
    }


    // 对每一个endpoint节点都创建一个通道
    private boolean putServers(List<Endpoint> endpoints) throws Exception {
        for (Endpoint endpoint : endpoints) {
            ConnecManager connecManager = new ConnecManager(endpoint.getHost(), endpoint.getPort(), 4,
                    new ChannelInitializer<SocketChannel>() {
                        ByteBuf delimiter = Unpooled.copyShort(COMMON.MAGIC);

                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new DelimiterBasedFrameDecoder(2048, delimiter));
                            pipeline.addLast(new AgentClientResponseDecoder());
                        }
                    });  // 创建单个服务器的连接通道
            channelMap.put(endpoint, connecManager.getChannel());
        }
        return true;
    }

}
