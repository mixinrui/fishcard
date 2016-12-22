package com.boxfishedu.workorder.servicex.instantclass.tutorrange;

import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.mongo.InstantClassTimeRulesMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.InstantClassTimeRules;
import com.boxfishedu.workorder.servicex.instantclass.bean.TeacherInstantRangeBean;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * †
 * Created by hucl on 16/12/21.
 */
@Component(InstantRangeEnum.CN_RANGE)
public class CNRangeStrategy implements ITutorRangeStrategy {

    @Autowired
    private InstantClassTimeRulesMorphiaRepository instantClassTimeRulesMorphiaRepository;


    @Override
    public Optional<List<InstantClassTimeRules>> getInstantTimeList() {
        Optional<List<InstantClassTimeRules>> instantClassTimeRulesList
                = instantClassTimeRulesMorphiaRepository.getByDay(DateUtil.date2SimpleString(new Date()), TutorTypeEnum.CN.toString());
        return instantClassTimeRulesList;
    }

    @Override
    public String timeRangeKey() {
        return String.format("range:%s:%s", TutorTypeEnum.CN.toString(), DateUtil.date2SimpleString(new Date()));
    }
}
