package com.clmcat.basics.commons.lang;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteOrder;

/**
 * 数字转化工具
 */
public class NumberUtils {

    public static byte toByte(Object value){
        return toByte(value, (byte)0);
    }

    public static Byte toByte(Object value,Byte def){

        if(value == null){
            return def;
        }
        if(value instanceof Number){
            return ((Number) value).byteValue();
        }
        try{
            return (byte)Integer.parseInt(String.valueOf(value));
        }catch(Exception e){
            return def;
        }
    }

    public static byte[] toBytes(Object[] objs, byte def){
        byte[] vs = new byte[objs.length];
        for (int i = 0; i < vs.length; i++) {
            vs[i] = toByte(objs[i], def);
        }
        return vs;
    }

    public static short toShort(Object value){
        return toShort(value,(short)0);
    }

    public static Short toShort(Object value, Short def){

        if(value == null){
            return def;
        }
        if(value instanceof Number){
            return ((Number) value).shortValue();
        }
        try{
            return Short.parseShort(String.valueOf(value));
        }catch(Exception e){
            return def;
        }
    }
    public static short[] toShorts(Object[] objs, short def){
        short[] vs = new short[objs.length];
        for (int i = 0; i < vs.length; i++) {
            vs[i] = toShort(objs[i], def);
        }
        return vs;
    }

    public static int toInt(Object value){
        return toInt(value, 0);
    }

    public static Integer toInt(Object value, Integer def){

        if(value == null){
            return def;
        }
        if(value instanceof Number){
            return ((Number) value).intValue();
        }
        try{
            return Integer.parseInt(String.valueOf(value));
        }catch(Exception e){
            return def;
        }
    }

    public static int[] toInts(Object[] objs,int def){
        int[] vs = new int[objs.length];
        for (int i = 0; i < vs.length; i++) {
            vs[i] = toInt(objs[i], def);
        }
        return vs;
    }

    public static long toLong(Object value){
        return toLong(value, 0L);
    }

    public static Long toLong(Object value, Long def){

        if(value == null){
            return def;
        }
        if(value instanceof Number){
            return ((Number) value).longValue();
        }
        try{
            return Long.parseLong(String.valueOf(value));
        }catch(Exception e){
            return def;
        }
    }

    public static long[] toLongs(Object[] objs,long def){
        long[] vs = new long[objs.length];
        for (int i = 0; i < vs.length; i++) {
            vs[i] = toLong(objs[i], def);
        }
        return vs;
    }

    public static float toFloat(Object value){
        return toFloat(value, 0F);
    }

    public static Float toFloat(Object value,Float def){

        if(value == null){
            return def;
        }
        if(value instanceof Number){
            return ((Number) value).floatValue();
        }
        try{
            return Float.parseFloat(String.valueOf(value));
        }catch(Exception e){
            return def;
        }
    }

    public static float[] toFloats(Object[] objs,float def){
        float[] vs = new float[objs.length];
        for (int i = 0; i < vs.length; i++) {
            vs[i] = toFloat(objs[i], def);
        }
        return vs;
    }

    public static double toDouble(Object value){
        return toDouble(value, 0D);
    }

    public static Double toDouble(Object value,Double def){

        if(value == null){
            return def;
        }
        if(value instanceof Number){
            return ((Number) value).doubleValue();
        }
        try{
            return Double.parseDouble(String.valueOf(value));
        }catch(Exception e){
            return def;
        }
    }

    public static double[] toDoubles(Object[] objs,double def){
        double[] vs = new double[objs.length];
        for (int i = 0; i < vs.length; i++) {
            vs[i] = toDouble(objs[i], def);
        }
        return vs;
    }

    public static boolean toBoolean(Object value) {
        return toBoolean(value, false);
    }


