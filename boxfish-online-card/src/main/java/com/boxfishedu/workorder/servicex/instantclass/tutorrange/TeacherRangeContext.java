package com.boxfishedu.workorder.servicex.instantclass.tutorrange;

import com.boxfishedu.workorder.dao.mongo.InstantClassTimeRulesMorphiaRepository;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.servicex.instantclass.bean.TeacherInstantRangeBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by hucl on 16/12/21.
 */
@Component
public class TeacherRangeContext {
    @Autowired
    private Map<String, ITutorRangeStrategy> tutorRangeStrategyMap;

    //目前中外教一样,使用外教
    public TeacherInstantRangeBean teacherTimeRange(TeacherStudentRequester teacherStudentRequester
            , InstantClassTimeRulesMorphiaRepository instantClassTimeRulesMorphiaRepository, Long teacherId) {
        return tutorRangeStrategyMap.get(InstantRangeEnum.FRN_RANGE)
                .teacherTimeRange(teacherStudentRequester, instantClassTimeRulesMorphiaRepository, teacherId);

    }
}
