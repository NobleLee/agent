package com.alibaba.dubbo.performance.demo.agent.agent.httpserver;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-09 上午12:43
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final String BASE_DIR = System.getProperty("user.dir") + "/src/main/java/com/luangeng/netty/http/i";
    ;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        //400
        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        //405
        if (request.method() != HttpMethod.GET) {
            sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        //404
        String uri = request.uri();
//        uri = URLDecoder.decode(uri, "UTF-8");
//        uri = uri.replace('/', File.separatorChar);
//        File file = new File(BASE_DIR + uri);
//        if (!file.exists() || !file.isFile()) {
//            sendError(ctx, HttpResponseStatus.NOT_FOUND);
//            return;
//        }

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK);

//        FileChannel channel = new FileInputStream(file).getChannel();
//        ByteBuffer buffer = ByteBuffer.allocate(1024);
//        while (channel.read(buffer) != -1) {
//            buffer.flip();
//            response.content().writeBytes(buffer);
//            buffer.clear();
//        }
//        channel.close();



        if (uri.endsWith(".css")) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/css; charset=UTF-8");
        } else {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        }
        long fileLength = response.content().readableBytes();
        HttpUtil.setContentLength(response, fileLength);

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendError(ChannelHandlerContext ctx,
                                  HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                status, Unpooled.copiedBuffer("Failure: " + status.toString()
                + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
