package com.boxfishedu.workorder.servicex.smallclass.teacherstatus;

import com.boxfishedu.workorder.servicex.smallclass.event.SmallClassEvent;
import com.boxfishedu.workorder.servicex.smallclass.event.SmallClassEventCustomer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Created by hucl on 17/1/8.
 */
@Order(1220)
@Component
public class TeacherLeaveEarlyEventCustomer extends SmallClassEventCustomer {

    @Override
    public void exec(SmallClassEvent smallClassEvent) {

    }
}
