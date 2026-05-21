package com.clmcat.basics.commons.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 分组操作、和分区并行执行.
 * 
 * @author zhangxingyu
 *
 */
public class GroupMapUtils {
	
	/*  // 分组索引KEY必须重新实现 hashcode 和 equals
	    
		List<CommentCacheKey> cacheKeys = new ArrayList<CommentCacheKey>();
		cacheKeys.add(CommentCacheKey.of(123, 3444));
		cacheKeys.add(CommentCacheKey.of(434, 222));
		cacheKeys.add(CommentCacheKey.of(435, 3444));
		// 直接分组
		Map<Long, List<CommentCacheKey>> group_1 = group(cacheKeys, key->key.getMomentId());
		
		// 分组后得到新的结果
		Map<Long, List<Long>> group_2 = group(cacheKeys, key->key.getMomentId(), key->key.getCommentId());
		
		System.out.println(group_1);
		System.out.println(group_2);
		
	*/
	
	
	/*
	 * 并行分区执行.
	 * 例如: 集合分区,每50一组, 每组占用一个线程来完成数据收集. 这样多组可以在同一时间返回结果.
	 * 
	 * Map result = groupListParallelToMap(srcList, 50, itemList -> {
	 *  	Map map = new HashMap(); 
	 *  	// ...数据收集过程...   
	 *  
	 *  	return map; 
	 * }); 
	 * 
	 */
	
	
	/**
	 * 将集合进行分组
	 * @param <T>
	 * @param srcList
	 * @param itemLength 每一组的长度
	 * @return
	 */
	public static <T> List<List<T>> groupList(Iterable<T> srcList, int itemLength) {
		List<List<T>> list = new ArrayList<List<T>>();
		int size = 0;
		List<T> item = null;
		for (T t : srcList) {
			if (size % itemLength == 0) {
				item = new ArrayList<T>();
				list.add(item);
			}
			item.add(t);
			size++;
		}
		return list;
	}
	
	
	public static <I,B> Map<I, Integer> groupCountSize(Iterable<B> srcList, Function<B, I> indexFunction) {
		Map<I, Integer> map = new HashMap<I, Integer>();
		for (B bean : srcList) {
			// 分组索引
			I indexKey = indexFunction.apply(bean);	
			if (indexKey != null) { // 索引不等于 NULL 可以累加
				Integer total = map.get(indexKey);
				if (total == null) {
					total = 0;
				}
				map.put(indexKey, total + 1);
			}
		}
		return map;
	}
	
	public static <I,B> Map<I, Long> groupCountLongValue(Iterable<B> srcList, Function<B, I> indexFunction, Function<B, Long> valueFunction) {
		Map<I, Long> map = new HashMap<I, Long>();
		for (B bean : srcList) {
			// 分组索引
			I indexKey = indexFunction.apply(bean);	
			if (indexKey != null) { // 索引不等于 NULL 可以累加
				Long total = map.get(indexKey);
				if (total == null) {
					total = 0L;
				}
				Long curr = valueFunction.apply(bean);
				if (curr != null) {
					total += curr;
				}
				map.put(indexKey, total);
			}
		}
		return map;
	}
	
	public static <I,B> Map<I, Integer> groupCountIntValue(Iterable<B> srcList, Function<B, I> indexFunction, Function<B, Integer> valueFunction) {
		Map<I, Integer> map = new HashMap<I, Integer>();
		for (B bean : srcList) {
			// 分组索引
			I indexKey = indexFunction.apply(bean);	
			if (indexKey != null) { // 索引不等于 NULL 可以累加
				Integer total = map.get(indexKey);
				if (total == null) {
					total = 0;
				}
				Integer curr = valueFunction.apply(bean);
				if (curr != null) {
					total += curr;
				}
				map.put(indexKey, total);
			}
		}
		return map;
	}
	
