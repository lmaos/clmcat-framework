package com.clmcat.basics.commons.util;

public class SnowflakeSharder {

    /**
     * 雪花ID -> 高质量哈希 -> 安全取模 -> 表下标
     * @param snowflakeId 雪花ID
     * @param tableCount 分表数量（不要求2的幂）
     * @return 下标 0 ~ tableCount-1
     */
    public static int getTableIndex(long snowflakeId, int tableCount) {
        long hash = splitmix64(snowflakeId);
        // 安全取模（避免负数）
        return (int) ((hash & Long.MAX_VALUE) % tableCount);
    }

    /**
     * splitmix64 算法（优质、快速、无外部依赖）
     */
    public static long splitmix64(long x) {
        x = (x + 0x9e3779b97f4a7c15L) & Long.MAX_VALUE;
        x = (x ^ (x >>> 30)) * 0xbf58476d1ce4e5b9L;
        x = (x ^ (x >>> 27)) * 0x94d049bb133111ebL;
        x = x ^ (x >>> 31);
        return x;
    }
}