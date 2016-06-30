package com.boxfishedu.workorder.dao.redis;

import com.boxfishedu.workorder.common.util.JacksonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisRepository {

    @Autowired
    private
    @Qualifier("teachingServiceRedisTemplate")
    StringRedisTemplate template;

    public String get(String id) {
        return template.boundValueOps(id).get();
    }

    public void set(String id, String content) {
        template.boundValueOps(id).set(content,24, TimeUnit.HOURS);
    }

    public <T> void getAndSet(String id,T t){
        String val= JacksonUtil.toJSon(t);
        template.boundValueOps(id).getAndSet(val);
    }

    public Boolean expire(String id){
        return template.expire(id,120,TimeUnit.SECONDS);
    }

    public void delete(String id){
        template.delete(id);
    }

    public <T> void set(String id,T t){
        String val= JacksonUtil.toJSon(t);
        this.set(id,val);
    }

    public <T> T get(String id,Class<T> t){
        String val=this.get(id);
        return JacksonUtil.readValue(val,t);
    }

    public boolean contain(String id) {
        return template.hasKey(id);
    }

    public List<String> getValues() {
        return template.opsForValue().multiGet(getKeys());
    }

    public Set<String> getKeys() {
        return template.keys("*");
    }

    public void clear() {
        template.getConnectionFactory().getConnection().flushDb();
    }

}