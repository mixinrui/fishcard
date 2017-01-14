package com.boxfishedu.workorder.servicex.multiteaching.teacherstatus;

import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassCardStatus;
import com.boxfishedu.workorder.servicex.multiteaching.event.SmallClassEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by hucl on 17/1/5.
 */
@Order(1200)
@Component
public class TeacherCompleteEventCustomer extends SmallClassEventCustomer{

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void initEvent(){
        this.setSmallClassCardStatus(SmallClassCardStatus.COMPLETED);
    }

    @Override
    public void exec(SmallClassEvent smallClassEvent) {
        logger.info("课程结束...");
    }
}
