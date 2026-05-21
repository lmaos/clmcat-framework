package com.clmcat.basics.commons.io;

import com.clmcat.basics.commons.cache.BasicByteBuff;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.BiConsumer;

/**
 * IO 工具
 */
public class IOUtils {
    /**
     * 按行读取，每读取一行进行消费
     * @param in
     * @param consumer 参数1 第几行， 参数2 本行的内容
     */
    public static void readLine(InputStream in, BiConsumer<Integer, byte[]> consumer, BasicByteBuff basicByteBuff) {


        byte[] data = new byte[1024];
        int len = 0;
        int index = 0;
        int readIndex = 0;
        int num = 0;
        boolean f = basicByteBuff.readable() > 0;
        try {
            while (true) {
                if (f) {
                    len = basicByteBuff.read(data);
                    if (basicByteBuff.readable() == 0) {
                        f = false;
                    }
                } else {
                    len = in.read(data);
                    if (len == -1) {
                        break;
                    }
                }


                for (index = 0,readIndex = 0; index < len; index++) {

                    byte b = data[index];

                    if (b == '\n') {

                        byte[] bytes = basicByteBuff.readAll();
                        byte[] bs = null;
                        if (index == 0) {
                            if (bytes.length > 0 && bytes[bytes.length - 1] == '\r') {
                                bs = new byte[bytes.length - 1];
                                System.arraycopy(bytes, 0, bs, 0, bs.length);
                            } else {
                                bs = bytes;
                            }
                        } else {
                            if (data[index - 1] == '\r') {
                                bs = new byte[bytes.length + (index - 1) - readIndex];
                            } else {
                                bs = new byte[bytes.length + index - readIndex];
                            }
                            System.arraycopy(bytes, 0, bs, 0, bytes.length);
                            System.arraycopy(data, readIndex, bs, bytes.length, bs.length);
                            readIndex = index + 1;
                        }

                        consumer.accept(num++, bs);
                    }

                }

                if (readIndex < len) {
                    basicByteBuff.write(data, readIndex, len - readIndex);
                    readIndex = len;
                }
            }

            if (basicByteBuff.readable() > 0) {
                byte[] bs = basicByteBuff.readAll();
                consumer.accept(num++, bs);
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (readIndex < len) {
                basicByteBuff.write(data, readIndex, len - readIndex);
                readIndex = len;
            }
        }
    }
    public static void main(String[] args){
        BasicByteBuff basicByteBuff = new BasicByteBuff(1024);
        ByteArrayInputStream inputStream = new ByteArrayInputStream("111111\r\n111111\r\n\r\n".getBytes());
        for (int i = 0; i < 4; i++) {
            readLine(inputStream, (num, bs)-> {
              System.out.println(Arrays.toString(bs));
              throw new RuntimeException();
          }, basicByteBuff);

        }
    }
}
