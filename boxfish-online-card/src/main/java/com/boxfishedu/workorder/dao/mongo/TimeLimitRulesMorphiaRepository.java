package com.boxfishedu.workorder.dao.mongo;

import com.boxfishedu.workorder.common.bean.SlotRuleEnum;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mongo.TimeLimitRules;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class TimeLimitRulesMorphiaRepository extends BaseMorphiaRepository<TimeLimitRules> {

    @Autowired
    private CacheManager cacheManager;

    public static final String NONE="NONE";

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    public Optional<TimeLimitRules> getById(Long id) {
        Query<TimeLimitRules> query = datastore.createQuery(TimeLimitRules.class);
        query.criteria("id").equal(id);
        query.limit(1);
        return Optional.ofNullable(query.get());
    }

    public List<TimeLimitRules> queryByComboType(String comboType) {
        Query<TimeLimitRules> query = datastore.createQuery(TimeLimitRules.class);
        query.and(query.criteria("comboType").equal(comboType));
        return query.asList();
    }

    public Optional<List<TimeLimitRules>> queryByComboTypeAndRuleAndDay(String comboType, SlotRuleEnum slotRuleEnum, Integer day) {
        String limitRules = cacheManager.getCache(CacheKeyConstant.TIME_LIMIT_RULES_CACHE_KEY).get(TimeLimitRules.getCacheKey(comboType,slotRuleEnum.toString(), day), String.class);
        if (limitRules != null) {
            logger.debug("@queryByComboTypeAndDay#redis#limitules[{}]",limitRules);
            if(limitRules.equals(NONE)){
                return Optional.empty();
            }
            List<TimeLimitRules> limitRulesList = Lists.newArrayList();
            try {
                limitRulesList= new ObjectMapper().readValue(limitRules, new TypeReference<List<TimeLimitRules>>(){});
            }
            catch (Exception ex){
                logger.error("redis转换失败",ex);
            }
            return Optional.ofNullable(limitRulesList);
        }
        logger.debug("@queryByComboTypeAndDay#mongo#comboType[{}]#day[{}]",comboType,day);
        Query<TimeLimitRules> query = datastore.createQuery(TimeLimitRules.class);
        query.and(
                query.criteria("comboType").equal(comboType),
                query.criteria("rule").equal(slotRuleEnum.toString()),
                query.criteria("day").equal(day)
        );
        List<TimeLimitRules> timeLimitRules=query.asList();
        if(CollectionUtils.isEmpty(timeLimitRules)){
            cacheManager.getCache(CacheKeyConstant.TIME_LIMIT_RULES_CACHE_KEY).put(TimeLimitRules.getCacheKey(comboType,slotRuleEnum.toString(), day),NONE);
            return Optional.empty();
        }
        else{
            cacheManager.getCache(CacheKeyConstant.TIME_LIMIT_RULES_CACHE_KEY).put(TimeLimitRules.getCacheKey(comboType,slotRuleEnum.toString(), day),JacksonUtil.toJSon(timeLimitRules));
            return Optional.ofNullable(timeLimitRules);
        }
    }
}
