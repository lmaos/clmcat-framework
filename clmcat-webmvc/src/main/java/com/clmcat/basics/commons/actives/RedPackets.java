package com.clmcat.basics.commons.actives;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class RedPackets {

	/**
	 * 分配算法,  比如 群发红包 发 1000金币, 最大允许100人获得, 最小获得金额 1, 最大获得金额  200, 分配均衡程度: 0.4适中
	 * 计算出来100人获得的金币数.  抢红包时候, 你需要将这个数放入 队列, 每个人从头取一个即可.
	 *  
	 * @param total      总数
	 * @param min        最小
	 * @param max        最大
	 * @param num        可以获得奖励的总人数
	 * @param groupAlloc 组分配, 保证分配均匀的程度 0 ~ 1.0 数越大整体趋近平均, 越小差异越大
	 * @return
	 */
	public static long[] alloc(long total, long min, long max, int num, double groupAlloc) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		max = Math.max(max, min); // 如果max < min 时候, 使用 min为主
		int realNum = (int) (total / min);
		realNum = Math.min(realNum, num);
		long calculateNum = total; // 计算的钱
		long[] values = new long[realNum]; //
		for (int i = 0; i < values.length; i++) {
			long allocNum = Math.min(calculateNum, min);
			values[i] = allocNum;
			calculateNum = calculateNum - allocNum;
		}
		if (realNum < num) { // 不够分的只给这些人.
			values[realNum - 1] += calculateNum;
			values[realNum - 1] = Math.min(values[realNum - 1], max);
		} else if (calculateNum > 0) {
			// 红包随机分配算法.
			int groupSize = (int) (realNum * groupAlloc); // 0 不分组
			groupSize = Math.max(groupSize, 1);
			int everyGroupNum = realNum / groupSize;
			long[] totalGroup = new long[groupSize];
			long totalItem = calculateNum / groupSize;
			for (int i = 0; i < groupSize; i++) {
				totalGroup[i] = totalItem;
			}
			totalGroup[groupSize - 1] += calculateNum % groupSize;
			int index = 0; // 当前Index
			for (int i = 0; i < groupSize; i++) {
				long currentGroupNum = totalGroup[i];
				int length = everyGroupNum;
				if (i == groupSize - 1) {
					length += realNum % groupSize; // 组人数+n
				}
				for (int j = 0; j < length && currentGroupNum > 0; j++, index++) {
					long allocValue = j == length - 1 ? currentGroupNum : random.nextLong(currentGroupNum) + 1;
					allocValue = Math.min(allocValue, max);
					values[index] += allocValue;
					currentGroupNum = currentGroupNum - allocValue;
					calculateNum = calculateNum - allocValue;
				}
			}

			if (calculateNum > 0) {
				long allocNum = calculateNum / values.length;
				for (int i = 0; i < values.length; i++) {
					values[i] = allocNum;
					if (i == values.length - 1) {
						values[i] += calculateNum % values.length;
					}
					values[i] = Math.min(values[i], max);
				}
			}
		}

		return values;
	}

	/**
	 * 分配算法,  比如 群发红包 发 1000金币, 最大允许100人获得, 最小获得金额 1, 分配均衡程度: 0.4适中
	 * 计算出来100人获得的金币数.  抢红包时候, 你需要将这个数放入 队列, 每个人从头取一个即可.
	 * @param total      总数
	 * @param min        最小
	 * @param num        可以获得奖励的总人数
	 * @param groupAlloc 组分配, 保证分配均匀的程度 0 ~ 1.0 数越大整体趋近平均, 越小差异越大
	 * @return
	 */
	public static long[] alloc(long total, long min, int num, double groupAlloc) {
		return alloc(total, min, Long.MAX_VALUE, num, groupAlloc);
	}

	public static void main(String[] args) {
		System.out.println(Arrays.toString(alloc(1000, 20, 6000, 10, 0.4)));
	}

}
