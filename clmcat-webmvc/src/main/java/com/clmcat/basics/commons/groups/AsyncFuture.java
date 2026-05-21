package com.clmcat.basics.commons.groups;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class AsyncFuture {
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
	