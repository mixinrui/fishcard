package com.boxfishedu.workorder.servicex.multiteaching.studentstatus;

import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassCardStatus;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.servicex.multiteaching.event.SmallClassEvent;
import com.boxfishedu.workorder.servicex.multiteaching.initstrategy.GroupInitStrategy;
import com.boxfishedu.workorder.servicex.multiteaching.teacherstatus.SmallClassEventCustomer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Created by hucl on 17/1/5.
 */
@Order(400)
@Component
public class StudentOnWheatCustomer extends SmallClassEventCustomer {

    @Autowired
    Map<String, GroupInitStrategy> groupInitStrategyMap;

    private final Logger logger= LoggerFactory.getLogger(this.getClass());


    @PostConstruct
    public void initEvent() {
        this.setSmallClassCardStatus(SmallClassCardStatus.CREATE);
    }

    public final String prefix = "INIT_";

    @Override
    public void exec(SmallClassEvent smallClassEvent) {
    }
}
