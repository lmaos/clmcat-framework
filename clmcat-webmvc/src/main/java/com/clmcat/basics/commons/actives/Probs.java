package com.clmcat.basics.commons.actives;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.clmcat.basics.commons.util.locks.SingleLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Probs {
	static Logger log = LoggerFactory.getLogger(Probs.class);
	private List<ProbEntity> entities = new ArrayList<>();
	private List<ProbEntity> selects;
	private SingleLock lock = new SingleLock();
	private int density = 9;

	public static Probs of() {
		return new Probs();
	}

	/**
	 * 设置随机的密度. 颗粒度越密集,随机的比例越准确.
	 */
	public Probs setDensity(int density) {
		this.density = Math.min(density, 11);
		return this;
	}

	public Probs add(double rate, Object value) {
		return add(BigDecimal.valueOf(rate), value);
	}

	public Probs add(BigDecimal rate, Object value) {
		lock.synchronizedLockNotThrows(() -> {
			entities.add(new ProbEntity(value, rate));
			selects = null;
		});
		return this;
	}

	/**
	 * 随机获取一个
	 * 
	 * @return
	 */
	public Object get() {
		List<ProbEntity> selects = this.selects;
		if (selects == null) {
			try {
				selects = lock.synchronizedLock(() -> {
					if (this.selects != null) {
						return this.selects;
					}
					this.selects = new ArrayList<>(entities.size());
					if (entities.size() == 0) {
						return this.selects;
					}
					BigDecimal total = BigDecimal.ZERO;
					for (ProbEntity probEntity : entities) {
						total = total.add(probEntity.rate);
					}
					long count = 0;
					for (ProbEntity probEntity : entities) {
						BigDecimal rate = probEntity.rate.divide(total, density, BigDecimal.ROUND_HALF_UP);
						long num = rate.multiply(BigDecimal.valueOf(10).pow(density)).longValue();
						if (num > 0) {
							probEntity.num = num + count;
							count = probEntity.num;
							this.selects.add(probEntity);
						}
					}
					return this.selects;
				});
			} catch (Exception e) {
				log.error("操作失败-随机数初始化", e);
				throw new RuntimeException("操作失败-随机数初始化", e);
			}
		}
		if (selects.size() > 0) {
			long densitynum = selects.get(selects.size() - 1).num;
			long index = ThreadLocalRandom.current().nextLong(densitynum);
			for (ProbEntity probEntity : selects) {
				if (probEntity.num > index) {
					return probEntity.value;
				}
			}
		}
		return null;
	}

	private static class ProbEntity {

		private long num;
		private Object value;
		private BigDecimal rate;

		public ProbEntity(Object value, BigDecimal rate) {
			this.value = value;
			this.rate = rate;
		}

	}

	public static void main(String[] args) {
		Counts<Integer> counts = Counts.of();
		Probs p = Probs.of().add(0.001, 1).add(0.299, 2).add(0.5, 3).add(0.2, 4);
		for (int i = 0; i < 100000; i++) {
			int num = (int) p.get();
			counts.incr(num);
		}
		System.out.println(counts.getCounts());
	}
}
