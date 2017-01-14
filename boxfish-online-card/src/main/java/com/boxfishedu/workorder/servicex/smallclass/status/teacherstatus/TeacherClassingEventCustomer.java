package com.boxfishedu.workorder.servicex.smallclass.status.teacherstatus;

import com.boxfishedu.workorder.common.bean.PublicClassInfoConstantStatus;
import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEvent;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEventCustomer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by hucl on 17/1/5.
 */
@Order(PublicClassInfoConstantStatus.TEACHER_CLASSING)
@Component
public class TeacherClassingEventCustomer extends SmallClassEventCustomer {

    @PostConstruct
    public void initEvent() {
        this.setSmallClassCardStatus(PublicClassInfoStatusEnum.TEACHER_CLASSING);
    }

    @Override
    public void exec(SmallClassEvent smallClassEvent) {
        logger.info("教师开始上课...");
    }
}
