package com.boxfishedu.workorder.servicex.smallclass.teacherstatus;

import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.servicex.smallclass.event.SmallClassEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by hucl on 17/1/5.
 */
@Order(1000)
@Component
public class TeacherValidatedCustomer extends SmallClassEventCustomer{

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void initEvent(){
        this.setSmallClassCardStatus(PublicClassInfoStatusEnum.TEACHER_CARD_VALIDATED);
    }

    @Override
    public void exec(SmallClassEvent smallClassEvent) {
        logger.info("教师开始上课...");
    }
}