	public static <I,B> Map<I, BigDecimal> groupCountBigDecimalValue(Iterable<B> srcList, Function<B, I> indexFunction, Function<B, BigDecimal> valueFunction) {
		Map<I, BigDecimal> map = new HashMap<I, BigDecimal>();
		for (B bean : srcList) {
			// 分组索引
			I indexKey = indexFunction.apply(bean);	
			if (indexKey != null) { // 索引不等于 NULL 可以累加
				BigDecimal total = map.get(indexKey);
				if (total == null) {
					total = BigDecimal.ZERO;
				}
				BigDecimal curr = valueFunction.apply(bean);
				if (curr != null) {
					total = total.add(curr);
				}
				map.put(indexKey, total);
			}
		}
		return map;
	}
	
	
	/**
	 * 分组 (分组索引KEY必须重新实现 hashcode 和 equals) <br>
	 * 
	 * list结构 {@code list: [{userId:10, accessCount:199}]      } <br/>
	 * 分组示范  {@code Map<Long, List<Long>> map = groupOne(list, n->n.getUserId(), n->n.getAccessCount()) }
	 * 
	 *  
	 * @param <I> 索引对象
	 * @param <B> 每一个BEAN
	 * @param srcList 源集合
	 * @param indexFunction 索引创建函数 分组索引必须重新实现 hashcode 和 equals
	 * @param resultFunction 结果创建函数
	 * @return
	 */
	public static <I,B,R> Map<I, List<R>> group(Iterable<B> srcList, Function<B, I> indexFunction, Function<B, R> resultFunction) {
		Map<I, List<R>> map = new HashMap<I, List<R>>();
		for (B bean : srcList) {
			// 分组索引
			I indexKey = indexFunction.apply(bean);	
			if (indexKey != null) { // 索引不等于NULL 可以通过
				List<R> list = map.get(indexKey);
				if (list == null) {
					list = new ArrayList<R>();
					map.put(indexKey, list); // 分组
				}
				// 定制分组结果
				R result = resultFunction.apply(bean);
				if (result != null) { // 分组的结果不等于 NULL 可以放入
					list.add(result);
				}
			}
		}
		
		return map;
	}
	
	/**
	 * 分组 (分组索引KEY必须重新实现 hashcode 和 equals)
	 * @param <I> 索引对象
	 * @param <B> 每一个BEAN
	 * @param srcList 源集合
	 * @param indexFunction 索引创建函数 分组索引必须重新实现 hashcode 和 equals
	 * @param resultFunction 结果创建函数
	 * @return
	 */
	public static <I,B,R> Map<I, List<R>> groups(Iterable<B> srcList, Function<B, List<I>> indexFunction, Function<B, R> resultFunction) {
		Map<I, List<R>> map = new HashMap<I, List<R>>();
		for (B bean : srcList) {
			// 分组索引
			List<I> indexKeys = indexFunction.apply(bean);	
			if (indexKeys != null) { // 索引不等于NULL 可以通过
				for (I indexKey : indexKeys) {
					List<R> list = map.get(indexKey);
					if (list == null) {
						list = new ArrayList<R>();
						map.put(indexKey, list); // 分组
					}
					// 定制分组结果
					R result = resultFunction.apply(bean);
					if (result != null) { // 分组的结果不等于 NULL 可以放入
						list.add(result);
					}
				}
			}
		}
		
		return map;
	}

	/**
	 * 分组 (分组索引KEY必须重新实现 hashcode 和 equals) <br>
	 * 
	 * list结构 {@code list<UserAccess>: [{userId:10, accessCount:199}]      } <br/>
	 * 分组示范  {@code Map<Long, List<Access>> map = groupOne(list, n->n.getUserId()) }
	 * 
	 * @param <I> 索引对象 
	 * @param <B> 每一个BEAN
	 * @param srcList 源集合
	 * @param indexFunction 索引创建函数 分组索引KEY必须重新实现 hashcode 和 equals
	 * @return
	 */
	public static <I,B> Map<I, List<B>> group(Iterable<B> srcList, Function<B, I> indexFunction) {
		Map<I, List<B>> map = new HashMap<I, List<B>>();
		for (B bean : srcList) {
			// 分组索引
			I indexKey = indexFunction.apply(bean);	
			if (indexKey != null) {
				List<B> list = map.get(indexKey);
				if (list == null) {
					list = new ArrayList<B>();
					map.put(indexKey, list);
				}
				list.add(bean);
			}
		}
		
		return map;
	}
	
