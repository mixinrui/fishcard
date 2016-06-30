package com.boxfishedu.workorder.service;

import com.boxfishedu.workorder.dao.jpa.WorkOrderUserJpaRepository;
import com.boxfishedu.workorder.entity.mysql.WorkOrderUser;
import com.boxfishedu.workorder.service.base.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 登陆用户信息服务
 * Created by zijun.jiao on 16/6/28.
 */
@Component
public class WorkOrderUserService extends BaseService<WorkOrderUser, WorkOrderUserJpaRepository, Long> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public WorkOrderUser findByUserCodeAndFlag(String userCode, String flag) {
        return jpa.findByUserCodeAndFlag(userCode,flag);
    }


}
