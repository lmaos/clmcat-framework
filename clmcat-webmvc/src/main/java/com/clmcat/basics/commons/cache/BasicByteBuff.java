package com.clmcat.basics.commons.cache;

import javax.sound.midi.Soundbank;
import java.util.Arrays;
import java.util.Random;

/**
 * byte buff
 */
public class BasicByteBuff {


    private byte[] values;
    private int expansionCapacity = 1024;
    public BasicByteBuff(int capacity) {
        this.values = new byte[capacity];
    }

    public BasicByteBuff(int capacity, int expansionCapacity) {
        this.values = new byte[capacity];
        this.expansionCapacity = expansionCapacity;
    }

    private int readIndex = 0;  // 读取INDEX
    private int writeIndex = 0; // 写INDEX

    private int makeReadIndex = -1;


    public void write(byte b) {
        clearAndExpansion(1);
        values[writeIndex++] = b;
    }
    public void write(byte[] bs) {
        write(bs, 0, bs.length);
    }
    public void write(byte[] bs, int off, int len) {
        if (len > bs.length - off) {
            throw new ArrayIndexOutOfBoundsException(String.format("off=%d,len=%d, size=%d", off, len, bs.length));
        }
        clearAndExpansion(bs.length);
        System.arraycopy(bs, off, values, writeIndex, len);
        writeIndex = writeIndex + len;
    }

    public void clearAndExpansion(int needLength) {

        if (writeIndex + needLength <= values.length) {
            return;
        }
        // 扩容
//        System.out.println("-------");
//        System.out.printf("扩容前 %d, %d, \r\n", readIndex, writeIndex);
        // 有效的数据开始位置
        int dataStartIndex = readIndex;

        if (makeReadIndex != -1) {
            dataStartIndex = makeReadIndex;
        }

        // 数据的有效长度
        int dataLength = writeIndex - dataStartIndex;

        // 得到新的数据 原数据长度 + 写入的数据长度 + 1024进行扩容
        byte[] newValues = new byte[dataLength + needLength + expansionCapacity];

        // 将数据 copy 到新的数组内
        System.arraycopy(values, dataStartIndex, newValues, 0, dataLength);

        // 整理读写位置, 所有的index的都向左偏移，其偏移量 = dataStartIndex

        readIndex = readIndex - dataStartIndex;

        if (makeReadIndex != -1) {
            makeReadIndex = 0;
        }

        writeIndex = writeIndex - dataStartIndex;
        this.values = newValues;
        // System.out.printf("扩容后 %d, %d, %d, \r\n", readIndex, writeIndex, values.length);
    }

    public int readable() {
        return writeIndex - readIndex;
    }


    public int read() {
        if (readable() == 0) {
            throw new IndexOutOfBoundsException("len=0");
        }
        return values[readIndex++];
    }

    public byte[] readAll() {
        byte[] bs = toBytes();
        readIndex = readIndex + bs.length;
        return bs;
    }

    public int read(byte[] bs) {
        int length = bs.length;
        int readable = readable();
        if (length > readable) {
            length = readable;
        }
        if (length > 0) {
            System.arraycopy(values, readIndex, bs, 0, length);
            readIndex = readIndex + length;
        }
        return length;
    }

    public void clear() {
        writeIndex = 0;
        readIndex = 0;
        makeReadIndex = -1;
    }

    public void makeReadIndex() {
        makeReadIndex = readIndex;
    }


    public void resetReadIndex() {
        if (makeReadIndex != -1) {
            readIndex = makeReadIndex;
            makeReadIndex = -1;
        }
    }

    public Byte lastByte () {
        if (writeIndex == 0) {
            return null;
        }
        return values[writeIndex - 1];
    }

    public byte[] toBytes() {
        byte[] bs = new byte[readable()];
        System.arraycopy(values, readIndex, bs, 0, bs.length);
        return bs;
    }

//    public static void main(String[] args){
//        BasicByteBuff bs = new BasicByteBuff(10, 10);
//        Random r = new Random();
//        for (int j = 0; j < 30; j++) {
//
//            byte[] data = new byte[r.nextInt(10) + 1];
//            for (int i = 0; i < data.length; i++) {
//                data[i] = (byte)(r.nextInt(256));
//            }
//            bs.write(data);
//
//            byte[] res = new byte[r.nextInt(data.length) + 1];
//            bs.read(res);
//        }
//        System.out.println(Arrays.toString(bs.toBytes()));
//        System.out.println("----");
//        byte[] res = new byte[20];
//        bs.read(res);
//        System.out.println(Arrays.toString(res));
//        System.out.println("----");
//        System.out.println(Arrays.toString(bs.toBytes()));
//
//    }
}
