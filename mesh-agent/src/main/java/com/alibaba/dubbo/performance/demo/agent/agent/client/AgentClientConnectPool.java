package com.alibaba.dubbo.performance.demo.agent.agent.client;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.agent.ConnecManager;
import com.alibaba.dubbo.performance.demo.agent.agent.httpserver.HttpServerHandler;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EndpointHelper;
import com.alibaba.dubbo.performance.demo.agent.tools.LOCK;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
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


    // key 节点 value 通道
    private static HashMap<Endpoint, Channel> channelMap = new HashMap<>();
    // 连接节点数
    private static List<Endpoint> endpoints = new ArrayList<>(); //之后可以考虑把初始化的过程放到前面

    private static ConnecManager connecManager;

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


    private AgentClientConnectPool() {
    }


    /**
     * 生成请求header，directly 采用一个buffer读写
     *
     * @param buf
     * @param channelId
     * @throws Exception
     */
    public void sendToServerwithChannelId(ByteBuf buf, long channelId) {
        if (buf.readableBytes() < 136) {
            logger.error("message length less 136 ");
            return;
        }
        buf.skipBytes(128);
        buf.setLong(buf.readerIndex(), channelId);
        buf.writeShort(COMMON.MAGIC);
        // 根据负载均衡算法，选择一个节点发送数据
        // TODO 没有考虑ChanelMap的线程安全问题；假设在服务过程中没有新的服务的注册问题
        Channel sendChannel = channelMap.get(endpoints.get(2));
        buf.retain();
        // ByteBufUtils.printStringln(buf, 8,""+id+"  ");
        sendChannel.writeAndFlush(buf);
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
        response.headers().set(CONTENT_LENGTH, buf.readableBytes());
        response.content().writeBytes(buf);
        Channel channel = HttpServerHandler.channelList.get((int) requestId);
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(response);
        }
    }

    /**
     * 直接睡眠将结果返回
     *
     * @param buf
     * @param channel
     */
    private static ScheduledExecutorService executorService;

    public void responseTest(ByteBuf buf, Channel channel) {
        if (executorService == null) {
            synchronized (this) {
                if (executorService == null)
                    executorService = Executors.newScheduledThreadPool(256);
            }
        }

        buf.skipBytes(136);
        String hashcode = String.valueOf(buf.toString(Charsets.UTF_8).hashCode());
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, hashcode.length());
        response.content().writeBytes(hashcode.getBytes());
        executorService.schedule(() -> {
            ChannelFuture channelFuture = channel.writeAndFlush(response);
        }, 50, TimeUnit.MILLISECONDS);
    }

//    /**
//     * 本地自环测试with hash冲突
//     *
//     * @param buf
//     * @param channel
//     */
//    public void responseTestWithCost(ByteBuf buf, Channel channel) {
//        if (buf.readableBytes() < 136) return;
//        if (executorService == null) {
//            synchronized (this) {
//                if (executorService == null)
//                    executorService = Executors.newScheduledThreadPool(256);
//            }
//        }
//        buf.skipBytes(136);
//        String hashcode = String.valueOf(buf.toString(Charsets.UTF_8).hashCode());
//        // 将请求的id写入ByteBuf
//        byte index = (byte) (Thread.currentThread().getId() % COMMON.HTTPSERVER_WORK_THREAD);
//        long id = System.currentTimeMillis() << 35 | ((long) r.nextInt(Integer.MAX_VALUE)) << 3 | index;
//        requestList.get(index).put(Long.valueOf(id), channel);
//
//        // 跳过前面的固定字符串
//        executorService.schedule(() -> {
//            int idx = (int) (id & 0x7);
//            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
//            response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
//            response.headers().set(CONTENT_LENGTH, hashcode.length());
//            response.content().writeBytes(hashcode.getBytes());
//            Channel remove = requestList.get(idx).remove(id);
//            if (remove != null && remove.isActive()) {
//                remove.writeAndFlush(response);
//            }
//        }, 50, TimeUnit.MILLISECONDS);
//
//    }


    /**
     * 对每一个endpoint节点都创建一个通道
     *
     * @param endpoints
     * @return
     * @throws Exception
     */
    public static boolean putServers(List<Endpoint> endpoints) {
        // TODO  应该加锁
//        LOCK.AgentChannelLock = true;
//        if (instance == null) return false;
//
//        if (connecManager == null) {
//            synchronized (ConnecManager.class) {
//                // if (connecManager == null)
//                // connecManager = new ConnecManager(COMMON.AgentClient_THREAD, AgentClientInitializer.class);
//            }
//        }
//
//        for (Endpoint endpoint : endpoints) {
//            if (!channelMap.containsKey(endpoint)) {
//                // logger.info("prepare connect server：" + endpoint.toString());
//                Channel channel = connecManager.bind(endpoint.getHost(), endpoint.getPort());// 创建单个服务器的连接通道
//                AgentClientConnectPool.endpoints.add(endpoint);
//                synchronized (AgentClientConnectPool.class) {
//                    channelMap.put(endpoint, channel);
//                }
//                logger.info("add a server channel!; endpoint: " + endpoint.toString());
//            } else {
//                logger.info("the channel exist!; endpoint: " + endpoint.toString());
//            }
//        }
//
//        LOCK.AgentChannelLock = false;
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
//        LOCK.AgentChannelLock = true;
//        if (instance == null) return false;
//        for (Endpoint endpoint : endpoints) {
//            if (AgentClientConnectPool.channelMap.containsKey(endpoint)) {
//                synchronized (AgentClientConnectPool.class) {
//                    Channel removeChannel = channelMap.remove(endpoint);
//                    removeChannel.close();
//                }
//                AgentClientConnectPool.endpoints.remove(endpoint);
//                logger.info("close channel; endpoint: " + endpoint.toString());
//            } else {
//                logger.info("the delete chanel don't exist!; endpoint: " + endpoint.toString());
//            }
//        }
//        LOCK.AgentChannelLock = false;
        return true;
    }
}
