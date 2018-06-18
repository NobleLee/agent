package com.alibaba.dubbo.performance.demo.agent.consumer.client.tcp;

import com.alibaba.dubbo.performance.demo.agent.consumer.ConnecManager;
import com.alibaba.dubbo.performance.demo.agent.provider.dubbo.DubboClientInitializer;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EndpointHelper;
import com.alibaba.dubbo.performance.demo.agent.tools.ByteBufUtils;
import com.alibaba.dubbo.performance.demo.agent.tools.LOCK;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;


/**
 * 描述: 用于管理agent之间的连接
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-05 下午5:41
 */
public class AgentTcpClient {

    private static Logger logger = LoggerFactory.getLogger(AgentTcpClient.class);

    /**
     * 存放本Client绑定的 http Channel
     */
    private List<Channel> responseChannelList = new ArrayList<>();

    /**
     * Http channel 在本Client中的编号
     */
    private int responseChannelCount = 0;

    /**
     * 负载均衡相关对象
     */
    private static EndpointHelper balanceHelper = EndpointHelper.getINSTANCE(true);

    /**
     * 为防止每次都new，在初始化预先分配一个对象
     */
    private FullHttpResponse response;

    /**
     * 用于给Client进行编号
     */
    private static AtomicInteger agentTcpClientCount = new AtomicInteger(0);

    /**
     * 本Client的编号值
     */
    private int clientIndex;

    /**
     * channel List
     */
    private Channel[] channels = new Channel[3];

    private FutureTask<Channel>[] futureTasks = new FutureTask[3];

    public AgentTcpClient(EventLoop loop) {
        ConnecManager connecManager = new ConnecManager(loop, this, AgentTcpInitializer.class);

        int count = 0;
        for (Endpoint endpoint : balanceHelper.getEndpoints()) {
            FutureTask<Channel> futureTask = connecManager.bind(endpoint.getHost(), endpoint.getPort().get(0));
            futureTasks[count++] = futureTask;
        }

        response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");

        clientIndex = agentTcpClientCount.getAndIncrement();

        balanceHelper.addEndpointReq(clientIndex);
    }

    public int putChannel(Channel channel) {
        responseChannelList.add(channel);
        return responseChannelCount++;
    }

    /**
     * 给指定的server发送消息
     *
     * @param buf
     */
    public void send(ByteBuf buf) throws ExecutionException, InterruptedException {
        // 根据负载均衡算法，选择一个节点发送数据
        int index = balanceHelper.getBalanceIndex(clientIndex);

        while (channels[index] == null && futureTasks[index].get() != null) {
            channels[index] = futureTasks[index].get();
        }

        ByteBufUtils.printDubboMsg(buf);
        channels[index].writeAndFlush(buf);
    }

    /**
     * 将接收到的agent response返回
     *
     * @param msg
     */
    public void response(ByteBuf msg) {

        int id = msg.readInt();
        short endpointIndex = msg.readShort();
        balanceHelper.decrease(clientIndex, endpointIndex);

        Channel responseChannel = responseChannelList.get(id);
        // 封装返回response
        response.retain();
        response.headers().set(CONTENT_LENGTH, msg.readableBytes());
        response.content().writeBytes(msg);

        if (responseChannel != null && responseChannel.isActive()) {
            responseChannel.writeAndFlush(response);
        }

    }


    /**
     * 对每一个endpoint节点都创建一个通道
     *
     * @param endpoints
     * @return
     * @throws Exception
     */
    public synchronized static boolean putServers(List<Endpoint> endpoints) {
        // TODO  应该加锁
        LOCK.AgentChannelLock = true;
        for (Endpoint endpoint : endpoints) {
            balanceHelper.addEndpoint(endpoint);
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
    public synchronized static boolean deleteServers(List<Endpoint> endpoints) {
        // TODO 应该加锁
//        for (Endpoint endpoint : endpoints) {
//            if (endpointList.contains(endpoint)) {
//                int index = endpointList.indexOf(endpoint);
//                endpointList.remove(endpoint);
//                interList.remove(index);
//                logger.info("close channel; endpoint: " + endpoint.toString());
//            }
//        }
        return true;
    }

}
