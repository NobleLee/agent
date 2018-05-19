package com.alibaba.dubbo.performance.demo.agent.agent.client;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.dubbo.ConnecManager;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EndpointHelper;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.tools.ByteBufUtils;
import com.alibaba.dubbo.performance.demo.agent.tools.LOCK;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

/**
 * 描述: 用于管理agent之间的连接
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-05 下午5:41
 */
public class AgentClientConnectPool {

    private static Logger logger = LoggerFactory.getLogger(AgentClientConnectPool.class);

    //public static ConcurrentHashMap<Long, Channel> requestHolderMap = new ConcurrentHashMap<>();
    public static List<ConcurrentHashMap<Long, Channel>> requestList = new ArrayList<>(COMMON.HTTPSERVER_WORK_THREAD);

    static {
        for (int i = 0; i < COMMON.HTTPSERVER_WORK_THREAD; i++) {
            requestList.add(new ConcurrentHashMap<>());
        }
    }

    // key 节点 value 通道
    private static HashMap<Endpoint, Channel> channelMap = new HashMap<>();
    // 连接节点数
    private static List<Endpoint> endpoints = new ArrayList<>(); //之后可以考虑把初始化的过程放到前面
    // 附带请求id
    private static AtomicLong requestId = new AtomicLong(1);

    private static AgentClientConnectPool instance;

    public static AgentClientConnectPool getInstance() {
        if (instance == null) {
            synchronized (AgentClientConnectPool.class) {
                if (instance == null) {
                    instance = new AgentClientConnectPool();
                }
            }
        }
        return instance;
    }

    Random r = new Random(1);


    private AgentClientConnectPool() {
    }

    /**
     * 生成请求header，将http链接放到map中
     *
     * @param buf
     * @param channel
     * @throws Exception
     */
    public void sendToServer(ByteBuf buf, Channel channel) throws Exception {
        //  ByteBufUtils.printStringln(buf, "");
        if (buf.readableBytes() < 136) return;
        // 将请求的id写入ByteBuf
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.directBuffer(buf.readableBytes() - 126);

        // long id = requestId.getAndIncrement();
        long id = System.currentTimeMillis() << 32 | r.nextInt(Integer.MAX_VALUE);
        // 写入消息头标志符
        buffer.writeShort(COMMON.MAGIC);
        // 写入请求id
        buffer.writeLong(id);
        // 跳过前面的固定字符串
        buf.skipBytes(136);
        // 写入消息体
        buffer.writeBytes(buf);
        // 因为是请求是HTTP连接，因此需要存储id的连接通道
        // TODO 更改成数组 hashmap 避免锁竞争
        // requestHolderMap.put(Long.valueOf(id), channel);
        requestList.get((int) id % COMMON.HTTPSERVER_WORK_THREAD).put(Long.valueOf(id), channel);
        // 根据负载均衡算法，选择一个节点发送数据
        // TODO 没有考虑ChanelMap的线程安全问题；假设在服务过程中没有新的服务的注册问题
        channelMap.get(EndpointHelper.getBalancePoint(endpoints)).writeAndFlush(buffer);
    }

    /**
     * 将接收到的agent response返回
     *
     * @param buf
     */
    public void response(ByteBuf buf) {
        // 获取请求id
        long requestId = buf.readLong();
        // 封装返回response
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        response.content().writeBytes(buf);
        // 发送请求数据
        // ChannelFuture channelFuture = requestHolderMap.remove(requestId).writeAndFlush(response);
        Channel remove = requestList.get((int) requestId % COMMON.HTTPSERVER_WORK_THREAD).
                remove(requestId);
        if (remove.isActive()) {
            ChannelFuture channelFuture = remove.writeAndFlush(response);
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }


    }


    /**
     * 对每一个endpoint节点都创建一个通道
     *
     * @param endpoints
     * @return
     * @throws Exception
     */
    public static boolean putServers(List<Endpoint> endpoints) {
        // TODO  应该加锁
        LOCK.AgentChannelLock = true;
        for (Endpoint endpoint : endpoints) {
            if (!channelMap.containsKey(endpoint)) {
                logger.info("prepare connect server：" + endpoint.toString());
                ConnecManager connecManager = new ConnecManager(endpoint.getHost(), endpoint.getPort(), COMMON.AGENT_CLIENT_THREAD,
                        new ChannelInitializer<NioSocketChannel>() {
                            ByteBuf delimiter = Unpooled.copyShort(COMMON.MAGIC);

                            @Override
                            protected void initChannel(NioSocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new DelimiterBasedFrameDecoder(2048, delimiter));
                                pipeline.addLast(new AgentClientResponseDecoder());
                            }
                        });  // 创建单个服务器的连接通道
                AgentClientConnectPool.endpoints.add(endpoint);
                channelMap.put(endpoint, connecManager.getChannel());
                logger.info("add a server channel!; endpoint: " + endpoint.toString());
            } else {
                logger.info("the channel exist!; endpoint: " + endpoint.toString());
            }
        }
        LOCK.AgentChannelLock = false;
        return true;
    }

    /**
     * 对每一个endpoint节点删除通道
     *
     * @param endpoints
     * @return
     * @throws Exception
     */
    public static boolean deleteServers(List<Endpoint> endpoints) {
        // TODO 应该加锁
        LOCK.AgentChannelLock = true;
        for (Endpoint endpoint : endpoints) {
            if (AgentClientConnectPool.channelMap.containsKey(endpoint)) {
                Channel removeChannel = AgentClientConnectPool.channelMap.remove(endpoint);
                removeChannel.close();
                AgentClientConnectPool.endpoints.remove(endpoint);
                logger.info("close channel; endpoint: " + endpoint.toString());
            } else {
                logger.info("the delete chanel don't exist!; endpoint: " + endpoint.toString());
            }
        }
        LOCK.AgentChannelLock = false;
        return true;
    }
}
