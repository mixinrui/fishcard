package com.boxfishedu.workorder.servicex.instantclass.instantvalidator;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.dao.mongo.InstantClassTimeRulesMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.AccountCardInfo;
import com.boxfishedu.workorder.entity.mongo.InstantClassTimeRules;
import com.boxfishedu.workorder.service.accountcardinfo.AccountCardInfoService;
import com.boxfishedu.workorder.service.accountcardinfo.OnlineAccountService;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import lombok.Data;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by hucl on 16/11/4.
 */
@Order(2)
@Component
public class LatestClassValidator implements InstantClassValidator {
    @Autowired
    private OnlineAccountService onlineAccountService;

    @Autowired
    private InstantClassJpaRepository instantClassJpaRepository;

    private org.slf4j.Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Override
    public int preValidate() {
        InstantRequestParam instantRequestParam=ThreadLocalUtil.instantRequestParamThreadLocal.get();
        logger.debug("@instantcard_{}_validate#3_LatestClassValidator,校验开始",instantRequestParam.getStudentId());
        if(!onlineAccountService.isMember(instantRequestParam.getStudentId())){
            logger.debug("@instantcard_{}_validate3_LatestClassValidator#preValidate#[{}]不是购买用户",instantRequestParam.getStudentId(),instantRequestParam.getStudentId());
            return InstantClassRequestStatus.UNKNOWN.getCode();
        }
        Optional<Date> dateOptional = workOrderJpaRepository.findLatestClassDateByStudentId(instantRequestParam.getStudentId(),new Integer(0),new Date());
        if(dateOptional.isPresent()){
            if(LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(30).isAfter(
                    LocalDateTime.ofInstant(dateOptional.get().toInstant(), ZoneId.systemDefault()))) {
                logger.debug("@LatestClassValidator#preValidate#[{}]30分钟内有课",instantRequestParam.getStudentId());
                ThreadLocalUtil.classDateIn30Minutes.set(dateOptional.get());
                return InstantClassRequestStatus.HAVE_CLASS_IN_HALF_HOURS.getCode();
            }
        }
        return InstantClassRequestStatus.UNKNOWN.getCode();
    }

}
