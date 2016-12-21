package com.boxfishedu.workorder.servicex.instantclass.classdatagenerator;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.threadpool.LogPoolManager;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.instantclass.schedulestrategy.FirstClassStrategy;
import com.boxfishedu.workorder.servicex.instantclass.schedulestrategy.LastClassStrategy;
import com.boxfishedu.workorder.servicex.instantclass.schedulestrategy.ScheduleStrategyContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by hucl on 16/11/9.
 */
@Component
public class ScheduleEntranceDataGenerator implements IClassDataGenerator {
    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private CourseScheduleRepository courseScheduleRepository;

    @Autowired
    private ServiceJpaRepository serviceJpaRepository;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    @Autowired
    private LogPoolManager logPoolManager;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private InstantClassJpaRepository instantClassJpaRepository;

    @Autowired
    private FirstClassStrategy firstClassStrategy;

    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    private ServiceSDK serviceSDK;

    @Autowired
    private ScheduleStrategyContext scheduleStrategyContext;

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    @Override
    public List<WorkOrder> initCardAndSchedule(InstantClassCard instantClassCard) {
        //目前暂时只会使用到第一节课的策略,以后会根据需求变更
        return scheduleStrategyContext.prepareInstantSchedule(instantClassCard,null);
    }

    @Override
    public void initCourses(WorkOrder workOrder) {

    }

}
