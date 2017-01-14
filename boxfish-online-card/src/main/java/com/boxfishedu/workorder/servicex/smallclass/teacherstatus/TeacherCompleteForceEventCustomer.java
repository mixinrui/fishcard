package com.boxfishedu.workorder.servicex.smallclass.teacherstatus;

import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassCardStatus;
import com.boxfishedu.workorder.servicex.smallclass.event.SmallClassEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by hucl on 17/1/5.
 */
@Order(1210)
@Component
public class TeacherCompleteForceEventCustomer extends SmallClassEventCustomer {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void initEvent() {
        this.setSmallClassCardStatus(SmallClassCardStatus.COMPLETED_FORCE);
    }

    @Override
    public void exec(SmallClassEvent smallClassEvent) {
        logger.info("教师开始进入上课...");
    }
}
