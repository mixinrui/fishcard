package com.boxfishedu.workorder.servicex.smallclass.status.teacherstatus;

import com.boxfishedu.workorder.common.bean.PublicClassInfoConstantStatus;
import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEvent;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEventCustomer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by hucl on 17/1/8.
 */
@Order(PublicClassInfoConstantStatus.TEACHER_SWITCH_STUDENT)
@Component
public class TeacherSwitchStudentCustomer  extends SmallClassEventCustomer {
    @PostConstruct
    public void initEvent() {
        this.setSmallClassCardStatus(PublicClassInfoStatusEnum.TEACHER_SWITCH_STUDENT);
    }

    @Override
    public void exec(SmallClassEvent smallClassEvent) {
        logger.info("教师上课切换学生...");
    }
}