	public static <I, B, R> Map<I, R> groupOne(Iterable<B> srcList, Function<B, I> indexFunction, Function<B, R> resultFunction) {
		Map<I, R> map = new HashMap<I, R>();
		for (B bean : srcList) {
			// 分组索引
			I indexKey = indexFunction.apply(bean);	
			if (indexKey != null) {
				R r = resultFunction.apply(bean);
				if (r != null) {
					map.put(indexKey, r);
				}
			}
		}
		
		return map;
	}
	/**
	 * 按照指定的 字段 进行分组, 值也可以直接获取特定的值.  <br/>
	 * 
	 * 
	 * list结构 {@code list: [{userId:10, accessCount:199}]      } <br/>
	 * 分组示范  {@code Map<Long, Long> map = groupOne(list, n->n.getUserId(), n->n.getAccessCount()) }
	 * 
	 * 
	 * @param <I>
	 * @param <B>
	 * @param <R>
	 * @param srcList
	 * @param indexFunction
	 * @param resultFunction
	 * @return
	 */
	public static <I, B, R> Map<I, R> groupOne(Collection<B> srcList, Function<B, I> indexFunction, Function<B, R> resultFunction) {
		return groupOne((Iterable<B>)srcList, indexFunction, resultFunction);
	}
	
	public static <I,B> Map<I, B> groupOne(Iterable<B> srcList, Function<B, I> indexFunction) {
		Map<I, B> map = new HashMap<I, B>();
		for (B bean : srcList) {
			// 分组索引
			I indexKey = indexFunction.apply(bean);	
			if (indexKey != null) {
				map.put(indexKey, bean);
			}
		}
		
		return map;
	}
	/**
	 * 按照指定的 字段 进行分组 <br/ >
	 * 
	 * list结构 {@code list<UserAccess> : [{userId:10, accessCount:199}]      } <br/>
	 * 分组示范  {@code Map<Long, UserAccess> map = groupOne(list, n->n.getUserId()) }
	 * 
	 * @param <I>
	 * @param <B>
	 * @param srcList   集合类
	 * @param indexFunction 分组字段函数  如果是自定义KEY需要实现 equals 和  hashcode 的方法.
	 * @return
	 */
	public static <I,B> Map<I, B> groupOne(Collection<B> srcList, Function<B, I> indexFunction) {
		return groupOne((Iterable<B>)srcList, indexFunction);
	}
	
	/**
	 * 批量分组
	 * @param <I>
	 * @param <B>
	 * @param srcList
	 * @param indexFunction
	 * @return
	 */
	public static <I,B> Map<I, List<B>> groups(Iterable<B> srcList, Function<B, List<I>> indexFunction) {
		Map<I, List<B>> map = new HashMap<I, List<B>>();
		for (B bean : srcList) {
			// 分组索引
			List<I> indexKeys = indexFunction.apply(bean);
			if (indexKeys != null) {
				for (I indexKey : indexKeys) {
					List<B> list = map.get(indexKey);
					if (list == null) {
						list = new ArrayList<B>();
						map.put(indexKey, list);
					}
					list.add(bean);
				}
			}
		}
		
		return map;
	}
	
	/**
	 * 分组
	 * @param <I> 索引对象
	 * @param <B> 每一个BEAN
	 * @param srcList 源集合
	 * @param indexFunction 索引创建函数
	 * @param resultFunction 结果创建函数
	 * @return
	 */
	public static <I,B,R> Map<I, List<R>> group(Collection<B> srcList, Function<B, I> indexFunction, Function<B, R> resultFunction) {
		return group((Iterable<B>)srcList, indexFunction, resultFunction);
	}
	
