package com.boxfishedu.workorder.common.redis;

import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCachePrefix;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.lang.reflect.Method;
import java.util.Map;

@SuppressWarnings("ALL")
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfiguration extends CachingConfigurerSupport {

    @Autowired
    private RedisProperties redisProperties;

    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMinIdle(1);
        jedisPoolConfig.setMaxTotal(200);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setLifo(true);
        factory.setPoolConfig(jedisPoolConfig);
        factory.setHostName(redisProperties.getHostName());
        factory.setPassword(redisProperties.getPassword());
        factory.setPort(redisProperties.getPort());
        factory.setTimeout(redisProperties.getTimeout()); //设置连接超时时间
        return factory;
    }

    @Bean
    public KeyGenerator redisKeyGenerator() {
        KeyGenerator redisKeyGenerator = (Object target, Method method, Object... params) -> {
            StringBuilder sb = new StringBuilder("BOXFISH_ONLINE_SERVICE-");
            sb.append(target.getClass().getName()).append(("-"));
            for (Object obj : params) {
                sb.append(obj.toString());
            }
            return sb.toString();
        };
        return redisKeyGenerator;
    }

    private class BoxFishRedisCachePrefix implements RedisCachePrefix {
        @Override
        public byte[] prefix(String cacheName) {
            return cacheName.getBytes();
        }
    }

    @Bean
    public CacheManager cacheManager(RedisTemplate redisTemplate) {
        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
        cacheManager.setCachePrefix(new BoxFishRedisCachePrefix());
        cacheManager.setUsePrefix(true);
        // 默认过期时间 24小时
        cacheManager.setDefaultExpiration(3600 * 24);

        Map<String, Long> expires = Maps.newHashMap();
        // 时间片有效期为2小时
        expires.put("timeSlots", 3600 * 2L);
        expires.put(DayTimeSlots.CACHE_KEY, 3600 * 2L);
        expires.put(CacheKeyConstant.NOTIFY_TEACHEZr_PREPARE_CLASS_KEY, 60 * 10l);
        expires.put(CacheKeyConstant.TIME_LIMIT_RULES_CACHE_KEY, 3600 * 24L);
        //  鱼卡后台 用户信息永久有效
        expires.put(CacheKeyConstant.FISHCARD_BACK_ORDER_USERINFO, 3600 * 24 * 30 * 12 * 15L);
        expires.put(CacheKeyConstant.NOTIFY_TEACHER_PREPARE_CLASS_KEY, 60 * 10l);
        // 防重复选时间缓存
        expires.put(CacheKeyConstant.WORKORDERS_REPEATED_SUBMISSION, 2L);
        //立即上课,缓存三分钟
        expires.put(CacheKeyConstant.WORKORDERS_REPEATED_SUBMISSION, 3 * 60L);
        // 选时间缓存一周
        expires.put(CacheKeyConstant.BASE_TIME_SLOTS, 3600 * 24 * 7L);
        // 外教点评次数缓存
        expires.put(CacheKeyConstant.COMMENT_CARD_AMOUNT, 3600 * 24L);
        // 公开课缓存一天
        expires.put(CacheKeyConstant.PUBLIC_CLASS_ROOM_WITH_LEVELANDDATE, 3600 * 24L);
        expires.put(CacheKeyConstant.PUBLIC_CLASS_ROOM_WITH_ID, 3600 * 24L);

        //学生骰子
        expires.put(CacheKeyConstant.SMALL_CLASS_HEART_BEAT_KEY, 3600 * 24L * 5);
        expires.put(CacheKeyConstant.TEACHER_OPERATION_KEY, 3600 * 24L * 5);
        expires.put(CacheKeyConstant.STUDENT_PICKED_KEY, 3600 * 24L * 5);
        cacheManager.setExpires(expires);
        return cacheManager;
    }

    @Bean(name = "teachingServiceRedisTemplate")
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate(factory);
        setSerializer(template); //设置序列化工具，这样ReportBean不需要实现Serializable接口
        template.afterPropertiesSet();
        return template;
    }

    @Bean(name = "stringLongRedisTemplate")
    public RedisTemplate<String, Long> stringLongTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Long> stringLongTemplate = new RedisTemplate<>();
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        stringLongTemplate.setKeySerializer(stringSerializer);
        stringLongTemplate.setConnectionFactory(factory);
        stringLongTemplate.afterPropertiesSet();
        ;
        return stringLongTemplate;
    }

    private void setSerializer(RedisTemplate template) {
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        template.setValueSerializer(jackson2JsonRedisSerializer);
    }
}