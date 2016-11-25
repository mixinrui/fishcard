package com.boxfishedu.workorder.servicex.instantclass.instantvalidator;

import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.accountcardinfo.OnlineAccountService;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.servicex.studentrelated.TimePickerServiceXV1;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by hucl on 16/11/4.
 */
@Order(4)
@Component
public class ServiceTimeValidator implements InstantClassValidator {
    @Autowired
    private OnlineAccountService onlineAccountService;

    private org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TimePickerServiceXV1 timePickerServiceXV1;

    @Override
    public int preValidate() {
        InstantRequestParam instantRequestParam = ThreadLocalUtil.instantRequestParamThreadLocal.get();
        switch (InstantRequestParam.SelectModeEnum.getSelectMode(instantRequestParam.getSelectMode())) {
            case COURSE_SCHEDULE_ENTERANCE:
                return InstantClassRequestStatus.UNKNOWN.getCode();
            case OTHER_ENTERANCE:
                List<Service> services=timePickerServiceXV1.ensureConvertOver(InstantRequestParam.timeSlotParamAdapter(instantRequestParam),0);
                if(services.get(0).getOriginalAmount()!=1){
                    throw new BusinessException("您当前的订单服务次数超过一次，不能立即上课");
                }
                return InstantClassRequestStatus.UNKNOWN.getCode();
            default:
                throw new BusinessException("未知的入口参数");
        }
    }
}
