package com.boxfishedu.workorder.servicex.smallclass.teacherstatus;

import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.servicex.smallclass.event.SmallClassEvent;
import com.boxfishedu.workorder.servicex.smallclass.event.SmallClassEventCustomer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by hucl on 17/1/5.
 */
@Order(1200)
@Component
public class TeacherCompleteEventCustomer extends SmallClassEventCustomer {

    @PostConstruct
    public void initEvent() {
        this.setSmallClassCardStatus(PublicClassInfoStatusEnum.TEACHER_COMPLETED);
    }

    @Override
    public void exec(SmallClassEvent smallClassEvent) {
        logger.info("课程结束...");
    }
}
