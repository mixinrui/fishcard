package com.boxfishedu.workorder.web.controller.studentrelated;

import com.boxfishedu.workorder.common.bean.SlotRuleEnum;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.dao.mongo.TimeLimitRulesMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.TimeLimitRules;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.BeanUtils;
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
        for (int i = 0; i < 7; i++) {
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("OVERALL");
            timeLimitRules1.setDay(i);
            timeLimitRules1.setFrom("19:00:00");
            timeLimitRules1.setTo("19:55:00");
            timeLimitRules1.setRule(SlotRuleEnum.MUTEX.toString());

            TimeLimitRules timeLimitRules2=new TimeLimitRules();
            BeanUtils.copyProperties(timeLimitRules1,timeLimitRules2);
            timeLimitRules2.setComboType("EXCHANGE");

            timeLimitRulesMorphiaRepository.save(timeLimitRules2);
            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
        }

        for (int i = 0; i < 7; i++) {
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("OVERALL");
            timeLimitRules1.setDay(i);
            timeLimitRules1.setFrom("20:00:00");
            timeLimitRules1.setTo("20:55:00");
            timeLimitRules1.setRule(SlotRuleEnum.MUTEX.toString());

            TimeLimitRules timeLimitRules2=new TimeLimitRules();
            BeanUtils.copyProperties(timeLimitRules1,timeLimitRules2);
            timeLimitRules2.setComboType("EXCHANGE");

            timeLimitRulesMorphiaRepository.save(timeLimitRules2);
            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
        }

        for (int i = 0; i < 7; i++) {
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("OVERALL");
            timeLimitRules1.setDay(i);
            timeLimitRules1.setFrom("21:00:00");
            timeLimitRules1.setTo("21:55:00");
            timeLimitRules1.setRule(SlotRuleEnum.MUTEX.toString());

            TimeLimitRules timeLimitRules2=new TimeLimitRules();
            BeanUtils.copyProperties(timeLimitRules1,timeLimitRules2);
            timeLimitRules2.setComboType("EXCHANGE");

            timeLimitRulesMorphiaRepository.save(timeLimitRules2);
            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
        }


        //周六,周日
        for (int i = 6; i < 8; i++) {
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("OVERALL");
            if (i == 7) {
                timeLimitRules1.setDay(0);
            } else {
                timeLimitRules1.setDay(i);
            }
            timeLimitRules1.setFrom("09:00:00");
            timeLimitRules1.setTo("09:55:00");
            timeLimitRules1.setRule(SlotRuleEnum.MUTEX.toString());

            TimeLimitRules timeLimitRules2=new TimeLimitRules();
            BeanUtils.copyProperties(timeLimitRules1,timeLimitRules2);
            timeLimitRules2.setComboType("EXCHANGE");

            timeLimitRulesMorphiaRepository.save(timeLimitRules2);
            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
        }

        for (int i = 6; i < 8; i++) {
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("OVERALL");
            if (i == 7) {
                timeLimitRules1.setDay(0);
            } else {
                timeLimitRules1.setDay(i);
            }
            timeLimitRules1.setFrom("10:00:00");
            timeLimitRules1.setTo("10:55:00");
            timeLimitRules1.setRule(SlotRuleEnum.MUTEX.toString());

            TimeLimitRules timeLimitRules2=new TimeLimitRules();
            BeanUtils.copyProperties(timeLimitRules1,timeLimitRules2);
            timeLimitRules2.setComboType("EXCHANGE");

            timeLimitRulesMorphiaRepository.save(timeLimitRules2);
            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
        }
        return JsonResultModel.newJsonResultModel("ok");
    }

    //http://127.0.0.1:8080/random_slot/OVERALL
    @RequestMapping(value = "/{combo_type}", method = RequestMethod.POST)
    public JsonResultModel clearRedis(@PathVariable("combo_type") String comboType) {
        for (int i = 0; i < 7; i++) {
            cacheManager.getCache(CacheKeyConstant.TIME_LIMIT_RULES_CACHE_KEY).evict(TimeLimitRules.getCacheKey(comboType, SlotRuleEnum.MUTEX.toString(), i));
        }
        return JsonResultModel.newJsonResultModel("ok");
    }

    @RequestMapping(value = "/init_range", method = RequestMethod.POST)
    public JsonResultModel initRange() {
        //////////////中教
        for (int i = 1; i < 6; i++) {
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("OVERALL");
            timeLimitRules1.setDay(i);
            timeLimitRules1.setLimitMini("19:00:00");
            timeLimitRules1.setLimitMax("23:55:00");
            timeLimitRules1.setRule(SlotRuleEnum.RANGE.toString());

            TimeLimitRules timeLimitRules2=new TimeLimitRules();
            BeanUtils.copyProperties(timeLimitRules1,timeLimitRules2);
            timeLimitRules2.setComboType("EXCHANGE");
            timeLimitRulesMorphiaRepository.save(timeLimitRules2);

            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
        }

        {
            //周六
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("OVERALL");
            timeLimitRules1.setDay(6);
            timeLimitRules1.setLimitMini("09:00:00");
            timeLimitRules1.setLimitMax("23:55:00");
            timeLimitRules1.setRule(SlotRuleEnum.RANGE.toString());

            TimeLimitRules timeLimitRules2 = new TimeLimitRules();
            BeanUtils.copyProperties(timeLimitRules1, timeLimitRules2);
            timeLimitRules2.setComboType("EXCHANGE");
            timeLimitRulesMorphiaRepository.save(timeLimitRules2);

            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
        }
        {
            //周日
            TimeLimitRules timeLimitRules2 = new TimeLimitRules();
            timeLimitRules2.setComboType("OVERALL");
            timeLimitRules2.setDay(0);
            timeLimitRules2.setLimitMini("09:00:00");
            timeLimitRules2.setLimitMax("23:55:00");
            timeLimitRules2.setRule(SlotRuleEnum.RANGE.toString());

            TimeLimitRules timeLimitRules3 = new TimeLimitRules();
            BeanUtils.copyProperties(timeLimitRules2, timeLimitRules3);
            timeLimitRules3.setComboType("EXCHANGE");
            timeLimitRulesMorphiaRepository.save(timeLimitRules3);

            timeLimitRulesMorphiaRepository.save(timeLimitRules2);
        }


        //////////////外教
        for (int i = 1; i < 6; i++) {
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("FOREIGN");
            timeLimitRules1.setDay(i);
            timeLimitRules1.setLimitMini("19:00:00");
            timeLimitRules1.setLimitMax("22:55:00");
            timeLimitRules1.setRule(SlotRuleEnum.RANGE.toString());

            //chinese
            TimeLimitRules timeLimitRules2=new TimeLimitRules();
            BeanUtils.copyProperties(timeLimitRules1,timeLimitRules2);
            timeLimitRules2.setComboType("CHINESE");

            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
            timeLimitRulesMorphiaRepository.save(timeLimitRules2);
        }

        { //周六
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("FOREIGN");
            timeLimitRules1.setDay(6);
            timeLimitRules1.setLimitMini("09:00:00");
            timeLimitRules1.setLimitMax("11:55:00");
            timeLimitRules1.setRule(SlotRuleEnum.RANGE.toString());

            TimeLimitRules timeLimitRules11=new TimeLimitRules();
            BeanUtils.copyProperties(timeLimitRules1,timeLimitRules11);
            timeLimitRules11.setComboType("CHINESE");

            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
            timeLimitRulesMorphiaRepository.save(timeLimitRules11);

            TimeLimitRules timeLimitRules2 = new TimeLimitRules();
            timeLimitRules2.setComboType("FOREIGN");
            timeLimitRules2.setDay(6);
            timeLimitRules2.setLimitMini("19:00:00");
            timeLimitRules2.setLimitMax("22:55:00");
            timeLimitRules2.setRule(SlotRuleEnum.RANGE.toString());

            TimeLimitRules timeLimitRules21=new TimeLimitRules();
            BeanUtils.copyProperties(timeLimitRules2,timeLimitRules21);
            timeLimitRules21.setComboType("CHINESE");

            timeLimitRulesMorphiaRepository.save(timeLimitRules2);
            timeLimitRulesMorphiaRepository.save(timeLimitRules21);
        }

        {
            //周日
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("FOREIGN");
            timeLimitRules1.setDay(0);
            timeLimitRules1.setLimitMini("09:00:00");
            timeLimitRules1.setLimitMax("11:55:00");
            timeLimitRules1.setRule(SlotRuleEnum.RANGE.toString());

            TimeLimitRules timeLimitRules11=new TimeLimitRules();
            BeanUtils.copyProperties(timeLimitRules1,timeLimitRules11);
            timeLimitRules11.setComboType("CHINESE");

            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
            timeLimitRulesMorphiaRepository.save(timeLimitRules11);

            TimeLimitRules timeLimitRules2 = new TimeLimitRules();
            timeLimitRules2.setComboType("FOREIGN");
            timeLimitRules2.setDay(0);
            timeLimitRules2.setLimitMini("19:00:00");
            timeLimitRules2.setLimitMax("22:55:00");
            timeLimitRules2.setRule(SlotRuleEnum.RANGE.toString());

            TimeLimitRules timeLimitRules21=new TimeLimitRules();
            BeanUtils.copyProperties(timeLimitRules2,timeLimitRules21);
            timeLimitRules21.setComboType("CHINESE");

            timeLimitRulesMorphiaRepository.save(timeLimitRules2);
            timeLimitRulesMorphiaRepository.save(timeLimitRules21);
        }
        return JsonResultModel.newJsonResultModel("ok");
    }

}
