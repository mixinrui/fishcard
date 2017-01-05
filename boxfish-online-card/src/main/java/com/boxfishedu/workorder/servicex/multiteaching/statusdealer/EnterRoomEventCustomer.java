package com.boxfishedu.workorder.servicex.multiteaching.statusdealer;

import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassCardStatus;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by hucl on 17/1/5.
 */
@Order(20)
@Component
public class EnterRoomEventCustomer extends SmallClassEventCustomer {

    @PostConstruct
    public void initEvent(){
        this.setSmallClassCardStatus(SmallClassCardStatus.TEACHER_ENTER_ROOM);
    }

    @Override
    public void exec(SmallClassEvent smallClassEvent) {
        logger.info("教师开始进入上课...");
    }
}
