package com.clmcat.basics.selectserver.distributedtimer;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import com.clmcat.basics.selectserver.mastertimer.MasterTimer;
import com.clmcat.basics.selectserver.queues.SchedulerQueue;
import com.clmcat.basics.selectserver.selectmaster.SelectMaster;

import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 分布式主定时器。
 * @author zhangxingyu
 */
@Slf4j
public class DistributedMasterTimer extends MasterTimer {
    /**
     *
     * @param selectMaster   选主
     * @param schedulerQueue 调度队列
     * @param task           定时任务
     */
    public DistributedMasterTimer(SelectMaster selectMaster, SchedulerQueue schedulerQueue, ScheduledExecutorService task) {
        super(selectMaster, task);
        this.schedulerQueue = schedulerQueue;
    }

    private SchedulerQueue schedulerQueue;

    private Map<String, DistributedSchedulerRunner> distributedSchedulerRunnerMap = new ConcurrentHashMap<>();

    private Thread subscribeThread = new Thread(this::subscribe, "distributed-master-subscribe-timer");
    {
        subscribeThread.setDaemon(true);
    }
    /// 执行时间超过这么久则无效。毫秒。比如消费时候， 要求执行的时间在 10:00， 设置超过30分钟则不执行， 则10:30之后不执行， 在之前执行。
    @Setter
    private long executeInvalidTime = -1;


    @Override
    public void addTimer(String timerName, Runnable run, int delay, TimeUnit timeUnit) {
        DistributedSchedulerRunner distributedSchedulerRunner = convertSchedulerRunner(timerName, run);
        super.addTimer(timerName, distributedSchedulerRunner, delay, timeUnit);
    }


    @Override
    public void addTimer(String timerName, Runnable run, String cron, TimeZone timeZone) {
        DistributedSchedulerRunner distributedSchedulerRunner = convertSchedulerRunner(timerName, run);
        super.addTimer(timerName, distributedSchedulerRunner, cron, timeZone);
    }

    private DistributedSchedulerRunner convertSchedulerRunner(String timerName, Runnable run) {
        DistributedSchedulerRunner distributedSchedulerRunner = new DistributedSchedulerRunner(getMasterName(), timerName, run, schedulerQueue);
        distributedSchedulerRunnerMap.put(timerName, distributedSchedulerRunner);
        return distributedSchedulerRunner;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        subscribeThread.start();
    }
    /// 订阅
    private void subscribe() {
        try {
            String masterName = getMasterName();
            while (isEnable()) {
                String format = this.schedulerQueue.take(masterName, 10);

                String timerName;
                /// 目标在这个点进行执行。 时间戳，ms
                long execTime = 0;
                if (format.startsWith("V:")) {
                    int splitIndex = format.indexOf("-");
                    timerName = format.substring(2, splitIndex);
                    execTime = Long.parseLong(format.substring(splitIndex + 1));
                } else {
                    log.error("分布式定时器订阅异常，格式错误：{}", format);
                    continue;
                }

                /// 超过指定时间则无效。
                if (executeInvalidTime > 0 &&  System.currentTimeMillis() - execTime > executeInvalidTime) {
                    continue;
                }

                DistributedSchedulerRunner distributedSchedulerRunner = distributedSchedulerRunnerMap.get(timerName);
                /// 可以使用这个任务则执行。


                if (distributedSchedulerRunner != null) {
                    /// 执行调度器任务
                    if (schedulerQueue.use(masterName, timerName)) {
                        getTask().execute(() -> {
                            try {
                                distributedSchedulerRunner.getRun().run();
                            } finally {
                                 schedulerQueue.unUse(masterName, timerName);
                            }
                        });
                    }
                } else {
                    log.error("分布式定时器订阅异常，未找到定时器任务：{}", timerName);
                }

            }

        } catch (Exception e) {
            log.error("分布式定时器订阅异常",e);
        }
    }
}
