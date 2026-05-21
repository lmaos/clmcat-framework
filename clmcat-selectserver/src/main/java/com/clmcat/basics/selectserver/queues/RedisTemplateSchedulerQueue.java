package com.clmcat.basics.selectserver.queues;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import com.clmcat.basics.commons.util.ThreadNamed;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author zhangxingyu
 */
@Slf4j
public class RedisTemplateSchedulerQueue implements SchedulerQueue {

    private StringRedisTemplate redis;
    private static ScheduledExecutorService keepExec = Executors.newScheduledThreadPool(2, ThreadNamed.of("SchedulerUseKeepThread"));
    private Map<String, ScheduledFuture<?>> keepMap = new ConcurrentHashMap<>();
    public RedisTemplateSchedulerQueue(StringRedisTemplate redis) {
        this.redis = redis;
    }
    @Override
    public void publish(String topic, String message) {
        redis.opsForList().rightPush(topic + ".topic", message);
    }

    @Override
    public String take(String topic, int seconds) {
        try {
            return redis.opsForList().leftPop(topic + ".topic", seconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    @Override
    public boolean isUsing(String masterName, String timerName) {
        return redis.opsForValue().get(masterName + "." + timerName) != null;
    }

    @Override
    public boolean use(String masterName, String timerName) {
        String redisKey = masterName + "." + timerName;
        String value = RandomStringUtils.random(32, "1234567890QWERTYUIOPASDFGHJKLZXCVBNM");
        if (redis.opsForValue().setIfAbsent(redisKey, value, 20, TimeUnit.SECONDS) != null) {
            /// 保持存活的方式。当定时任务的线程被持续运行，则持续保持，防止其他线程占用导致并发， 如果极短时间线程执行完成则保持线程也会被快速释放。
            ScheduledFuture<?> schedule = keepExec.scheduleWithFixedDelay(() -> {
                String oldvalue = redis.opsForValue().get(redisKey);
                if (oldvalue == null || !oldvalue.equals(value)) {
                    ScheduledFuture<?> remove = keepMap.remove(redisKey);
                    if (remove != null) {
                        remove.cancel(true);
                    }
                } else {
                    redis.expire(redisKey, 20, TimeUnit.SECONDS);
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

    @Override
    public void unUse(String masterName, String timerName) {
        ScheduledFuture<?> remove = keepMap.remove(masterName + "." + timerName);
        if (remove != null) {
            remove.cancel(true);
        }
        redis.delete(masterName + "." + timerName);
    }
}
