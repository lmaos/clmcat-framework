package com.clmcat.basics.commons.util;

public class ShardUtils {

    /**
     * 雪花ID -> 均匀哈希 -> 取表下标
     * @param snowflakeId 雪花ID
     * @param tableCount 分表数量（比如 16、32、64）
     * @return 表下标 0 ~ tableCount-1
     */
    public static int getTableIndex(long snowflakeId, int tableCount) {
        // 核心：MurmurHash 打散
        long hash = murmurHash64(snowflakeId);
        // 保证正数 + 取模
        return (int) Math.abs(hash % tableCount);
    }

    // MurmurHash64 实现（无依赖，直接复制用）
    private static long murmurHash64(long key) {
        long h = key;
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return h;
    }
}