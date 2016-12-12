package com.boxfishedu.workorder.servicex.instantclass.grabordervalidator;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.bean.instanclass.TeacherInstantClassStatus;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.boxfishedu.workorder.web.param.TeacherInstantRequestParam;
import com.boxfishedu.workorder.web.result.InstantClassResult;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Created by hucl on 16/11/4.
 */
@Order(1)
@Component
public class ImmutableDbValidator implements IGrabInstantClassValidator {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private InstantClassJpaRepository instantClassJpaRepository;

    @Autowired
    private ServeService serveService;

    @Override
    public TeacherInstantClassStatus preValidate() {
        logger.debug(">>>>>1:ImmutableDbValidator;数据库校验");
        TeacherInstantRequestParam teacherInstantRequestParam = ThreadLocalUtil.getTeacherInstantParam();
        //如果状态被标记为已匹配或者未匹配,则直接返回抢单失败
        InstantClassCard instantClassCard = instantClassJpaRepository.findOne(teacherInstantRequestParam.getCardId());
        if (Objects.isNull(instantClassCard)) {
            throw new BusinessException("@ImmutableDbValidator不存在对应的instantclassCard");
        }
        if (instantClassCard.getEntrance() == InstantRequestParam.SelectModeEnum.OTHER_ENTERANCE.getCode()) {
            List<Service> services = serveService.findByOrderId(instantClassCard.getOrderId());
            if (CollectionUtils.isEmpty(services) || services.get(0).getCoursesSelected() == 1) {
                logger.error("课程已经选过课,不能再重复消费,学生[{}],card[{}],教师[{}]"
                        , instantClassCard.getStudentId(), instantClassCard.getId(), instantClassCard.getTeacherId());
                return TeacherInstantClassStatus.FAIL_TO_MATCH;
            }
        }
        switch (InstantClassRequestStatus.getEnumByCode(instantClassCard.getStatus())) {
            case MATCHED:
            case NO_MATCH:
                logger.debug("@<<<<<1:fail:ImmutableDbValidator.preValidate;该单已经被标记为[{}],teacher[{}],学生[{}]不可发起抢单,退出抢单...."
                        , instantClassCard.getStatus(), teacherInstantRequestParam.getTeacherId(),instantClassCard.getStudentId());
                return TeacherInstantClassStatus.FAIL_TO_MATCH;
            default:
                return TeacherInstantClassStatus.UNKNOWN;
        }
    }
}
