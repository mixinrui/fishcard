package com.boxfishedu.workorder.servicex.multiteaching.statusdealer;

import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassCardStatus;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by hucl on 17/1/5.
 */
@Order(200)
@Component
public class CardValidatedCustomer extends SmallClassEventCustomer{
    @PostConstruct
    public void initEvent(){
        this.setSmallClassCardStatus(SmallClassCardStatus.CARD_VALIDATED);
    }

    @Override
    public void exec(SmallClassEvent smallClassEvent) {
        logger.info("教师开始上课...");
    }
}