    public static Boolean toBoolean(Object value, Boolean def){
        if (value == null) {
            return def;
        }
        if (value instanceof Boolean) {
            return (Boolean)value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    public static boolean[] toBooleans(Object[] objs){
        boolean[] vs = new boolean[objs.length];
        for (int i = 0; i < vs.length; i++) {
            vs[i] = toBoolean(objs[i]);
        }
        return vs;
    }

    public static BigDecimal toBigDecimal(Object value){
        return toBigDecimal(value, new BigDecimal(0));
    }

    public static BigDecimal toBigDecimal(Object value,BigDecimal def){
        if(value == null){
            return def;
        }
        if(value instanceof BigDecimal){
            return (BigDecimal) value;
        }
        if(value instanceof Number){
            return new BigDecimal(((Number)value).doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception e) {
            return def;
        }
    }

    // short to bytes
    public static byte[] toBytesByShort(ByteOrder byteOrder, Object val) {
    	if (val instanceof Character) {
    		val = (short)(char)val;
    	}
    	return toBytesByShort(byteOrder, ((Number)val).shortValue());
    }
    
    public static byte[] toBytesByShort(ByteOrder byteOrder, short val) {
    	return byteOrder == ByteOrder.BIG_ENDIAN ? toBytesByShortB(val) : toBytesByShortL(val);
    }
    
    public static byte[] toBytesByShortB(short val){
        byte[] bs = new byte[2];
        bs[0] = (byte) ((val>>8)&0xFF);
        bs[1] = (byte) ((val)&0xFF);
        return bs;
    }
    

    public static short toShortByBytesB(byte[] bs) {
        short val = (short) (((bs[0]&0xFF)<<8)|(bs[1]&0xFF));
        return val;
    }
    
    public static short toShortByBytesB(byte[] bs, int startIndex) {
    	short val = (short) (((bs[startIndex]&0xFF)<<8)|(bs[startIndex + 1]&0xFF));
    	return val;
    }

    public static byte[] toBytesByShortL(short val) {
        byte[] bs = new byte[2];
        bs[1] = (byte) ((val>>8)&0xFF);
        bs[0] = (byte) ((val)&0xFF);
        return bs;
    }
    

    public static short toShortByBytesL(byte[] bs) {
        short val = (short) (((bs[1]&0xFF)<<8)|(bs[0]&0xFF));
        return val;
    }
    public static short toShortByBytesL(byte[] bs, int startIndex) {
    	short val = (short) (((bs[startIndex + 1]&0xFF)<<8)|(bs[startIndex]&0xFF));
    	return val;
    }
    //>> put-short
    
    public static int putBytesByShort(ByteOrder byteOrder, Object val, byte[] bs, int startIndex) {
    	if (val instanceof Character) {
    		val = (short)(char)val;
    	}
    	return putBytesByShort(byteOrder, ((Number)val).shortValue(), bs, startIndex);
    }
    
    public static int putBytesByShort(ByteOrder byteOrder, short val, byte[] bs, int startIndex) {
    	return byteOrder == ByteOrder.BIG_ENDIAN ? putBytesByShortB(val, bs, startIndex) : putBytesByShortL(val, bs, startIndex);
    }

    public static int putBytesByShortB(short val, byte[] bs, int startIndex){
    	bs[startIndex] = (byte) ((val>>8)&0xFF);
    	bs[startIndex + 1] = (byte) ((val)&0xFF);
    	return 2;
    }

    public static int putBytesByShortL(short val, byte[] bs, int startIndex) {
        bs[startIndex + 1] = (byte) ((val>>8)&0xFF);
        bs[startIndex] = (byte) ((val)&0xFF);
        return 2;
    }
    
    
    // int to bytes
    
    public static byte[] toBytesByInt(ByteOrder byteOrder, Object val) {
    	return toBytesByInt(byteOrder, ((Number)val).intValue());
    }
    
    public static byte[] toBytesByInt(ByteOrder byteOrder, int val) {
    	return byteOrder == ByteOrder.BIG_ENDIAN ? toBytesByIntB(val) : toBytesByIntL(val);
    }
    

    public static byte[] toBytesByIntB(int val) {
        byte[] bs = new byte[4];
        bs[0] = (byte) ((val>>24)&0xFF);
        bs[1] = (byte) ((val>>16)&0xFF);
        bs[2] = (byte) ((val>>8)&0xFF);
        bs[3] = (byte) ((val)&0xFF);
        return bs;
    }
    
    public static int toIntByBytesB(byte[] bs) {
        int val = (int) (((bs[0]&0xFF)<<24)
                |((bs[1]&0xFF)<<16)
                |((bs[2]&0xFF)<<8)
                |(bs[3]&0xFF));
        return val;
    }
    
    public static int toIntByBytesB(byte[] bs, int startIndex) {
        int val = (int) (((bs[startIndex]&0xFF)<<24)
                |((bs[startIndex + 1]&0xFF)<<16)
                |((bs[startIndex + 2]&0xFF)<<8)
                |(bs[startIndex + 3]&0xFF));
        return val;
    }

    public static byte[] toBytesByIntL(int val) {
        byte[] bs = new byte[4];
        bs[3] = (byte) ((val>>24)&0xFF);
        bs[2] = (byte) ((val>>16)&0xFF);
        bs[1] = (byte) ((val>>8)&0xFF);
        bs[0] = (byte) ((val)&0xFF);
        return bs;
    }
    public static int toIntByBytesL(byte[] bs) {
        int val = (int) (((bs[3]&0xFF)<<24)
                |((bs[2]&0xFF)<<16)
                |((bs[1]&0xFF)<<8)
                |(bs[0]&0xFF));
        return val;
    }
    
    public static int toIntByBytesL(byte[] bs, int startIndex) {
        int val = (int) (((bs[startIndex + 3]&0xFF)<<24)
                |((bs[startIndex + 2]&0xFF)<<16)
                |((bs[startIndex + 1]&0xFF)<<8)
                |(bs[startIndex]&0xFF));
        return val;
    }
    
    // >> put int
    
    public static int putBytesByInt(ByteOrder byteOrder, Object val, byte[] bs, int startIndex) {
    	if (val instanceof Character) {
    		val = (short)(char)val;
    	}
    	return putBytesByInt(byteOrder, ((Number)val).intValue(), bs, startIndex);
    }
    
    public static int putBytesByInt(ByteOrder byteOrder, int val, byte[] bs, int startIndex) {
    	return byteOrder == ByteOrder.BIG_ENDIAN ? putBytesByIntB(val, bs, startIndex) : putBytesByIntL(val, bs, startIndex);
    }

    public static int putBytesByIntB(int val, byte[] bs, int startIndex) {
        bs[startIndex] = (byte) ((val>>24)&0xFF);
        bs[startIndex + 1] = (byte) ((val>>16)&0xFF);
        bs[startIndex + 2] = (byte) ((val>>8)&0xFF);
        bs[startIndex + 3] = (byte) ((val)&0xFF);
        return 4;
    }
    
    public static int putBytesByIntL(int val, byte[] bs, int startIndex) {
        bs[startIndex + 3] = (byte) ((val>>24)&0xFF);
        bs[startIndex + 2] = (byte) ((val>>16)&0xFF);
        bs[startIndex + 1] = (byte) ((val>>8)&0xFF);
        bs[startIndex] = (byte) ((val)&0xFF);
        return 4;
    }
    
    
    // long to bytes
    
    public static byte[] toBytesByLong(ByteOrder byteOrder, Object val) {
    	if (val instanceof Character) {
    		val = (short)(char)val;
    	}
    	return toBytesByLong(byteOrder, ((Number)val).longValue());
    }
    
    public static byte[] toBytesByLong(ByteOrder byteOrder, long val) {
    	return byteOrder == ByteOrder.BIG_ENDIAN ? toBytesByLongB(val) : toBytesByLongL(val);
    }

	public static byte[] toBytesByLongB(long val) {
		byte[] bs = new byte[8];
		bs[0] = (byte) ((val >> 56) & 0xFF);
		bs[1] = (byte) ((val >> 48) & 0xFF);
		bs[2] = (byte) ((val >> 40) & 0xFF);
		bs[3] = (byte) ((val >> 32) & 0xFF);
		bs[4] = (byte) ((val >> 24) & 0xFF);
		bs[5] = (byte) ((val >> 16) & 0xFF);
		bs[6] = (byte) ((val >> 8) & 0xFF);
		bs[7] = (byte) ((val) & 0xFF);
		return bs;
	}
    
    public static long toLongByBytesB(byte[] bs) {
        long val = (long) (
                ((long)(bs[0]&0xFF)<<56)
                        |((long)(bs[1]&0xFF)<<48)
                        |((long)(bs[2]&0xFF)<<40)
                        |((long)(bs[3]&0xFF)<<32)
                        |((long)(bs[4]&0xFF)<<24)
                        |((long)(bs[5]&0xFF)<<16)
                        |((long)(bs[6]&0xFF)<<8)
                        |(long)(bs[7]&0xFF));
        return val;
    }
    public static long toLongByBytesB(byte[] bs, int startIndex){
    	long val = (long) (
    			((long)(bs[startIndex]&0xFF)<<56)
    			|((long)(bs[startIndex + 1]&0xFF)<<48)
    			|((long)(bs[startIndex + 2]&0xFF)<<40)
    			|((long)(bs[startIndex + 3]&0xFF)<<32)
    			|((long)(bs[startIndex + 4]&0xFF)<<24)
    			|((long)(bs[startIndex + 5]&0xFF)<<16)
    			|((long)(bs[startIndex + 6]&0xFF)<<8)
    			|(long)(bs[startIndex + 7]&0xFF));
    	return val;
    }

	public static byte[] toBytesByLongL(long val) {
		byte[] bs = new byte[8];
		bs[7] = (byte) ((val >> 56) & 0xFF);
		bs[6] = (byte) ((val >> 48) & 0xFF);
		bs[5] = (byte) ((val >> 40) & 0xFF);
		bs[4] = (byte) ((val >> 32) & 0xFF);
		bs[3] = (byte) ((val >> 24) & 0xFF);
		bs[2] = (byte) ((val >> 16) & 0xFF);
		bs[1] = (byte) ((val >> 8) & 0xFF);
		bs[0] = (byte) ((val) & 0xFF);
		return bs;
	}

    public static long toLongByBytesL(byte[] bs){
        long val = (long) (
                ((long)(bs[7]&0xFF)<<56)
                        |((long)(bs[6]&0xFF)<<48)
                        |((long)(bs[5]&0xFF)<<40)
                        |((long)(bs[4]&0xFF)<<32)
                        |((long)(bs[3]&0xFF)<<24)
                        |((long)(bs[2]&0xFF)<<16)
                        |((long)(bs[1]&0xFF)<<8)
                        |(long)(bs[0]&0xFF));
        return val;
    }
    
    public static long toLongByBytesL(byte[] bs, int startIndex){
    	long val = (long) (
    			((long)(bs[startIndex + 7]&0xFF)<<56)
    			|((long)(bs[startIndex + 6]&0xFF)<<48)
    			|((long)(bs[startIndex + 5]&0xFF)<<40)
    			|((long)(bs[startIndex + 4]&0xFF)<<32)
    			|((long)(bs[startIndex + 3]&0xFF)<<24)
    			|((long)(bs[startIndex + 2]&0xFF)<<16)
    			|((long)(bs[startIndex + 1]&0xFF)<<8)
    			|(long)(bs[startIndex]&0xFF));
    	return val;
    }
    
    // >> put long
    
    public static int putBytesByLong(ByteOrder byteOrder, Object val, byte[] bs, int startIndex) {
    	if (val instanceof Character) {
    		val = (short)(char)val;
    	}
    	return putBytesByLong(byteOrder, ((Number)val).longValue(), bs, startIndex);
    }
    
    public static int putBytesByLong(ByteOrder byteOrder, long val, byte[] bs, int startIndex) {
    	return byteOrder == ByteOrder.BIG_ENDIAN ? putBytesByLongB(val, bs, startIndex) : putBytesByLongL(val, bs, startIndex);
    }
    
    public static int putBytesByLongB(long val, byte[] bs, int startIndex) {
		bs[startIndex] = (byte) ((val >> 56) & 0xFF);
		bs[startIndex + 1] = (byte) ((val >> 48) & 0xFF);
		bs[startIndex + 2] = (byte) ((val >> 40) & 0xFF);
		bs[startIndex + 3] = (byte) ((val >> 32) & 0xFF);
		bs[startIndex + 4] = (byte) ((val >> 24) & 0xFF);
		bs[startIndex + 5] = (byte) ((val >> 16) & 0xFF);
		bs[startIndex + 6] = (byte) ((val >> 8) & 0xFF);
		bs[startIndex + 7] = (byte) ((val) & 0xFF);
		return 8;
	}
    
	public static int putBytesByLongL(long val, byte[] bs, int startIndex) {
		bs[startIndex + 7] = (byte) ((val >> 56) & 0xFF);
		bs[startIndex + 6] = (byte) ((val >> 48) & 0xFF);
		bs[startIndex + 5] = (byte) ((val >> 40) & 0xFF);
		bs[startIndex + 4] = (byte) ((val >> 32) & 0xFF);
		bs[startIndex + 3] = (byte) ((val >> 24) & 0xFF);
		bs[startIndex + 2] = (byte) ((val >> 16) & 0xFF);
		bs[startIndex + 1] = (byte) ((val >> 8) & 0xFF);
		bs[startIndex] = (byte) ((val) & 0xFF);
		return 8;
	}

    // double to bytes
    
    public static byte[] toBytesByDouble(ByteOrder byteOrder, Object val) {
    	return toBytesByDouble(byteOrder, ((Number)val).doubleValue());
    }
    
    public static byte[] toBytesByDouble(ByteOrder byteOrder, double val){
    	return byteOrder == ByteOrder.BIG_ENDIAN ? toBytesByDoubleB(val) : toBytesByDoubleL(val);
    }


    public static byte[] toBytesByDoubleB(double val){

        return toBytesByLongB(Double.doubleToLongBits(val));
    }

    public static double toDoubleByBytesB(byte[] bs){
        long val = toLongByBytesB(bs);
        return Double.longBitsToDouble(val);
    }
    
    public static double toDoubleByBytesB(byte[] bs, int startIndex){
    	long val = toLongByBytesB(bs, startIndex);
    	return Double.longBitsToDouble(val);
    }

    public static byte[] toBytesByDoubleL(double val){

        return toBytesByLongL(Double.doubleToLongBits(val));
    }

	public static double toDoubleByBytesL(byte[] bs) {
		long val = toLongByBytesL(bs);
		return Double.longBitsToDouble(val);
	}
    
	public static double toDoubleByBytesL(byte[] bs, int startIndex) {
		long val = toLongByBytesL(bs, startIndex);
		return Double.longBitsToDouble(val);
	}
	
	// >> put double
	
    public static int putBytesByDouble(ByteOrder byteOrder, Object val, byte[] bs, int startIndex) {
    	return putBytesByDouble(byteOrder, ((Number)val).doubleValue(), bs, startIndex);
    }
    
    public static int putBytesByDouble(ByteOrder byteOrder, double val, byte[] bs, int startIndex) {
    	return byteOrder == ByteOrder.BIG_ENDIAN ? putBytesByDoubleB(val, bs, startIndex) : putBytesByDoubleL(val, bs, startIndex);
    }
	
	public static int putBytesByDoubleB(double val, byte[] bs, int startIdex) {

		return putBytesByLongB(Double.doubleToLongBits(val), bs, startIdex);
	}
	
	public static int putBytesByDoubleL(double val, byte[] bs, int startIdex) {

		return putBytesByLongL(Double.doubleToLongBits(val), bs, startIdex);
	}

    // float to bytes
    
    public static byte[] toBytesByFloat(ByteOrder byteOrder, Object val) {
    	return toBytesByFloat(byteOrder, ((Number)val).floatValue());
    }
    
	public static byte[] toBytesByFloat(ByteOrder byteOrder, float val) {
		return byteOrder == ByteOrder.BIG_ENDIAN ? toBytesByFloatB(val) : toBytesByFloatL(val);
	}

    public static byte[] toBytesByFloatB(float val){

        return toBytesByIntB(Float.floatToIntBits(val));
    }

    public static float toFloatByBytesB(byte[] bs){
        int val = toIntByBytesB(bs);
        return Float.intBitsToFloat(val);
    }
    
    public static float toFloatByBytesB(byte[] bs, int startIndex){
    	int val = toIntByBytesB(bs, startIndex);
    	return Float.intBitsToFloat(val);
    }

    public static byte[] toBytesByFloatL(float val){

        return toBytesByIntB(Float.floatToIntBits(val));
    }

    public static float toFloatByBytesL(byte[] bs){
        int val = toIntByBytesB(bs);
        return Float.intBitsToFloat(val);
    }
    
    public static float toFloatByBytesL(byte[] bs, int startIndex){
    	int val = toIntByBytesB(bs, startIndex);
    	return Float.intBitsToFloat(val);
    }
    
    // >> put double
	
    public static int putBytesByFloat(ByteOrder byteOrder, Object val, byte[] bs, int startIndex) {
    	return putBytesByFloat(byteOrder, ((Number)val).floatValue(), bs, startIndex);
    }
    
    public static int putBytesByFloat(ByteOrder byteOrder, float val, byte[] bs, int startIndex) {
    	return byteOrder == ByteOrder.BIG_ENDIAN ? putBytesByFloatB(val, bs, startIndex) : putBytesByFloatL(val, bs, startIndex);
    }
    
	public static int putBytesByFloatB(float val, byte[] bs, int startIndex) {

		return putBytesByIntB(Float.floatToIntBits(val), bs, startIndex);
	}
	
	public static int putBytesByFloatL(float val, byte[] bs, int startIndex) {
		
		return putBytesByIntL(Float.floatToIntBits(val), bs, startIndex);
	}

    /**
     * 说明：保留小数点后位数		<br>
     * 时间：2017年6月1日 下午10:48:08
     *
     * @param val
     * @param px
     * @return
     */
    public static double toDoublePx(double val,int px){
        BigDecimal bd = new BigDecimal(val).setScale(px, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
