package com.alibaba.dubbo.performance.demo.agent.registry;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.agent.client.AgentClientConnectPool;
import com.alibaba.dubbo.performance.demo.agent.agent.client.udp.AgentUdpClient;
import com.alibaba.dubbo.performance.demo.agent.agent.server.udp.AgentUdpServer;
import com.alibaba.dubbo.performance.demo.agent.agent.server.udp.ServerUdpHandler;
import com.alibaba.dubbo.performance.demo.agent.tools.IpHelper;
import com.alibaba.dubbo.performance.demo.agent.tools.LOCK;
import com.coreos.jetcd.Client;
import com.coreos.jetcd.KV;
import com.coreos.jetcd.Lease;
import com.coreos.jetcd.Watch;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.options.GetOption;
import com.coreos.jetcd.options.PutOption;
import com.coreos.jetcd.options.WatchOption;
import com.coreos.jetcd.watch.WatchEvent;
import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class EtcdRegistry implements IRegistry {
    private static Logger logger = LoggerFactory.getLogger(EtcdRegistry.class);
    // 该EtcdRegistry没有使用etcd的Watch机制来监听etcd的事件
    // 添加watch，在本地内存缓存地址列表，可减少网络调用的次数
    // 使用的是简单的随机负载均衡，如果provider性能不一致，随机策略会影响性能
    private final String rootPath = "dubbomesh";
    private Client client;
    private Lease lease;
    private KV kv;
    private long leaseId;

    // key 注册地址 value 返回对象
    private static ConcurrentHashMap<String, EtcdRegistry> etcdRegistryMap = new ConcurrentHashMap<String, EtcdRegistry>();

    // etcd factory build etcdRegistry
    public static EtcdRegistry etcdFactory(String registryAddress) {
        if (!etcdRegistryMap.containsKey(registryAddress)) {
            synchronized (EtcdRegistry.class) {
                if (!etcdRegistryMap.containsKey(registryAddress)) {
                    EtcdRegistry temp = new EtcdRegistry(registryAddress);
                    etcdRegistryMap.put(registryAddress, temp);
                }
            }
        } else {
            logger.info("exist registryAddress :" + registryAddress);
        }
        return etcdRegistryMap.get(registryAddress);
    }

    // 获取监听内容
    private EtcdRegistry(String registryAddress) {
        logger.info("constructor EtcdRegistry " + registryAddress);
        this.client = Client.builder().endpoints(registryAddress).build();
        this.kv = client.getKVClient();
    }


    public void leaseOrWatch(String service) {
        String type = System.getProperty("type");   // 获取type参数
        logger.info("start type: " + type);
        if ("provider".equals(type)) {
            keepAlive();  // 对于consumer的agent，并不需要进行租期续约
        } else {
            // 监听key过期和服务
            watch(MessageFormat.format("/{0}/{1}", rootPath, service));
        }
    }


    // 向ETCD中注册服务
    public void register(String serviceName, List<Integer> ports) throws Exception {
        // 服务注册的key为:    /dubbomesh/com.some.package.IHelloService/192.168.100.100:2000
        logger.info("try to register a new endpoint.....");
        String portstr = "";
        for (Integer port : ports) {
            portstr += ":" + port;
        }
        portstr = portstr.substring(1);

        String strKey = MessageFormat.format("/{0}/{1}/{2}:{3}", rootPath, serviceName, IpHelper.getHostIp(), portstr);
        ByteSequence key = ByteSequence.fromString(strKey);
        ByteSequence val = ByteSequence.fromString("");     // 目前只需要创建这个key,对应的value暂不使用,先留空
        kv.put(key, val, PutOption.newBuilder().withLeaseId(leaseId).build()).get();
        logger.info("Register a new service at:" + strKey);
    }

    // 监控key的变化
    public void watch(String watchkey) {
        Watch watchClient = client.getWatchClient();
        Watch.Watcher watch = watchClient.watch(ByteSequence.fromString(watchkey), WatchOption.newBuilder().withPrefix(ByteSequence.fromString(watchkey)).build());
        // 启动时首先进行服务发现，然后再进行监控
        find(COMMON.ServiceName);
        // 对服务进行监听
        new Thread(() -> {
            while (true) {
                try {
                    logger.info("watching etcd service ....");
                    for (WatchEvent event : watch.listen().getEvents()) {
                        KeyValue kv = event.getKeyValue();
                        logger.info("get etcd change message, action:" + event.getEventType() + " key: " + kv.getKey().toStringUtf8() + " value" + kv.getValue().toStringUtf8());
                        // 如果是删除操作
                        if (WatchEvent.EventType.DELETE.equals(event.getEventType())) {
                            String key = kv.getKey().toStringUtf8();
                            List<Endpoint> endpoints = Lists.newArrayList(getEndpointFromStr(key));
                            // TCP
                            AgentClientConnectPool.deleteServers(endpoints);
                            // UDP
                            AgentUdpClient.deleteServers(endpoints);
                            logger.info("delete complete!");
                        } else if (WatchEvent.EventType.PUT.equals(event.getEventType())) {
                            // 如果是加入操作
                            String key = kv.getKey().toStringUtf8();
                            List<Endpoint> endpoints = Lists.newArrayList(getEndpointFromStr(key));
                            // TCP
                            AgentClientConnectPool.putServers(endpoints);
                            // UDP
                            AgentUdpClient.putServers(endpoints);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        ).start();
    }

    // 发送心跳到ETCD,表明该host是活着的
    public void keepAlive() {
        this.lease = client.getLeaseClient();
        try {
            this.leaseId = lease.grant(30).get().getID();
            // 如果是provider，去etcd注册服务
//            int port = Integer.valueOf(System.getProperty("server.port"));
            register(COMMON.ServiceName, AgentUdpServer.portList);
        } catch (Exception e) {
            logger.error(e.getStackTrace().toString());
        }
        Executors.newSingleThreadExecutor().submit(
                () -> {
                    try {
                        Lease.KeepAliveListener listener = lease.keepAlive(leaseId);
                        listener.listen();
                        logger.info("KeepAlive lease:" + leaseId + "; Hex format:" + Long.toHexString(leaseId));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }


    // 发现查找服务
    public void find(String serviceName) {
        try {
            String strKey = MessageFormat.format("/{0}/{1}", rootPath, serviceName);
            ByteSequence key = ByteSequence.fromString(strKey);
            GetResponse response = kv.get(key, GetOption.newBuilder().withPrefix(key).build()).get();
            List<Endpoint> endpoints = new ArrayList<>();
            for (com.coreos.jetcd.data.KeyValue kv : response.getKvs()) {
                endpoints.add(getEndpointFromStr(kv.getKey().toStringUtf8()));
            }
            logger.info("获得注册路径：" + Arrays.toString(endpoints.toArray()));
            AgentClientConnectPool.putServers(endpoints);
            AgentUdpClient.putServers(endpoints);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *   从字符串构造Endpoint
     *
     * @param key
     * @return
     */
    private Endpoint getEndpointFromStr(String key) {
        int index = key.lastIndexOf("/");
        String endpointStr = key.substring(index + 1, key.length());

        String[] eps = endpointStr.split(":");
        String host = eps[0];
        List<Integer> ports = new ArrayList<>();
        for (int i = 1; i < eps.length; i++) {
            ports.add(Integer.parseInt(eps[i]));
        }
        int port = Integer.valueOf(endpointStr.split(":")[1]);
        return new Endpoint(host, ports);
    }
}
