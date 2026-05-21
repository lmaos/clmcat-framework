package com.clmcat.basics.selectserver.queues;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import com.clmcat.basics.commons.util.ThreadNamed;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

@Slf4j
public class JedisSchedulerQueue implements SchedulerQueue {

    private Supplier<Jedis> redis;
    private static ScheduledExecutorService keepExec = Executors.newScheduledThreadPool(2, ThreadNamed.of("SchedulerUseKeepThread"));
    private Map<String, ScheduledFuture<?>> keepMap = new ConcurrentHashMap<>();
    public JedisSchedulerQueue(Supplier<Jedis> redis) {
        this.redis = redis;
    }



    @Override
    public void publish(String topic, String message) {
        try (Jedis jedis = redis.get()) {
            jedis.rpush(topic + ".topic", message);
        }
    }

    @Override
    public String take(String topic, int seconds) {
        try (Jedis jedis = redis.get()) {
            List<String> blpop = jedis.blpop(seconds, topic + ".topic");

            if (blpop == null || blpop.isEmpty()) {
                return null;
            }

            return blpop.get(1);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    @Override
    public boolean isUsing(String masterName, String timerName) {
        try (Jedis jedis = redis.get()) {
            return jedis.get(masterName + "." + timerName) != null;
        }
    }

    @Override
    public boolean use(String masterName, String timerName) {
        try (Jedis jedis = redis.get()) {
            String redisKey = masterName + "." + timerName;
            String value = RandomStringUtils.random(32, "1234567890QWERTYUIOPASDFGHJKLZXCVBNM");
            if (jedis.set(redisKey, value, SetParams.setParams().nx().ex(20)) != null) {
                /// 保持存活的方式。当定时任务的线程被持续运行，则持续保持，防止其他线程占用导致并发， 如果极短时间线程执行完成则保持线程也会被快速释放。
                ScheduledFuture<?> schedule = keepExec.scheduleWithFixedDelay(() -> {
                    String oldvalue = jedis.get(redisKey);
                    if (oldvalue == null || !oldvalue.equals(value)) {
                        ScheduledFuture<?> remove = keepMap.remove(redisKey);
                        if (remove != null) {
                            remove.cancel(true);
                        }
                    } else {
                        /// 继续保持存活。
                        jedis.expire(redisKey, 20);
                    }
                }, 9, 9, TimeUnit.SECONDS);
                /// 保持存活
                ScheduledFuture<?> put = keepMap.put(redisKey, schedule);
                if (put != null) {
                    put.cancel(true);
                }
                return true;
            } else {
                ///  其他线程使用中
                return false;
            }
        }
    }

    @Override
    public void unUse(String masterName, String timerName) {
        ///  释放
        ScheduledFuture<?> remove = keepMap.remove(masterName + "." + timerName);
        if (remove != null) {
            remove.cancel(true);
        }
        try (Jedis jedis = redis.get()) {
            jedis.del(masterName + "." + timerName);
        }
    }
}
