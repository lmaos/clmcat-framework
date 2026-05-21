package com.clmcat.basics.selectserver.selectmaster;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.time.DateFormatUtils;
import com.clmcat.basics.selectserver.mastertimer.MasterTimer;
import com.clmcat.basics.selectserver.selectmaster.redisselect.RedisMaster;
import com.clmcat.basics.selectserver.selectmaster.redisselect.RedisMasterTemplateLock;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class RedisMasterDemo {
	public static void main(String[] args) throws Exception {
		Logger logger = (Logger) LoggerFactory.getLogger("ROOT");
		logger.setLevel(Level.INFO);
		RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration("127.0.0.1", 6379);
		configuration.setDatabase(0);
		
		LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(configuration);
		connectionFactory.afterPropertiesSet();
		StringRedisTemplate redisTemplate = new StringRedisTemplate(connectionFactory);
		
		// Redis 选择主机
		RedisMaster master = new RedisMaster(new RedisMasterTemplateLock("nnn", redisTemplate));
		master.afterPropertiesSet();
		int i = new Random().nextInt(100);
		System.out.println("select---" + i);
		
		// 主机定时任务
		MasterTimer masterTimer = new MasterTimer(master, Executors.newScheduledThreadPool(10));
		masterTimer.afterPropertiesSet();
		String timerName = "test";
		// 添加一个定时任务 2秒执行一次
		masterTimer.addTimer(timerName, ()->{
			System.out.println(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss") + "-" + i);
		
		}, "0/2 * * * * ?");
//		masterTimer.addTimer(()->{
//			System.out.println("timer->" + i);
//		}, 2, TimeUnit.SECONDS);
		
		synchronized (masterTimer) {
			masterTimer.wait();
		}
		
	}
	public static void main_x(String[] args) throws Exception {
		RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration("127.0.0.1", 6379);
		configuration.setDatabase(0);
//		// 连接池配置
//		GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
//		genericObjectPoolConfig.setMaxIdle(10);
//		genericObjectPoolConfig.setMinIdle(10);
//		genericObjectPoolConfig.setMaxTotal(10);
//		genericObjectPoolConfig.setMaxWaitMillis(10000);
//
//		// redis客户端配置
//		LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration
//				.builder().commandTimeout(Duration.ofMillis(10000));
//
//		builder.shutdownTimeout(Duration.ofMillis(10000));
//		builder.poolConfig(genericObjectPoolConfig);
//		LettuceClientConfiguration lettuceClientConfiguration = builder.build();

		LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(configuration);
		connectionFactory.afterPropertiesSet();
		StringRedisTemplate redisTemplate = new StringRedisTemplate(connectionFactory);

		RedisMaster master = new RedisMaster(new RedisMasterTemplateLock("nnn", redisTemplate));
		master.afterPropertiesSet();
		int i = new Random().nextInt(100);
		System.out.println("select---" + i);
		while (true) {
			master.masterExecute(n -> {
				System.out.println("master--" + i);
			});
			Thread.sleep(1000);
		}
	}
}
