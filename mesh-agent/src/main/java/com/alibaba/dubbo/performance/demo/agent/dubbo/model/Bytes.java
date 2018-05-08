/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.performance.demo.agent.dubbo.model;


import java.util.HashMap;

/**
 * CodecUtils.
 */

public class Bytes {

    private static HashMap<Integer, String> strMap = new HashMap<>(7);

    static {
        strMap.put(1, "0000000");
        strMap.put(2, "000000");
        strMap.put(3, "00000");
        strMap.put(4, "0000");
        strMap.put(5, "000");
        strMap.put(6, "00");
        strMap.put(7, "0");
        strMap.put(8, "");
    }

    /**
     * 字节数组拷贝
     *
     * @param from
     * @param dst
     * @param src
     * @param srcFrom
     * @param length
     */
    public static void copy(int from, byte[] dst, byte[] src, int srcFrom, int length) {
        for (int i = srcFrom; i < srcFrom + length; i++) {
            dst[from++] = src[i];
        }
    }


    /**
     * 获取字符串id
     *
     * @param str
     * @return
     */
    public static String str2numstr(byte[] str) {
        return String.valueOf(Long.parseLong(new String(str)));
    }

    /**
     * 获取定长的stringid
     *
     * @param str
     * @return
     */
    public static String str2_8Byte(String str) {
        return strMap.get(str.length()) + str;
    }


    public static void str2bytes(String v, byte[] b, int off) {
        byte[] bytes = v.getBytes();
        b[off + 7] = bytes[7];
        b[off + 6] = bytes[6];
        b[off + 5] = bytes[5];
        b[off + 4] = bytes[4];
        b[off + 3] = bytes[3];
        b[off + 2] = bytes[2];
        b[off + 1] = bytes[1];
        b[off + 0] = bytes[0];
    }


    private Bytes() {
    }

    /**
     * to byte array.
     *
     * @param v value.
     * @param b byte array.
     */
    public static void short2bytes(short v, byte[] b) {
        short2bytes(v, b, 0);
    }

    /**
     * to byte array.
     *
     * @param v value.
     * @param b byte array.
     */
    public static void short2bytes(short v, byte[] b, int off) {
        b[off + 1] = (byte) v;
        b[off + 0] = (byte) (v >>> 8);
    }

    /**
     * to byte array.
     *
     * @param v   value.
     * @param b   byte array.
     * @param off array offset.
     */
    public static void int2bytes(int v, byte[] b, int off) {
        b[off + 3] = (byte) v;
        b[off + 2] = (byte) (v >>> 8);
        b[off + 1] = (byte) (v >>> 16);
        b[off + 0] = (byte) (v >>> 24);
    }

    /**
     * to byte array.
     *
     * @param v   value.
     * @param b   byte array.
     * @param off array offset.
     */
    public static void long2bytes(long v, byte[] b, int off) {
        b[off + 7] = (byte) v;
        b[off + 6] = (byte) (v >>> 8);
        b[off + 5] = (byte) (v >>> 16);
        b[off + 4] = (byte) (v >>> 24);
        b[off + 3] = (byte) (v >>> 32);
        b[off + 2] = (byte) (v >>> 40);
        b[off + 1] = (byte) (v >>> 48);
        b[off + 0] = (byte) (v >>> 56);
    }

    /**
     * to long.
     *
     * @param b   byte array.
     * @param off offset.
     * @return long.
     */
    public static long bytes2long(byte[] b, int off) {
        return ((b[off + 7] & 0xFFL) << 0) +
                ((b[off + 6] & 0xFFL) << 8) +
                ((b[off + 5] & 0xFFL) << 16) +
                ((b[off + 4] & 0xFFL) << 24) +
                ((b[off + 3] & 0xFFL) << 32) +
                ((b[off + 2] & 0xFFL) << 40) +
                ((b[off + 1] & 0xFFL) << 48) +
                (((long) b[off + 0]) << 56);
    }
}