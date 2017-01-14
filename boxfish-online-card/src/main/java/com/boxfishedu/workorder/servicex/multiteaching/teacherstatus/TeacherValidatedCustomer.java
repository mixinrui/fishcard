package com.boxfishedu.workorder.servicex.multiteaching.teacherstatus;

import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassCardStatus;
import com.boxfishedu.workorder.servicex.multiteaching.event.SmallClassEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by hucl on 17/1/5.
 */
@Order(1000)
@Component
public class TeacherValidatedCustomer extends SmallClassEventCustomer{
    @PostConstruct
    public void initEvent(){
        this.setSmallClassCardStatus(SmallClassCardStatus.CARD_VALIDATED);
    }

    @Override
    public void exec(SmallClassEvent smallClassEvent) {
        logger.info("教师开始上课...");
    }
}
