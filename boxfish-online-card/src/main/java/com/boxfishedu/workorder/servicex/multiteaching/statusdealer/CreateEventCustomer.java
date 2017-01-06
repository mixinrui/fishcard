package com.boxfishedu.workorder.servicex.multiteaching.statusdealer;

import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassCardStatus;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by hucl on 17/1/5.
 */
@Order(100)
@Component
public class CreateEventCustomer extends SmallClassEventCustomer {
    @PostConstruct
    public void initEvent(){
        this.setSmallClassCardStatus(SmallClassCardStatus.CREATE);
    }

    @Override
    public void exec(SmallClassEvent smallClassEvent) {
        logger.info("开始创建小班课卡...");
    }
}
