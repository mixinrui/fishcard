package com.boxfishedu.workorder.service.instantclass;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.requester.InstantTeacherRequester;
import com.boxfishedu.workorder.requester.MsgPushRequester;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.servicex.instantclass.classdatagenerator.OtherEntranceDataGenerator;
import com.boxfishedu.workorder.servicex.instantclass.classdatagenerator.ScheduleEntranceDataGenerator;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.boxfishedu.workorder.web.param.TeacherInstantRequestParam;
import com.boxfishedu.workorder.web.result.InstantGroupInfo;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by hucl on 16/11/7.
 */
@Component
public class InstantClassTeacherService {
    @Autowired
    private InstantTeacherRequester instantTeacherRequester;

    @Autowired
    private InstantClassJpaRepository instantClassJpaRepository;

    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    private CourseOnlineRequester courseOnlineRequester;

    @Autowired
    private OtherEntranceDataGenerator otherEntranceDataGenerator;

    @Autowired
    private ScheduleEntranceDataGenerator scheduleEntranceDataGenerator;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private MsgPushRequester msgPushRequester;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    public void dealFetchedTeachersAsync(InstantClassCard instantClassCard,boolean queryFlag){
        this.dealFetchedTeachersAsync(instantClassCard);
    }

    public void dealFetchedTeachersAsync(InstantClassCard instantClassCard){
        threadPoolManager.execute(new Thread(() -> {dealInstantFetchedTeachers(instantClassCard);}));
    }

    public void dealInstantFetchedTeachers(InstantClassCard instantClassCard){
        Optional<List<Long>> teacherIdsOptional=Optional.empty();
        try {
            teacherIdsOptional=instantTeacherRequester.getInstantTeacherIds(instantClassCard);
        }
        catch (Exception ex){
            logger.error("@dealInstantFetchedTeachers#user:[{}] card[{}] IIIIIIIIIIIIIII    获取实时推荐教师失败,将匹配状态设置为未匹配上"
                    ,instantClassCard.getStudentId(),JacksonUtil.toJSon(instantClassCard));
            this.updateNomatchStatus(instantClassCard);
            return;
        }
        if(!teacherIdsOptional.isPresent()||CollectionUtils.isEmpty(teacherIdsOptional.get())){
            logger.debug("@dealInstantFetchedTeachers IIIIIIIIIIIIIII 没有获取到可用的教师列表,cardId[{}],studentId[{}]"
                    ,JacksonUtil.toJSon(instantClassCard),instantClassCard.getStudentId());
            return;
        }
        logger.debug("@dealInstantFetchedTeachers IIIIIIIIIIIIIII 获取到教师列表[{}],card[{}],学生[{}]"
                ,teacherIdsOptional.get().toString(),JacksonUtil.toJSon(instantClassCard),instantClassCard.getStudentId());
        //匹配上老师,则向教师推送抢单的消息
        msgPushRequester.notifyInstantGroupClassMsg(instantClassCard,teacherIdsOptional.get());
    }

    private void updateNomatchStatus(InstantClassCard instantClassCard){
        instantClassJpaRepository.updateStatus(instantClassCard.getId(), InstantClassRequestStatus.NO_MATCH.getCode());
    }

    @Transactional
    public List<WorkOrder> prepareForInstantClass(InstantClassCard instantClassCard
            ,InstantTeacherRequester.InstantAssignTeacher instantAssignTeacher,InstantGroupInfo instantGroupInfo){
        List<WorkOrder> workOrders=this.initCardAndSchedule(instantClassCard,instantAssignTeacher);
        try {
            BeanUtils.copyProperties(instantGroupInfo
                    , courseOnlineRequester.instantCreateGroup(workOrderService.findOne(instantClassCard.getWorkorderId())));
        }
        catch (Exception ex){
            logger.error("@prepareForInstantClass#创建群组失败,instantcard:{}",instantClassCard,ex);
            throw new BusinessException("创建群组失败");
        }
        return workOrders;

    }

    public List<WorkOrder> initCardAndSchedule(InstantClassCard instantClassCard, InstantTeacherRequester.InstantAssignTeacher instantAssignTeacher) {
        instantClassCard=instantClassJpaRepository.findForUpdate(instantClassCard.getId());
        if(instantClassCard.getStatus()==InstantClassRequestStatus.MATCHED.getCode()
                ||instantClassCard.getStatus()==InstantClassRequestStatus.NO_MATCH.getCode()){
            throw new BusinessException("抢单失败.数据库的数据已经被更新为不可抢单状态");
        }
        instantClassCard.setTeacherId(instantAssignTeacher.getTeacherId());
        instantClassCard.setTeacherName(instantAssignTeacher.getTeacherName());
        instantClassCard.setStatus(InstantClassRequestStatus.MATCHED.getCode());
        instantClassJpaRepository.save(instantClassCard);
        List<WorkOrder> workOrders= Collections.emptyList();
        switch (InstantRequestParam.SelectModeEnum.getSelectMode(instantClassCard.getEntrance())){
            case COURSE_SCHEDULE_ENTERANCE:
                workOrders=scheduleEntranceDataGenerator.initCardAndSchedule(instantClassCard);
                break;
            case OTHER_ENTERANCE:
                workOrders=otherEntranceDataGenerator.initCardAndSchedule(instantClassCard);
                break;
            default:
                throw new BusinessException("不合法的入口参数");
        }
        instantClassJpaRepository.save(instantClassCard);
        return workOrders;
    }
}
