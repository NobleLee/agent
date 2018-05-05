package com.alibaba.dubbo.performance.demo.agent.dubbo.model;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class RpcRequestHolder {

    private static HashMap<String, RpcRequestHolder> requestHolderMap = new HashMap<>();

    static {
        requestHolderMap.put("agentClient", new RpcRequestHolder());
        requestHolderMap.put("agentServer", new RpcRequestHolder());
        requestHolderMap.put("dubboClient", new RpcRequestHolder());
    }

    private RpcRequestHolder() {
    }

    public static RpcRequestHolder getRpcRequestHolderByName(String name) {
        return requestHolderMap.get(name);
    }


    // key: requestId     value: RpcFuture
    private ConcurrentHashMap<String, RpcFuture> processingRpc = new ConcurrentHashMap<>();

    public void put(String requestId, RpcFuture rpcFuture) {
        processingRpc.put(requestId, rpcFuture);
    }

    public RpcFuture get(String requestId) {
        return processingRpc.get(requestId);
    }

    public void remove(String requestId) {
        processingRpc.remove(requestId);
    }
}
