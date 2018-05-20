package com.alibaba.dubbo.performance.demo.agent.agent.httpserver;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-09 上午12:36
 */
public class HttpChannelInitializer extends ChannelInitializer<EpollSocketChannel> {
    private static String header = "interface";

    private static ByteBuf delimiter = Unpooled.copiedBuffer(header.getBytes());

    @Override
    protected void initChannel(EpollSocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // HTTP请求消息解码器
//       pipeline.addLast("http-decoder", new HttpRequestDecoder());
//
//        /*
//         * HttpObjectAggregator解码器
//         * 将多个消息转换为单一的FullHttpRequest或FullHttpResponse对象
//         */
//        pipeline.addLast("http-aggregator", new HttpObjectAggregator(65536));
//
//        // HTTP响应编码器,对HTTP响应进行编码
        pipeline.addLast("http-encoder", new HttpResponseEncoder());
//
//        // 对请求的处理逻辑
//        pipeline.addLast("server-handler", new HttpServerHandler());
     //  pipeline.addLast("mesg-split",new LineBasedFrameDecoder(2048));
        pipeline.addLast("simple-handler", new HttpSimpleHandler());

    }


}
