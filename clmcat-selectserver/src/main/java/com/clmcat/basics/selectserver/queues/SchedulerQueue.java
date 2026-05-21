package com.clmcat.basics.selectserver.queues;

public interface SchedulerQueue {

    /**
     * publish message to topic
     * @param topic
     * @param message
     */
    void publish(String topic, String message);

    /** take message from topic
     *
     * @param topic name
     * @param seconds 秒
     * @return message or null
     */
    String take(String topic, int seconds);
    /**
     * 是否被使用中。
     */
    boolean isUsing(String masterName, String timerName);

    /**
     * 标记正在使用， 成功标记则 返回true
     */
    boolean use(String masterName, String timerName);

    /**
     * 标记不使用。
     */
    void unUse(String masterName, String timerName);
}
