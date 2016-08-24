package com.boxfishedu.workorder.web.controller.studentrelated;

import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.dao.mongo.TimeLimitRulesMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.TimeLimitRules;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hucl on 16/8/23.
 * 仅供测试使用
 */
@CrossOrigin
@RestController
@RequestMapping("/random_slot")
@SuppressWarnings("ALL")
public class RandomSlotController {
    @Autowired
    private TimeLimitRulesMorphiaRepository timeLimitRulesMorphiaRepository;
    @Autowired
    private CacheManager cacheManager;

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public JsonResultModel init() {
        for(int i=0;i<7;i++) {
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("OVERALL");
            timeLimitRules1.setDay(i);
            timeLimitRules1.setFrom("19:00:00");
            timeLimitRules1.setTo("19:55:00");
            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
        }

        for(int i=0;i<7;i++) {
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("OVERALL");
            timeLimitRules1.setDay(i);
            timeLimitRules1.setFrom("20:00:00");
            timeLimitRules1.setTo("20:55:00");
            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
        }

        for(int i=0;i<7;i++) {
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("OVERALL");
            timeLimitRules1.setDay(i);
            timeLimitRules1.setFrom("21:00:00");
            timeLimitRules1.setTo("21:55:00");
            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
        }


        //周六,周日
        for(int i=6;i<8;i++) {
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("OVERALL");
            if(i==7) {
                    timeLimitRules1.setDay(0);
                }
                else{
                    timeLimitRules1.setDay(i);
            }
            timeLimitRules1.setFrom("09:00:00");
            timeLimitRules1.setTo("09:55:00");
            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
        }

        for(int i=6;i<8;i++) {
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("OVERALL");
            if(i==7) {
                timeLimitRules1.setDay(0);
            }
            else{
                timeLimitRules1.setDay(i);
            }
            timeLimitRules1.setFrom("10:00:00");
            timeLimitRules1.setTo("10:55:00");
            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
        }
        return JsonResultModel.newJsonResultModel("ok");
    }

    //http://127.0.0.1:8080/random_slot/OVERALL
    @RequestMapping(value = "/{combo_type}", method = RequestMethod.POST)
    public JsonResultModel clearRedis(@PathVariable("combo_type") String comboType){
        for (int i=0;i<7;i++) {
            cacheManager.getCache(CacheKeyConstant.TIME_LIMIT_RULES_CACHE_KEY).evict(TimeLimitRules.getCacheKey(comboType,i));
        }
        return JsonResultModel.newJsonResultModel("ok");
    }
}
