package com.boxfishedu.workorder.servicex.instantclass.instantvalidator;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hucl on 16/11/4.
 */
@Component
public class InstantClassValidators {
    @Autowired
    private List<InstantClassValidator> preValidators;

    public int preValidate(InstantRequestParam instantRequestParam) {
        for(InstantClassValidator preValidator:preValidators){
            int result=preValidator.preValidate(instantRequestParam);
            if(result > InstantClassRequestStatus.UNKNOWN.getCode()) {
                return result;
            }
        }
        return 0;
    }
}
