package com.boxfishedu.workorder.servicex.multiteaching.statusdealer;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Created by hucl on 17/1/8.
 */
@Order(420)
@Component
public class LeaveEarlyEventCustomer extends SmallClassEventCustomer {
    @Override
    public void exec(SmallClassEvent smallClassEvent) {

    }
}
