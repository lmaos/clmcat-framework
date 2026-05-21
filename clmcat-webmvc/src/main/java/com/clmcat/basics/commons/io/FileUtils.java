package com.clmcat.basics.commons.io;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件的操作工具
 */
public class FileUtils {

    /* *******************************************************

         读取文件数据的工具

     ********************************************************* */

    /** 返换行符\r\n分割的文件内容
     *
     * @param filePath 文件路径
     * @param charset  文件内容的编码
     * @return List-String
     * @throws IOException 文件不存在时会抛出 {@link FileNotFoundException}
     */
    public static final List<String> readFileToStrings(String filePath, Charset charset) throws IOException {
        return readFileToStrings(new File(filePath), charset);
    }

    /** 返换行符\r\n分割的文件内容
     *
     * @param file     文件路径
     * @param charset  文件内容的编码
     * @return List-String
     * @throws IOException 文件不存在时会抛出 {@link FileNotFoundException}
     */
    public static final List<String> readFileToStrings(File file, Charset charset) throws IOException {
        String str = readFileToString(file, charset);
        List<String> list = new ArrayList<>();
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < str.length(); i ++ ) {
            char c = str.charAt(i);
            if (c == '\r' || c == '\n') {
                if (tmp.length() > 0) {
                    list.add(tmp.toString());
                    tmp = new StringBuilder();
                }
            } else {
                tmp.append(c);
            }
        }
        if (tmp.length() > 0) {
            list.add(tmp.toString());
        }
        return list;
    }

    /** 返回文件的全部内容
     *
     * @param filePath 文件路径
     * @param charset  文件内容的编码
     * @return String
     * @throws IOException 文件不存在时会抛出 {@link FileNotFoundException}
     */
    public static final String readFileToString(String filePath, Charset charset) throws IOException {
        return readFileToString(new File(filePath), charset);
    }

    /** 返回文件的全部内容
     *
     * @param file     文件路径
     * @param charset  文件内容的编码
     * @return String
     * @throws IOException 文件不存在时会抛出 {@link FileNotFoundException}
     */
    public static final String readFileToString(File file, Charset charset) throws IOException {
        byte[] buf = readFileToBytes(file);
        return new String(buf, charset);
    }

    /** 返回文件数据 return byte[]
     *
     * @param filePath 文件路径
     * @return byte[]
     * @throws IOException 文件不存在时会抛出 {@link FileNotFoundException}
     */
    public static final byte[] readFileToBytes(String filePath) throws IOException {
        return readFileToBytes(new File(filePath));
    }

    /** 返回文件数据 return byte[]
     *
     * @param file     文件路径
     * @return byte[]
     * @throws IOException 文件不存在时会抛出 {@link FileNotFoundException}
     */
    public static final byte[] readFileToBytes(File file) throws IOException {
        try (FileInputStream fin = new FileInputStream(file)) {
            byte[] buf = new byte[(int)file.length()];
            fin.read(buf);
            return buf;
        }
    }

    /* *******************************************************

         写文件的工具

     ********************************************************* */


    /**
     *
     * @param text     字符串的内容
     * @param charset  内容的编码
     * @param filePath 文件路径
     * @param append 是否追加文件 true 追加，false 替换
     * @throws IOException 文件不存在时会抛出 {@link FileNotFoundException}
     */
    public static final void writeFile(String text, Charset charset, String filePath, boolean append) throws IOException {
        writeFile(text, charset, new File(filePath), append);
    }

    /**
     *
     * @param text     字符串的内容
     * @param charset  内容的编码
     * @param file     文件路径
     * @param append 是否追加文件 true 追加，false 替换
     * @throws IOException 文件不存在时会抛出 {@link FileNotFoundException}
     */
    public static final void writeFile(String text, Charset charset, File file, boolean append) throws IOException {
        writeFile(text.getBytes(charset), file, append);
    }

    /**
     *
     * @param buf       文件byte[]数据
     * @param filePath  文件路径
     * @param append    是否追加文件 true 追加，false 替换
     * @throws IOException 文件不存在时会抛出 {@link FileNotFoundException}
     */
    public static final void writeFile(byte[] buf, String filePath, boolean append) throws IOException {
        writeFile(buf, new File(filePath), append);
    }

    /**
     *
     * @param buf       文件byte[]数据
     * @param file      文件路径
     * @param append    是否追加文件 true 追加，false 替换
     * @throws IOException 文件不存在时会抛出 {@link FileNotFoundException}
     */
    public static final void writeFile(byte[] buf, File file, boolean append) throws IOException {
        try (FileOutputStream fout = new FileOutputStream(file, append)) {
            fout.write(buf);
            fout.flush();
        }
    }

}
