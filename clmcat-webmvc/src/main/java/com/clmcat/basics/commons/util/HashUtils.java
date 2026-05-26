package com.clmcat.basics.commons.util;

public final class HashUtils {

    // ==================== 1. splitmix64（推荐，雪崩好，速度快） ====================
    public static long splitmix64(long x) {
        x = (x + 0x9e3779b97f4a7c15L);
        x = (x ^ (x >>> 30)) * 0xbf58476d1ce4e5b9L;
        x = (x ^ (x >>> 27)) * 0x94d049bb133111ebL;
        x = x ^ (x >>> 31);
        return x;
    }

    // ==================== 2. xorshift64*（极快，质量足够，适合性能敏感） ====================
    public static long xorshift64star(long x) {
        x ^= x >>> 12;
        x ^= x << 25;
        x ^= x >>> 27;
        return x * 0x2545F4914F6CDD1DL;
    }

    // ==================== 3. murmur64 风格（非标准，但打散效果好） ====================
    public static long murmur64Mix(long x) {
        x ^= x >>> 33;
        x *= 0xff51afd7ed558ccdL;
        x ^= x >>> 33;
        x *= 0xc4ceb9fe1a85ec53L;
        x ^= x >>> 33;
        return x;
    }

    // ==================== 4. 基于乘法 + 位移的快速哈希 ====================
    public static long hash64Shift(long x) {
        long h = x * 0x9e3779b97f4a7c15L;
        h = (h ^ (h >>> 30)) * 0xbf58476d1ce4e5b9L;
        h = (h ^ (h >>> 27)) * 0x94d049bb133111ebL;
        return h ^ (h >>> 31);
    }

    // ==================== 5. 更简单的 64 位混合（原版基础改良） ====================
    public static long mix64(long x) {
        x = (x ^ (x >>> 30)) * 0xbf58476d1ce4e5b9L;
        x = (x ^ (x >>> 27)) * 0x94d049bb133111ebL;
        x = x ^ (x >>> 31);
        return x;
    }

    /**
     * 为给定的 key 生成 n 个不同的 64 位哈希值（用于布隆过滤器或多哈希场景）。
     * 使用双哈希技巧：h(i) = h1 + i * h2（自然溢出），性能极高，且哈希间统计独立性良好。
     *
     * @param key 输入值（如雪花ID）
     * @param n   需要生成的哈希数量（>=1）
     * @return 长度为 n 的 long 数组，每个值均在 [0, 2^64-1] 范围内
     */
    public static long[] hashFamily(long key, int n) {
        if (n <= 0) {
            return new long[0];
        }
        long h1 = splitmix64(key);               // 基础哈希1
        long h2 = xorshift64star(h1);            // 基础哈希2（由 h1 派生，保证独立性）
        if (h2 == 0) {
            h2 = 1;                              // 避免步长为0导致所有结果相同
        }
        long[] result = new long[n];
        for (int i = 0; i < n; i++) {
            // 等差数列，自然溢出相当于模 2^64，分布均匀
            result[i] = h1 + i * h2;
        }
        return result;
    }
}