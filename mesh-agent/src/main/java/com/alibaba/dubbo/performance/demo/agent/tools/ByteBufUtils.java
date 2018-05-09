package com.alibaba.dubbo.performance.demo.agent.tools;

import io.netty.buffer.ByteBuf;

import java.util.Arrays;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author gaoguili
 * @create 2018-05-08 上午10:15
 */
public class ByteBufUtils {

    public static void println(ByteBuf buf, String str) {
        ByteBuf copy = buf.copy();
        byte[] bytes = new byte[copy.readableBytes()];
        copy.readBytes(bytes);
        System.err.println(str + Arrays.toString(bytes));
    }
}