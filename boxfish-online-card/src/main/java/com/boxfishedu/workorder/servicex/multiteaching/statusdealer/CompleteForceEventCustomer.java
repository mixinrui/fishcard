package com.boxfishedu.workorder.servicex.multiteaching.statusdealer;

import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassCardStatus;
import com.boxfishedu.workorder.servicex.multiteaching.statusdealer.initstrategy.GroupInitStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Created by hucl on 17/1/5.
 */
@Order(200)
@Component
public class CompleteForceEventCustomer extends SmallClassEventCustomer {

    @PostConstruct
    public void initEvent() {
        this.setSmallClassCardStatus(SmallClassCardStatus.COMPLETED_FORCE);
    }

    @Override
    public void exec(SmallClassEvent smallClassEvent) {
        logger.info("教师开始进入上课...");
    }
}
