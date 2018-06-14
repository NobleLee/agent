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
        System.err.println(str + bytes.length + Arrays.toString(bytes));
    }

    public static void printBinln(ByteBuf buf, String str) {
        ByteBuf copy = buf.copy();
        byte[] bytes = new byte[copy.readableBytes()];
        copy.readBytes(bytes);
        int i = 0;
        System.err.print(str + bytes.length);
        byte[] array = new byte[8];
        for (byte aByte : bytes) {
            for (int i1 = 7; i1 >= 0; i1--) {
                array[i1] = (byte) (aByte & 1);
                aByte = (byte) (aByte >> 1);
            }
            if (i++ == 0) {
                System.err.print("[");
                for (byte b : array) {
                    System.err.print(b);
                }
            }else{
                System.err.print(", ");
                for (byte b : array) {
                    System.err.print(b);
                }
            }
        }
        System.err.println("]");
    }

    public static void printStringln(ByteBuf buf, int skip, String str) {
        ByteBuf copy = buf.copy();
        copy.skipBytes(skip);
        byte[] bytes = new byte[copy.readableBytes()];
        copy.readBytes(bytes);
        System.err.println(str + new String(bytes));
    }


    public static String getString(ByteBuf buf, String str) {
        ByteBuf copy = buf.copy();
        byte[] bytes = new byte[copy.readableBytes()];
        copy.readBytes(bytes);
        return new String(bytes);
    }

    public static void printStringln(ByteBuf buf, String str) {
        printStringln(buf, 0, str);

    }
}
