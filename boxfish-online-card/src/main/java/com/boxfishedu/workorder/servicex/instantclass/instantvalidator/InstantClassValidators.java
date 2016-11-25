package com.boxfishedu.workorder.servicex.instantclass.instantvalidator;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hucl on 16/11/4.
 */
@Component
public class InstantClassValidators {

    //参数校验->时间片验证->半小时内有课验证->课程表入口是否有课校验
    @Autowired
    private List<InstantClassValidator> preValidators;

    public int preValidate() {
        for(InstantClassValidator preValidator:preValidators){
            int result=preValidator.preValidate();
            if(result > InstantClassRequestStatus.UNKNOWN.getCode()) {
                return result;
            }
        }
        return InstantClassRequestStatus.UNKNOWN.getCode();
    }
}
