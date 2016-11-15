package com.boxfishedu.workorder.servicex.instantclass.grabordervalidator;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.bean.instanclass.TeacherInstantClassStatus;
import com.boxfishedu.workorder.servicex.instantclass.instantvalidator.InstantClassValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hucl on 16/11/4.
 */
@Component
public class GrabInstantClassValidators {

    //参数校验->时间片验证->半小时内有课验证->课程表入口是否有课校验
    @Autowired
    private List<IGrabInstantClassValidator> preValidators;

    public TeacherInstantClassStatus preValidate() {
        for(IGrabInstantClassValidator preValidator:preValidators){
            TeacherInstantClassStatus result=preValidator.preValidate();
            switch (result){
                case UNKNOWN:
                    break;
                default:
                    return result;
            }
        }
        return TeacherInstantClassStatus.UNKNOWN;
    }
}
