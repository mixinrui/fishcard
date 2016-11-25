package com.boxfishedu.workorder.servicex.instantclass.instantvalidator;

import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.service.accountcardinfo.OnlineAccountService;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by hucl on 16/11/4.
 */
@Order(0)
@Component
public class ParameterValidator implements InstantClassValidator {
    @Autowired
    private OnlineAccountService onlineAccountService;

    private org.slf4j.Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Override
    public int preValidate() {
        InstantRequestParam instantRequestParam= ThreadLocalUtil.instantRequestParamThreadLocal.get();
        logger.debug("@instantcard_{}_validate0#ParameterValidator,参数校验开始",instantRequestParam.getStudentId());
        if(Objects.isNull(instantRequestParam.getStudentId())){
            throw new BusinessException("用户参数为必填");
        }
        if(!StringUtils.isEmpty(instantRequestParam.getTutorType())){
            if(!instantRequestParam.getTutorType().equals(TutorTypeEnum.FRN.toString())){
                return InstantClassRequestStatus.TUTOR_TYPE_NOT_SUPPORT.getCode();
            }
        }
        return InstantClassRequestStatus.UNKNOWN.getCode();
    }
}
