## 选主定时任务

### Master-time Bean 配置

```java

	// 选择主机配置 - 必须注入 用来进行主机选择的。
	@Bean
	SelectMaster apiRedisMaster(StringRedisTemplate redisTemplate) {
		return new RedisMaster(new RedisMasterTemplateLock("api-time-master", redisTemplate));
	}
	// 设置主任务 - 必须注入 用来执行任务的。
	@Bean
	MasterTimer apiMasterTimer(StringRedisTemplate redisTemplate) {
		ScheduledExecutorService task = Executors.newScheduledThreadPool(10, MasterTimerNamed.of("API_MASTER_TIMER-T"));
		return new MasterTimer(apiRedisMaster(redisTemplate), task);
	}
	
```

---

### 在Spring项目内使用

```java

public class MyTimer implements InitializingBean {
	@Autowired
	MasterTimer masterTimer;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// 创建 2秒的定时器
		masterTimer.addTimer(this::orderScanTimer, 2, TimeUnit.SECONDS);
		// 创建每2秒执行的定时器，条件通过才执行.  最后一个参数是条件。
		masterTimer.addTimer(this::orderScanTimer, 2, TimeUnit.SECONDS, () -> true );
		
		masterTimer.addTimer(this::myTimerCron, "0/2 * * * * ?");
	}

	private void orderScanTimer() {
		// 执行了
	}
	
	private void myTimer() {
	
	}
	
	private void myTimerCron() {
	
	}
	
}
```


### cron 表达式说明

```

cron 表达式由七个位置组成，空格分隔

* 秒 0~59

* 分 0~59

* 小时 0~23

* 天 1~31; 注意有的⽉份不⾜31天

* 月 0~11; 或者 JAN,FEB,MAR,APR,MAY,JUN,JUL,AUG,SEP,OCT,NOV,DEC

* 周 1~7; 1=SUN或者 SUN,MON,TUE,WEB,THU,FRI,SAT

* 年 1970~2099 可选项 一般很少填写

其中天和星期相互冲突，如果设置天 则星期设置 ?。

输入方向从左至右，通过空格分割。 秒 分 时 天 月 星期 年

 其中 * 代表所有，*/n ，n是间隔的时间，n1-n2 (代表范围)

例如:

*/1 * * * * ?   每1秒执行一次

*/10 * * * * ?   每10秒执行一次

1-20/2 * * * * ? 仅在时间的秒数为 1-20范围内，每2秒执行一次。例如2020-05-19 14:08:18 满足条件则执行。

* */1 * * * ? 每1分钟执行一次
————————————————
版权声明：本文为CSDN博主「张星宇」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/zxy2711352/article/details/106212827


```