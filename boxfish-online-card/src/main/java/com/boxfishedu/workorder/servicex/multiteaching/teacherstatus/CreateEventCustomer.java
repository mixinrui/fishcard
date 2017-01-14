package com.boxfishedu.workorder.servicex.multiteaching.teacherstatus;

import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassCardStatus;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.servicex.multiteaching.event.SmallClassEvent;
import com.boxfishedu.workorder.servicex.multiteaching.initstrategy.GroupInitStrategy;
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
@Order(10)
@Component
public class CreateEventCustomer extends SmallClassEventCustomer {

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
        SmallClass smallClass = smallClassEvent.getSource();
        smallClass.setStatus(smallClassEvent.getType().getCode());

        GroupInitStrategy groupInitStrategy = groupInitStrategyMap.get(prefix + smallClass.getClassType());

        //初始化小班课信息
        groupInitStrategy.initGroupClass(smallClass);

        //保存smallclass

        //将小班课信息更新进workorder,courseschedule

        //持久化数据到数据库

        //记录流水日志

    }
}
