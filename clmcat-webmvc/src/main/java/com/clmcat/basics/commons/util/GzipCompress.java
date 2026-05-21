package com.clmcat.basics.commons.util;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author zhangxingyu
 *
 * 描述：gzip压缩工具类
 */
public class GzipCompress {

    /**
     * gzip压缩
     */

    public static byte[] gzipCompress(byte[] data, int bufferSize) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bufferSize);
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
        gzipOutputStream.write(data);
        gzipOutputStream.finish();
        return outputStream.toByteArray();
    }

    /**
     * gzip解压
      */
    public static byte[] gzipDecompress(byte[] data) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
        return readBytes(gzipInputStream);
    }

    /**
     * gzip解压, 判断是否是GZIP编码. 如果不是则返回data, 如果是则进行解压.
     */
    public static byte[] autoAzipDecompress(byte[] data) throws IOException {
        if (isGzip(data)) {
            return gzipDecompress(data);
        } else {
            return data;
        }
    }
    /**
     Gzip文件头前两个字节为固定值：‌ID1: 0x1F（十进制31）,ID2: 0x8B（十进制139）.
     这两个字节的组合0x1F8B是Gzip格式的全局唯一标识符12。任何以这两个字节开头的二进制数据均可初步判定为Gzip压缩数据
     */
    public static boolean isGzip(byte[] data) {
        if (data.length < 2) {
            return false;
        }
        return (data[0] == (byte) 0x1f && data[1] == (byte) 0x8b);
    }

    public static byte[] readBytes(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
        while (true) {
            int len = inputStream.read(buffer);
            if (len <= 0) {
                break;
            }
            outputStream.write(buffer, 0, len);
        }
        return outputStream.toByteArray();
    }

}
