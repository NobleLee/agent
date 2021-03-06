package com.alibaba.dubbo.performance.demo.agent.consumer.httpserver;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-09 上午12:36
 */
public class HttpServerInitializer extends ChannelInitializer<NioSocketChannel> {

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        //HTTP请求消息解码器
//        pipeline.addLast("http-decoder", new HttpRequestDecoder());

//        /*
//         * HttpObjectAggregator解码器
//         * 将多个消息转换为单一的FullHttpRequest或FullHttpResponse对象
//         */
 //       pipeline.addLast("http-aggregator", new HttpObjectAggregator(1534));
//
////        pipeline.addLast(new MessageToByteEncoder() {
////
////            @Override
////            protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
////                ByteBuf in = (ByteBuf) msg;
////                ByteBufUtils.printStringln((ByteBuf) msg, "return:\n");
////                out.writeBytes(in);
////            }
////        });
////        // HTTP响应编码器,对HTTP响应进行编码
        pipeline.addLast("http-encoder", new HttpResponseEncoder());
////
////        // 对请求的处理逻辑
 //       pipeline.addLast("server-handler", new HttpServerHandler());

         pipeline.addLast("simple-handler", new HttpSimpleHandler());

    }


}
