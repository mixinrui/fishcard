package com.boxfishedu.workorder.servicex.smallclass.status.commonstatus;

import com.boxfishedu.workorder.common.bean.PublicClassInfoConstantStatus;
import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.common.bean.RoleEnum;
import com.boxfishedu.workorder.dao.mongo.SmallClassLogMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.SmallClassLog;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.service.smallclass.SmallClassLogService;
import com.boxfishedu.workorder.servicex.smallclass.initstrategy.GroupInitStrategy;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEvent;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEventCustomer;
import com.boxfishedu.workorder.servicex.smallclass.status.event.StatusDealer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Created by hucl on 17/1/5.
 */
@Order(PublicClassInfoConstantStatus.CREATE)
@Component
public class CreateEventCustomer extends SmallClassEventCustomer {

    @Autowired
    Map<String, GroupInitStrategy> groupInitStrategyMap;

    @Autowired
    private SmallClassLogService smallClassLogService;

    @PostConstruct
    public void initEvent() {
        this.setSmallClassCardStatus(PublicClassInfoStatusEnum.CREATE);
    }

    @Override
    public void execute(SmallClass smallClass) {
        GroupInitStrategy groupInitStrategy = groupInitStrategyMap.get(this.prefix + smallClass.getClassType());

        //初始化小班课信息
        groupInitStrategy.initGroupClass(smallClass);

        //保存smallclass

        //将小班课信息更新进workorder,courseschedule

        //持久化数据到数据库

        //记录流水日志
    }

}
