package com.boxfishedu.workorder.servicex.multiteaching.teacherstatus;

import com.boxfishedu.workorder.servicex.multiteaching.event.SmallClassEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Created by hucl on 17/1/8.
 */
@Order(1220)
@Component
public class TeacherLeaveEarlyEventCustomer extends SmallClassEventCustomer {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    @Override
    public void exec(SmallClassEvent smallClassEvent) {

    }
}