	/**
	 * 分组
	 * @param <I> 索引对象
	 * @param <B> 每一个BEAN
	 * @param srcList 源集合
	 * @param indexFunction 索引创建函数
	 * @return
	 */
	public static <I,B> Map<I, List<B>> group(Collection<B> srcList, Function<B, I> indexFunction) {
		return group((Iterable<B>)srcList, indexFunction);
	}
	/**
	 * 分组--分俩层组
	 * @param <I0> 第一层KEY
	 * @param <I1> 第二层KEY
	 * @param <B>
	 * @param srcList
	 * @param indexFunction0 第一层分组条件
	 * @param indexFunction1 第二层分组条件
	 * @return
	 */
	public static <I0,I1,B> Map<I0, Map<I1,List<B>>> groupMulti(Collection<B> srcList, Function<B, I0> indexFunction0, Function<B, I1> indexFunction1) {
		Map<I0, Map<I1,List<B>>> result = new HashMap<>();
		Map<I0, List<B>> group0 = group(srcList, indexFunction0);
		for (Entry<I0, List<B>> entry : group0.entrySet()) {
			Map<I1, List<B>> group1 = group(entry.getValue(), indexFunction1);
			result.put(entry.getKey(), group1);
		}
		return result;
	}
	/**
	 * 
	 * @param <I0> 第一层KEY
	 * @param <I1> 第二层KEY
	 * @param <B>  元数据类型
	 * @param <R>  结果
	 * @param srcList 元数据
	 * @param indexFunction0
	 * @param indexFunction1
	 * @param resultFunction
	 * @return
	 */
	public static <I0, I1, B, R> Map<I0, Map<I1,R>> groupMultiOne(Collection<B> srcList, Function<B, I0> indexFunction0, Function<B, I1> indexFunction1, Function<B, R> resultFunction) {
		Map<I0, Map<I1, R>> result = new HashMap<>();
		Map<I0, List<B>> group0 = group(srcList, indexFunction0);
		for (Entry<I0, List<B>> entry : group0.entrySet()) {
			Map<I1, R> group1 = groupOne(entry.getValue(), indexFunction1, resultFunction);
			result.put(entry.getKey(), group1);
		}
		return result;
	}
	
	public static <I0, I1, B> Map<I0, Map<I1, B>> groupMultiOne(Collection<B> srcList, Function<B, I0> indexFunction0, Function<B, I1> indexFunction1) {
		Map<I0, Map<I1, B>> result = new HashMap<>();
		Map<I0, List<B>> group0 = group(srcList, indexFunction0);
		for (Entry<I0, List<B>> entry : group0.entrySet()) {
			Map<I1, B> group1 = groupOne(entry.getValue(), indexFunction1);
			result.put(entry.getKey(), group1);
		}
		return result;
	}
	
	/**
	 * 分组--分俩层组
	 * @param <I0> 第一层KEY
	 * @param <I1> 第二层KEY
	 * @param <B>
	 * @param srcList
	 * @param indexFunction0 第一层分组条件
	 * @param indexFunction1 第二层分组条件
	 * @param resultFunction 最终输出的结果
	 * @return
	 */
	public static <I0,I1,B, R> Map<I0, Map<I1,List<R>>> groupMulti(Collection<B> srcList, Function<B, I0> indexFunction0, Function<B, I1> indexFunction1, Function<B, R> resultFunction) {
		Map<I0, Map<I1,List<R>>> result = new HashMap<>();
		Map<I0, List<B>> group0 = group(srcList, indexFunction0);
		for (Entry<I0, List<B>> entry : group0.entrySet()) {
			Map<I1, List<R>> group1 = group(entry.getValue(), indexFunction1, resultFunction);
			result.put(entry.getKey(), group1);
		}
		return result;
	}
	
