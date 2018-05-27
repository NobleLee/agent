package com.alibaba.dubbo.performance.demo.agent.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.COMMON;
import com.alibaba.dubbo.performance.demo.agent.agent.dubbo.model.DubboRequest;
import com.alibaba.dubbo.performance.demo.agent.agent.dubbo.model.RpcInvocation;
import com.alibaba.dubbo.performance.demo.agent.tools.Bytes;
import com.alibaba.dubbo.performance.demo.agent.tools.JsonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class DubboRpcEncoder extends MessageToByteEncoder {
    // header length.
    protected static final int HEADER_LENGTH = 4;

    protected static final byte[] header = new byte[HEADER_LENGTH];

    static {
        header[0] = -38;
        header[1] = -69;
        header[2] = -58;
        header[3] = 0;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf buffer) throws Exception {
        DubboRequest req = (DubboRequest) msg;

        // set request id.
        Bytes.long2bytes(req.getId(), header, 4);

        // encode request data.
        int savedWriteIndex = buffer.writerIndex();
        buffer.writerIndex(savedWriteIndex + HEADER_LENGTH);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        encodeRequestData(bos, req.getData());

        int len = bos.size();
        buffer.writeBytes(bos.toByteArray());
        Bytes.int2bytes(len, header, 12);
        // write
        buffer.writerIndex(savedWriteIndex);
        buffer.writeBytes(header); // write header.
        buffer.writerIndex(savedWriteIndex + HEADER_LENGTH + len);

//        ByteBuf copy = buffer.copy();
//        byte[] body = new byte[copy.readableBytes()];
//        copy.readBytes(body);
//        System.err.println(len);
//        System.err.println(Arrays.toString(Arrays.copyOfRange(body,0,16)));
//        String s = new String(Arrays.copyOfRange(body, 16, body.length));
//        System.err.println(s);
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

//    public static ByteBuf directSend(ByteBuf buf) {
//        Bytes.long2bytes(buf.readLong(), header, 4);
//        int len = COMMON.Request.dubbo_msg_first.length + COMMON.Request.dubbo_msg_last.length + buf.readableBytes();
//        Bytes.int2bytes(len, header, 12);
//        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(len + HEADER_LENGTH);
//        byteBuf.writeBytes(header);
//        byteBuf.writeBytes(COMMON.Request.dubbo_msg_first);
//        byteBuf.writeBytes(buf);
//        byteBuf.writeBytes(COMMON.Request.dubbo_msg_last);
//        return byteBuf;
//    }

    /**
     * 直接将数据封装成Dubbo Req
     *
     * @param buf
     * @return
     */
    public static ByteBuf directSend(ByteBuf buf) {

        long id = buf.readLong();

        int len = COMMON.Request.dubbo_msg_first.length + COMMON.Request.dubbo_msg_last.length + buf.readableBytes();

        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(len + HEADER_LENGTH);
        /** 加入头 */
        byteBuf.writeBytes(header);
        /** 加入reqid */
        byteBuf.writeLong(id);
        /** 信息长度 */
        byteBuf.writeInt(len);
        /** 加入消息头 */
        byteBuf.writeBytes(COMMON.Request.dubbo_msg_first);
        /** 加入消息参数 */
        byteBuf.writeBytes(buf);
        /** 加入消息尾 */
        byteBuf.writeBytes(COMMON.Request.dubbo_msg_last);

        return byteBuf;
    }

}
