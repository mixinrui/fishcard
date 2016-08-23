package com.boxfishedu.workorder.web.controller.studentrelated;

import com.boxfishedu.workorder.dao.mongo.TimeLimitRulesMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.TimeLimitRules;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by hucl on 16/8/23.
 */
@CrossOrigin
@RestController
@RequestMapping("/random_slot")
@SuppressWarnings("ALL")
public class RandomSlotController {
    @Autowired
    private TimeLimitRulesMorphiaRepository timeLimitRulesMorphiaRepository;

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public JsonResultModel init() {
        for(int i=0;i<7;i++) {
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("OVERALL");
            timeLimitRules1.setDay(i+1);
            timeLimitRules1.setFrom("19:00:00");
            timeLimitRules1.setTo("19:55:00");
            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
        }

        for(int i=0;i<7;i++) {
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("OVERALL");
            timeLimitRules1.setDay(i+1);
            timeLimitRules1.setFrom("20:00:00");
            timeLimitRules1.setTo("20:55:00");
            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
        }

        for(int i=0;i<7;i++) {
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("OVERALL");
            timeLimitRules1.setDay(i+1);
            timeLimitRules1.setFrom("21:00:00");
            timeLimitRules1.setTo("21:55:00");
            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
        }

        for(int i=5;i<7;i++) {
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("OVERALL");
            timeLimitRules1.setDay(i+1);
            timeLimitRules1.setFrom("09:00:00");
            timeLimitRules1.setTo("09:55:00");
            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
        }

        for(int i=5;i<7;i++) {
            TimeLimitRules timeLimitRules1 = new TimeLimitRules();
            timeLimitRules1.setComboType("OVERALL");
            timeLimitRules1.setDay(i+1);
            timeLimitRules1.setFrom("10:00:00");
            timeLimitRules1.setTo("10:55:00");
            timeLimitRulesMorphiaRepository.save(timeLimitRules1);
        }

        return JsonResultModel.newJsonResultModel("ok");
    }
}