	/**
	 * 分组--分三层组
	 * @param <I0> 第一层KEY
	 * @param <I1> 第二层KEY
	 * @param <I2> 第三次KEY
	 * @param <B>
	 * @param srcList
	 * @param indexFunction0 第一层分组条件
	 * @param indexFunction1 第二层分组条件
	 * @param indexFunction2 第三层分组条件
	 * @return
	 */
	public static <I0,I1,I2,B> Map<I0, Map<I1,Map<I2, List<B>>>> groupMulti3(Collection<B> srcList, Function<B, I0> indexFunction0, Function<B, I1> indexFunction1, Function<B, I2> indexFunction2) {
		Map<I0, Map<I1,Map<I2, List<B>>>> result = new HashMap<>();
		Map<I0, List<B>> group0 = group(srcList, indexFunction0);
		for (Entry<I0, List<B>> entry0 : group0.entrySet()) {
			
			Map<I1, Map<I2,List<B>>> result1 = new HashMap<>();
			Map<I1, List<B>> group1 = group(entry0.getValue(), indexFunction1);
			for (Entry<I1, List<B>> entry1 : group1.entrySet()) {
				Map<I2, List<B>> group2 = group(entry1.getValue(), indexFunction2);
				result1.put(entry1.getKey(), group2);	
			}
			result.put(entry0.getKey(), result1);
		}
		return result;
	}
	
	/**
	 * 分组--分三层组
	 * @param <I0> 第一层KEY
	 * @param <I1> 第二层KEY
	 * @param <I2> 第三次KEY
	 * @param <B>
	 * @param srcList
	 * @param indexFunction0 第一层分组条件
	 * @param indexFunction1 第二层分组条件
	 * @param indexFunction2 第三层分组条件
	 * @param resultFunction 最终输出的结果
	 * @return
	 */
	public static <I0,I1,I2,B, R> Map<I0, Map<I1,Map<I2, List<R>>>> groupMulti3(Collection<B> srcList, Function<B, I0> indexFunction0, Function<B, I1> indexFunction1, Function<B, I2> indexFunction2, Function<B, R> resultFunction) {
		Map<I0, Map<I1,Map<I2, List<R>>>> result = new HashMap<>();
		Map<I0, List<B>> group0 = group(srcList, indexFunction0);
		for (Entry<I0, List<B>> entry0 : group0.entrySet()) {
			
			Map<I1, Map<I2,List<R>>> result1 = new HashMap<>();
			Map<I1, List<B>> group1 = group(entry0.getValue(), indexFunction1);
			for (Entry<I1, List<B>> entry1 : group1.entrySet()) {
				Map<I2, List<R>> group2 = group(entry1.getValue(), indexFunction2, resultFunction);
				result1.put(entry1.getKey(), group2);	
			}
			result.put(entry0.getKey(), result1);
		}
		return result;
	}
	
