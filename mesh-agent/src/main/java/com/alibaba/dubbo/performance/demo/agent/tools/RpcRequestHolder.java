package com.alibaba.dubbo.performance.demo.agent.tools;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class RpcRequestHolder<T> {

    private static HashMap<String, RpcRequestHolder> requestHolderMap = new HashMap<>();

    static {
        requestHolderMap.put("agentClient", new RpcRequestHolder<Channel>());
        requestHolderMap.put("agentServer", new RpcRequestHolder<RpcFuture>());
        requestHolderMap.put("dubboClient", new RpcRequestHolder<RpcFuture>());


        requestHolderMap.put("agentServerResponse",new RpcRequestHolder<ChannelHandlerContext>());
    }

    private RpcRequestHolder() {
    }

    public static RpcRequestHolder getRpcRequestHolderByName(String name) {
        return requestHolderMap.get(name);
    }


    // key: requestId     value: RpcFuture
    private ConcurrentHashMap<Long, T> processingRpc = new ConcurrentHashMap<>();

    public void put(Long requestId, T rpcFuture) {
        processingRpc.put(requestId, rpcFuture);
    }

    public T get(Long requestId) {
        return processingRpc.get(requestId);
    }

    public void remove(Long requestId) {
        processingRpc.remove(requestId);
    }
}
