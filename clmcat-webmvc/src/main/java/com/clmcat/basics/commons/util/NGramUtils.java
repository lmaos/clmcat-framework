package com.clmcat.basics.commons.util;

import java.util.LinkedHashSet;
import java.util.Set;


/**
 * N-gram 工具类，用于将字符串切分为连续的 n 个字符组成的子串集合。
 * <p>
 * 常用于构建倒排索引、实现模糊搜索（如昵称搜索）等场景。
 * </p>
 */
public class NGramUtils {


    public static Set<String> ngram(String word, int n) {
        if (word == null ||  n <= 0) {
            throw new IllegalArgumentException("word or n should be greater than 0");
        }
        Set<String> set = new LinkedHashSet<>();
        if (word.length() == n) {
            set.add(word);
            return set;
        }
        if  (word.length() < n) {
            return set;
        }
        for (int i = n; i <= word.length(); i++){

            set.add(word.substring(i - n, i));
        }
        return set;
    }
    /*
    查询时候：

    SELECT user_id
        FROM inverted_index
        WHERE word IN (...)
        GROUP BY user_id
        HAVING COUNT(DISTINCT word) = k
        LIMIT 100

    k 是 in里面的 去重token数量。
     */
    public static void main(String[] args) {
        String value = "HelloWorld";
        System.out.println(value);
        System.out.println(ngram(value, 2));
    }
}