	/**
	 * 并行执行线程.
	 */
	static ExecutorService exec = Executors.newCachedThreadPool(run->{
		Thread thread = new Thread(run, "group-exec");
		thread.setDaemon(true);
		return thread;
	});
	/**
	 * LIST分区、并行执行.
	 * <BR>
	 * 
	 * 例如: 集合分区,每50一组, 每组占用一个线程来完成数据收集. 这样多组可以在同一时间返回结果.
	 * <BR>
	 * 
	 * ```java
	 * <BR>
	 * Map<String, Object> result = 
	 * groupListParallelToMap(srcList, 50, itemList -> {
	 *   Map<String, Object> map = new HashMap();
	 *   
	 *   return map;
	 * });
	 * <BR>
	 * ```
	 * 
	 * 
	 * @param <K> 结果KEY
	 * @param <V> 结果类型
	 * @param <T> 集合的类型
	 * @param srcList    源集合
	 * @param itemLength 每组数量
	 * @param groupExec  每组执行函数
	 * @return
	 */
	public static <K,V,T> Map<K, V> groupListParallelToMap(Iterable<T> srcList, int itemLength, Function<List<T>, Map<K,V>> groupExec) {
		Map<K, V> map = new HashMap<K, V>();
		List<List<T>> groupList = groupList(srcList, itemLength);
		if (groupList.size() == 0) {
			return map;
		}
		// 如果只有1组,则直接在当前线程执行操作, 只有超过两组以上才会开启并行线程.
		if (groupList.size() == 1) {
			List<T> itemList = groupList.get(0); 
			Map<K, V> itemMap = groupExec.apply(itemList);
			if (itemMap != null && itemMap.size() > 0) {
				map.putAll(itemMap); // 结果收集-合并分组.
			}
			return map;
		}
		
		//
		//  分区并行执行  ----  减少 IO 时间.
		//
		List<Future<Map<K, V>>> futures = new ArrayList<Future<Map<K,V>>>();
		for (List<T> itemList : groupList) {
			Future<Map<K, V>> future = exec.submit(() -> {
				return groupExec.apply(itemList);
			});
			futures.add(future);
		}
		try {
			for (Future<Map<K, V>> future : futures) {
				Map<K, V> itemMap = future.get();
				if (itemMap != null && itemMap.size() > 0) {
					map.putAll(itemMap);
				}
			}
		} catch (Exception e) {
			throw new GroupParalleException("分组并行业务发生异常", e);
		}
		
		return map;
	}
	
	
	/**
	 * LIST分区、并行执行. 这个是groupListParallelToMap 的扩展方法, 如果MAP的值返回类型是一个集合,则合并进行集合
	 * <BR>
	 * 
	 * 例如: 集合分区,每50一组, 每组占用一个线程来完成数据收集. 这样多组可以在同一时间返回结果.
	 * <BR>
	 * 
	 * ```java
	 * <BR>
	 * Map<String, Object> result = 
	 * groupListParallelToMap(srcList, 50, itemList -> {
	 *   Map<String, Object> map = new HashMap();
	 *   
	 *   return map;
	 * });
	 * <BR>
	 * ```
	 * 
	 * 
	 * @param <K> 结果KEY
	 * @param <V> 结果类型
	 * @param <T> 集合的类型
	 * @param srcList    源集合
	 * @param itemLength 每组数量
	 * @param groupExec  每组执行函数
	 * @return
	 */
	public static <K,V,T> Map<K, V> groupListParallelToMaps(Iterable<T> srcList, int itemLength, Function<List<T>, Map<K,V>> groupExec) {
		Map<K, V> map = new HashMap<K, V>();
		List<List<T>> groupList = groupList(srcList, itemLength);
		if (groupList.size() == 0) {
			return map;
		}
		// 如果只有1组,则直接在当前线程执行操作, 只有超过两组以上才会开启并行线程.
		if (groupList.size() == 1) {
			List<T> itemList = groupList.get(0); 
			Map<K, V> itemMap = groupExec.apply(itemList);
			if (itemMap != null && itemMap.size() > 0) {
				map.putAll(itemMap); // 结果收集-合并分组.
			}
			return map;
		}
		
		//
		//  分区并行执行  ----  减少 IO 时间.
		//
		List<Future<Map<K, V>>> futures = new ArrayList<Future<Map<K,V>>>();
		for (List<T> itemList : groupList) {
			Future<Map<K, V>> future = exec.submit(() -> {
				return groupExec.apply(itemList);
			});
			futures.add(future);
		}
		try {
			for (Future<Map<K, V>> future : futures) {
				Map<K, V> itemMap = future.get();
				if (itemMap != null && itemMap.size() > 0) {
					
					for (Entry<K, V> entry : itemMap.entrySet()) {
						
						if (entry.getValue() == null) {
							continue;
						}
						K key = entry.getKey();
						V value = entry.getValue();
						V margeValue = map.get(key);
						
						if (value instanceof Collection) { // 如果是集合 进行内容集合合并
							Collection collection = null;
							if (margeValue == null) {
								collection = (Collection<?>)value.getClass().newInstance();
							} else if (!(margeValue instanceof Collection)) {
								collection = (Collection<?>)value.getClass().newInstance();
								collection.add(margeValue);
							} else {
								collection = (Collection<?>) margeValue;
							}
							collection.addAll((Collection<?>)value);
							margeValue = (V) collection;
						} else if (value instanceof Map) { // 如果是map 进行内容MAP合并
							Map mapValue = null;
							if (margeValue == null) {
								mapValue = (Map<?,?>)value.getClass().newInstance();
							} else if (!(margeValue instanceof Map)) {
								mapValue = (Map<?,?>)value.getClass().newInstance();
								mapValue.put(key, margeValue);
							} else {
								mapValue = (Map<?,?>) margeValue;
							}
							mapValue.putAll((Map<?,?>)value);
							margeValue = (V) mapValue;
						} else {
							margeValue = value;
						}
						
						
						map.put(key, margeValue);
					}
				}
			}
		} catch (Exception e) {
			throw new GroupParalleException("分组并行业务发生异常", e);
		}
		
		return map;
	}
	
