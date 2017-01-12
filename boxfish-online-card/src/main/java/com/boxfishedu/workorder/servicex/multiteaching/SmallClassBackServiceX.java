package com.boxfishedu.workorder.servicex.multiteaching;

import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassCardStatus;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.servicex.multiteaching.teacherstatus.SmallClassEvent;
import com.boxfishedu.workorder.servicex.multiteaching.teacherstatus.SmallClassEventDispatch;
import com.boxfishedu.workorder.web.param.fishcardcenetr.PublicClassBuilderParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by hucl on 17/1/10.
 */
@Component
public class SmallClassBackServiceX {
    @Autowired
    private SmallClassEventDispatch smallClassEventDispatch;

    public void configPublicClass(PublicClassBuilderParam publicClassBuilderParam) {
        SmallClass smallClass = new SmallClass(publicClassBuilderParam);
        new SmallClassEvent(smallClass,smallClassEventDispatch,SmallClassCardStatus.CREATE);
    }
}
