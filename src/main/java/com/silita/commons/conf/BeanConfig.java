package com.silita.commons.conf;

/**
 * Created by commons on 2017/1/28 0028.
 */
//@Configuration
public class BeanConfig {
//	//JedisPool pool;
//	private final String redisIp="127.0.0.1";
//
//	private int redisPort;
//
//	private final String pwd ="myredis";
//
//	@Bean
//	public JedisPoolConfig jedisPoolConfig() {
//		/**
//		 * <property name="maxActive" value="32"></property> <property
//		 * name="maxIdle" value="6"></property> <property name="maxWait"
//		 * value="15000"></property> <property name="minEvictableIdleTimeMillis"
//		 * value="300000"></property> <property name="numTestsPerEvictionRun"
//		 * value="3"></property> <property name="timeBetweenEvictionRunsMillis"
//		 * value="60000"></property> <property name="whenExhaustedAction"
//		 * value="1"></property>
//		 */
//		JedisPoolConfig jedis = new JedisPoolConfig();
//
//		jedis.setMaxTotal(10000);
//		jedis.setMaxIdle(6);
//		jedis.setMaxWaitMillis(150000);
//		jedis.setMinEvictableIdleTimeMillis(300000);
//		jedis.setNumTestsPerEvictionRun(3);
//		jedis.setTimeBetweenEvictionRunsMillis(60000);
//
//		return jedis;
//	}

//	@Bean
//	public JedisConnectionFactory jedisConnectionFactory(
//			JedisPoolConfig jedisPoolConfig) {
//		JedisConnectionFactory fac = new JedisConnectionFactory();
//		fac.setPoolConfig(jedisPoolConfig);
//		/**
//		 * <property name="poolConfig" ref="jedisPoolConfig"></property>
//		 * <property name="hostName" value="127.0.0.1"></property> <property
//		 * name="port" value="6379"></property> <property name="password"
//		 * value="0123456"></property> <property name="timeout"
//		 * value="15000"></property> <property name="usePool"
//		 * value="true"></property>
//		 */
//		fac.setHostName(redisIp);
//		fac.setPort(6379);
//		fac.setPassword(pwd);
//		fac.setTimeout(15000);
//		fac.setUsePool(true);
//
//		return fac;
//	}

//	@Bean
//	public RedisTemplate redisTemplate(
//			JedisConnectionFactory jedisConnectionFactory) {
//		RedisTemplate tml = new RedisTemplate();
//		tml.setConnectionFactory(jedisConnectionFactory);
//		tml.setDefaultSerializer(new StringRedisSerializer());
//
//		return tml;
//	}

//	@Bean
//	public RedisQueue redisQueue(RedisTemplate redisTemplate) {
//		String key = "liuqi";
//		RedisQueue rq = new RedisQueue(redisTemplate, key);
//		// RedisQueue<String> rq=new RedisQueue<String>();
//		rq.setKey("liuqi");
//		return rq;
//	}

//	@Bean
//	public Jedis jedis(JedisPool pool) {
//	    boolean broken = false;
//		Jedis jedis = null ;
//		try {
//			jedis = pool.getResource();
//			jedis.auth(pwd);
//		}catch(JedisConnectionException e) {
//		    pool.returnBrokenResource(jedis);
//		    //jedis = null;
//		    e.printStackTrace();
//		}catch (Exception e) {
//		    e.printStackTrace();
//		}
//		//finally {
//			//if(jedis != null){
//			//	pool.returnResource(jedis);
//			//}
//		//}
//		return jedis;
//	}
//
//	@Bean
//	public JedisPool jedisPool(JedisPoolConfig jedisPoolConfig) {
//		return new JedisPool(jedisPoolConfig, redisIp,
//				6379, 100000);
//	}


//	@Bean
//	public ArangoDB arangoDB() {
//		InputStream in = Notice.class.getResourceAsStream("arangodb.properties");
//    	ArangoDB arangoDB = new ArangoDB.Builder().loadProperties(in).build();
//		return arangoDB;
//	}
}