	/**
	 * 分组执行 将集合按照每组itemLength长度 分多组 进行多线程执行.
	 * 
	 * @param <T>
	 * @param srcList    集合
	 * @param itemLength 分组每组最大值
	 * @param groupExec 分组执行
	 * @return
	 */
	public static <T> AsyncFuture groupListParallelExec(Iterable<T> srcList, int itemLength, Consumer<List<T>> groupExec) {
		List<List<T>> groupList = groupList(srcList, itemLength);
		if (groupList.size() == 0) {
			return new AsyncFuture();
		}
		//
		//  分区并行执行  ----  减少 IO 时间.
		//
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (List<T> itemList : groupList) {
			Future<?> future = exec.submit(() -> {
				groupExec.accept(itemList);
			});
			futures.add(future);
		}
		return new AsyncFuture(futures);
	}
	
	/**
	 * MAP 分组并行、  每个Map key 为一组进行多线程执行.
	 * @param <K>
	 * @param <V>
	 * @param srcMap
	 * @param groupExec
	 */
	public static <K, V> AsyncFuture groupMapParallelExec(Map<K, V> srcMap, BiConsumer<K,V> groupExec) {
		if (srcMap.size() == 0) {
			return new AsyncFuture();
		}
		//
		//  分区并行执行  ----  减少 IO 时间.
		//
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (Entry<K, V> itemEntry : srcMap.entrySet()) {
			Future<?> future = exec.submit(() -> {
				groupExec.accept(itemEntry.getKey(), itemEntry.getValue());
			});
			futures.add(future);
		}
		return new AsyncFuture(futures);
	}
	
	
	/** 分组List转为Map集合 (非并行执行.). 
	 * 如果并行请使用 :  {@link GroupMapUtils#groupListParallelToMap(Iterable, int, Function) } 
	 */
	public static <K,V,T> Map<K, V> groupListToMap(Iterable<T> srcList, int itemLength, Function<List<T>, Map<K,V>> groupExec) {
		Map<K, V> map = new HashMap<K, V>();
		List<List<T>> groupList = groupList(srcList, itemLength); // 集合分区
		if (groupList.size() == 0) {
			return map;
		}
		for (List<T> itemList : groupList) {
			Map<K, V> itemMap = groupExec.apply(itemList); // 分区执行并分组
			if (itemMap != null && itemMap.size() > 0) {
				map.putAll(itemMap); // 结果收集-合并分组.
			}
		}
		return map;
	}
	
	public static String join(List<? extends GroupKey> groupKeys, String split, String groupAround) {
		return join(groupKeys, split, groupAround, null, "?");
	}
	
