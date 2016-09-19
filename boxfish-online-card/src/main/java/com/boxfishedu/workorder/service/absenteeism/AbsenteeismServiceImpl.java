package com.boxfishedu.workorder.service.absenteeism;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.service.absenteeism.sdk.AbsenteeismSDK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by ansel on 16/9/14.
 */

@Service
public class AbsenteeismServiceImpl implements AbsenteeismService{

    private static final long DEDUCT_SCORE = 30000l;

    private Logger logger = LoggerFactory.getLogger(AbsenteeismServiceImpl.class);

    @Autowired
    AbsenteeismSDK absenteeismSDK;

    @Override
    public JsonResultModel absenteeismDeductScore(Long studentId){
        logger.info("@AbsenteeismServiceImpl Student played truant. Deducting score ...");
        return absenteeismSDK.absenteeismDeductScore(studentId,DEDUCT_SCORE);
    }
}
