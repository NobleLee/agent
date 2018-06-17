package com.alibaba.dubbo.performance.demo.agent.provider.dubbo;

import com.alibaba.dubbo.performance.demo.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.provider.dubbo.model.RpcInvocation;
import com.alibaba.dubbo.performance.demo.agent.tools.JsonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class DubboEncoder extends MessageToByteEncoder {
    // header length.


    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf buffer) throws Exception {
//        DubboRequest req = (DubboRequest) msg;
//
//        // set request id.
//        Bytes.long2bytes(req.getId(), header, 4);
//
//        // encode request data.
//        int savedWriteIndex = buffer.writerIndex();
//        buffer.writerIndex(savedWriteIndex + HEADER_LENGTH);
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        encodeRequestData(bos, req.getData());
//
//        int len = bos.size();
//        buffer.writeBytes(bos.toByteArray());
//        Bytes.int2bytes(len, header, 12);
//        // write
//        buffer.writerIndex(savedWriteIndex);
//        buffer.writeBytes(header); // write header.
//        buffer.writerIndex(savedWriteIndex + HEADER_LENGTH + len);
    }


    public void encodeRequestData(OutputStream out, Object data) throws Exception {
        RpcInvocation inv = (RpcInvocation) data;

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));

        JsonUtils.writeObject(inv.getAttachment("dubbo", "2.0.1"), writer);
        JsonUtils.writeObject(inv.getAttachment("path"), writer);
        JsonUtils.writeObject(inv.getAttachment("version"), writer);
        JsonUtils.writeObject(inv.getMethodName(), writer);
        JsonUtils.writeObject(inv.getParameterTypes(), writer);

        JsonUtils.writeBytes(inv.getArguments(), writer);
        JsonUtils.writeObject(inv.getAttachments(), writer);
    }


    /**
     * 直接将数据封装成Dubbo Req
     *
     * @param buf
     * @return
     */
    public static ByteBuf directSend(ByteBuf buf, ByteBuf header) {

        long id = (long) buf.readInt();


        CompositeByteBuf compositeByteBuf = PooledByteBufAllocator.DEFAULT.compositeBuffer();

        /** 加入reqid */
        header.setLong(4, id);
        /** 信息长度 */
        header.setInt(12, COMMON.Request.dubbo_msg_first.length + COMMON.Request.dubbo_msg_last.length + buf.readableBytes());
        /** 加入头 */
        compositeByteBuf.addComponent(true, header);
        header.retain();
        /** 加入消息头 */
        compositeByteBuf.addComponent(true, COMMON.Request.dubbo_msg_first_buffer);
        COMMON.Request.dubbo_msg_first_buffer.retain();
        /** 加入消息参数 */
        compositeByteBuf.addComponent(true, buf);
        buf.retain();
        /** 加入消息尾 */
        compositeByteBuf.addComponent(true, COMMON.Request.dubbo_msg_last_buffer);
        COMMON.Request.dubbo_msg_last_buffer.retain();

        return compositeByteBuf;
    }

}
