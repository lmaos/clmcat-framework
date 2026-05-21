package com.clmcat.basics.selectserver.distributedtimer;

import lombok.Getter;
import com.clmcat.basics.selectserver.queues.SchedulerQueue;

/**
 * @author zhangxingyu
 */
public class DistributedSchedulerRunner implements Runnable {
    @Getter
    private String masterName;
    @Getter
    private String timerName;
    @Getter
    private Runnable run;
    /// 调度的队列
    private SchedulerQueue schedulerQueue;

    public DistributedSchedulerRunner(String masterName, String timerName, Runnable run, SchedulerQueue schedulerQueue) {
        this.masterName = masterName;
        this.timerName = timerName;
        this.run = run;
        this.schedulerQueue = schedulerQueue;
    }

    @Override
    public void run() {
        String message = "V:" + timerName + "-" + System.currentTimeMillis();
        schedulerQueue.publish(masterName, message);
    }
}