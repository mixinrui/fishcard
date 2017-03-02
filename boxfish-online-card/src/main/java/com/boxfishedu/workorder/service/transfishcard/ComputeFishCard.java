package com.boxfishedu.workorder.service.transfishcard;

import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.util.DateUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by jiaozijun on 17/3/1.
 */

@Component
public class ComputeFishCard {

    @Autowired
    private ComputeFishCardOne2One computeFishCardOne2One;

    @Autowired
    private ComputeFishCardPublic computeFishCardPublic;

    @Autowired
    private ComputeFishCardSMALL computeFishCardSMALL;

    public void compute(){

        computeFishCardOne2One.compute();
        computeFishCardPublic.compute();
        computeFishCardSMALL.compute();

    }


}