	public static String join(List<? extends GroupKey> groupKeys, String split, String groupAround, String itemAround) {
		return join(groupKeys, split, groupAround, itemAround, "?");
	}
	/**
	 * GroupKey 输出方式 <br>
	 * groupKeys:[[1,4,6],[7,8,9]] <br>
	 * join(groupKeys, ",", "(?), "'?'", "?"); <br>
	 * 
	 * 结果: = ('1','4','6'),('7','8','9')
	 * 
	 * @param groupKeys
	 * @param split 每项的分隔符
	 * @param groupAround 组环绕  (?)
	 * @param itemAround  每组内的项环绕 '?'
	 * @param aroundChar 环绕符号 默认 ?
	 * @return
	 */
	public static String join(List<? extends GroupKey> groupKeys, String split, String groupAround, String itemAround, String aroundChar) {
		if (aroundChar == null || aroundChar.isEmpty()) {
			aroundChar = "?";
		}
		String groupLx = "";
		String groupRx = "";
		String itemLx = "";
		String itemRx = "";
		if (groupAround != null && !groupAround.isEmpty()) {
			int gindex = groupAround.indexOf(aroundChar);
			if (gindex != -1) {
				groupLx = groupAround.substring(0, gindex);
				groupRx = groupAround.substring(gindex + aroundChar.length());
			} else {
				groupLx = groupAround;
			}
		}
		if (itemAround != null && !itemAround.isEmpty()) {
			int iindex = itemAround.indexOf(aroundChar);
			if (iindex != -1) {
				itemLx = itemAround.substring(0, iindex);
				itemRx = itemAround.substring(iindex + aroundChar.length());
			} else {
				itemLx = itemAround;
			}
		}
		
		StringBuilder builder = new StringBuilder(100);
		for (GroupKey groupKey : groupKeys) {
			Object[] values = groupKey.values;
			if (values.length > 0) {
				builder.append(groupLx);
				for (Object object : values) {
					builder.append(itemLx).append(object).append(itemRx).append(split);
				}
				builder.replace(builder.length()-1, builder.length(), groupRx);
				builder.append(split);
			}
		}
		if (builder.length() == 0) {
			return "";
		} else {
			return builder.substring(0, builder.length() - 1);
		}
	}
	public static class AsyncFuture {
		List<Future<?>> futures;

		public AsyncFuture(List<Future<?>> futures) {
			this.futures = futures;
		}
		public AsyncFuture() {
			futures = new ArrayList<Future<?>>();
		}
		public void sync() {
			for (Future<?> future : futures) {
				try {
					future.get();
				} catch (Exception e) {
					throw new GroupParalleException("分组并行业务发生异常", e);
				}
			}
		}
	}
	/**
	 * 快速分组 KEY
	 * 
	 * @author zhangxingyu
	 *
	 */
	public static class GroupKey {
		private Object[] values;
		private String value;
		public GroupKey(Object[] values) {
			this.values = values;
			StringBuilder stringBuilder = new StringBuilder();
			for (Object object : values) {
				stringBuilder.append(object).append("-");
			}
			value = stringBuilder.toString();
		}
		
		public static GroupKey of(Object... values) {
			return new GroupKey(values);
		}
		@Override
		public int hashCode() {
			return value.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			
			if (obj instanceof GroupKey) {
				return value.equals(((GroupKey) obj).value);
			}
			return obj == this;
		}
		
		@SuppressWarnings("unchecked")
		public <T>T getObject(int i) {
			return (T)values[i];
		}
		
		public String getString(int i) {
			return values[i] == null ? null : String.valueOf(values[i]);
		}
		public int size() {
			return values.length;
		}
		@Override
		public String toString() {
			
			return Arrays.toString(values);
		}
	}
	
	/**
	 * 并行执行发生异常
	 * 
	 * @author zhangxingyu
	 *
	 */
	public static class GroupParalleException extends RuntimeException {

		private static final long serialVersionUID = 1L;


		public GroupParalleException(String message, Throwable cause) {
			super(message, cause);
		}

		public GroupParalleException(String message) {
			super(message);
		}

		public GroupParalleException(Throwable cause) {
			super(cause);
		}
		
	}
	
	public static void main(String[] args) {
		List<GroupKey> groupKeys = Arrays.asList(GroupKey.of(1,3), GroupKey.of(3,5));
		System.out.println(join(groupKeys, ",", "(?)", "'?'"));
	}
}
