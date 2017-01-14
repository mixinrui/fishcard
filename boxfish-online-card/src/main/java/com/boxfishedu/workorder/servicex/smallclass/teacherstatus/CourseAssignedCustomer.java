package com.boxfishedu.workorder.servicex.smallclass.teacherstatus;

import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.servicex.smallclass.event.SmallClassEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by hucl on 17/1/5.
 */
@Order(110)
@Component
public class CourseAssignedCustomer extends SmallClassEventCustomer {
    @PostConstruct
    public void initEvent(){
        this.setSmallClassCardStatus(PublicClassInfoStatusEnum.COURSE_ASSIGNED);
    }

    @Override
    public void exec(SmallClassEvent smallClassEvent) {
        logger.info("分配课程");
    }
}
