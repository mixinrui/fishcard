package com.boxfishedu.workorder.servicex.fishcardcenter;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.service.fishcardcenter.FishCardModifyService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.web.param.fishcardcenetr.FishCardDeleteParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.param.TeacherChangeParam;
import com.boxfishedu.workorder.web.view.course.CourseView;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by hucl on 16/5/10.
 */
@Component
public class FishCardModifyServiceX {
    @Autowired
    private ServeService serveService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    @Autowired
    private ServiceSDK serviceSDK;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FishCardModifyService fishCardModifyService;

    @Autowired
    private UrlConf urlConf;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //选择五门课程供给学生选择
    public JsonResultModel getCoursesForUpdate(Long studentId) {
        List<CourseView> courseViews = serveService.getCoursesForUpdate(studentId, 5);
        return JsonResultModel.newJsonResultModel(courseViews);
    }

    public JsonResultModel changeTeacher(TeacherChangeParam teacherChangeParam) {
        //获取对应的workorder和courseschedule
        WorkOrder workOrder = workOrderService.findOne(teacherChangeParam.getWorkOrderId());
        CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(teacherChangeParam.getWorkOrderId());

        if ((null == workOrder) || (null == courseSchedule)) {
            throw new BusinessException("无对应的记录,请检查所传参数是否合法");
        }

        //对workorder和courseschedule做控制
        workOrder.setTeacherId(teacherChangeParam.getTeacherId());
        workOrder.setAssignTeacherTime(new Date());
        workOrder.setTeacherName(teacherChangeParam.getTeacherName());
        courseSchedule.setTeacherId(teacherChangeParam.getTeacherId());

        //通知师生运营更换老师
        teacherStudentRequester.notifyChangeTeacher(workOrder);

        //更新新的教师到workorder和courseschedule,此处做事务控制
        workOrderService.updateWorkOrderAndSchedule(workOrder, courseSchedule);

        //通知小马添加新的群组
        serviceSDK.createGroup(workOrder);

        changeTeacherLog(workOrder);
        //返回结果
        return JsonResultModel.newJsonResultModel(null);
    }

    private void changeTeacherLog(WorkOrder workOrder) {
        WorkOrderLog workOrderLog = new WorkOrderLog();
        workOrderLog.setCreateTime(new Date());
        workOrderLog.setWorkOrderId(workOrder.getId());
        workOrderLog.setStatus(workOrder.getStatus());
        workOrderLog.setContent("更换教师:" + FishCardStatusEnum.getDesc(workOrder.getStatus()));
        workOrderLogService.save(workOrderLog);
    }

    public void changeSpecialOrderCourses(Long studentId, Long orderId) {
        List<WorkOrder> workOrders = fishCardModifyService.findByStudentIdAndOrderIdAndStatusLessThan(studentId, orderId, FishCardStatusEnum.WAITFORSTUDENT.getCode());
        workOrders.forEach(workOrder -> {
            fishCardModifyService.changeCourse(workOrder);
        });
    }

    public void changerderCourses(Long studentId) {
        List<WorkOrder> workOrders = fishCardModifyService.findByStudentIdAndStatusLessThan(studentId, FishCardStatusEnum.WAITFORSTUDENT.getCode());
        workOrders.forEach(workOrder -> {
            fishCardModifyService.changeCourse(workOrder);
        });
    }

    public void changCourse(Long workOrderId) {
        WorkOrder workOrder = workOrderService.findOne(workOrderId);
        fishCardModifyService.changeCourse(workOrder);
    }

    public void changePhonicsCourses() {
        List<WorkOrder> workOrders = workOrderService.findByCourseType("PHONICS");
        for (WorkOrder workOrder : workOrders) {
            fishCardModifyService.changeCourse(workOrder, 7);
        }
    }

    public void deleteFishCardsByStudentIds(FishCardDeleteParam fishCardDeleteParam) {
        List<Long> studentIds = fishCardDeleteParam.getStudentIds();
        Date beginDate = new Date();
        if (!StringUtils.isEmpty(fishCardDeleteParam.getBeginDate())) {
            beginDate = DateUtil.String2Date(fishCardDeleteParam.getBeginDate());
        }
        for (Long studentId : studentIds) {
            List<WorkOrder> workOrders = fishCardModifyService.findByStudentIdAndStatusLessThanAndStartTimeAfter(studentId, FishCardStatusEnum.WAITFORSTUDENT.getCode(), beginDate);
            if (CollectionUtils.isEmpty(workOrders)) {
                continue;
            }
            List<Long> workOrderIds = Lists.newArrayList();
            for (WorkOrder workOrder : workOrders) {
                workOrderIds.add(workOrder.getId());
                logger.debug("待删除的鱼卡[{}]", workOrder.getId());
            }
            Long[] workOrderLongIds=new Long[workOrderIds.size()];
            List<CourseSchedule> courseSchedules = courseScheduleService.findByWorkorderIdIn(workOrderIds.toArray(workOrderLongIds));
            Map<String, CourseSchedule> courseScheduleMap = Maps.newHashMap();
            courseSchedules.stream().forEach(courseSchedule -> {
                courseScheduleMap.put(courseSchedule.getWorkorderId().toString(), courseSchedule);
            });
            workOrders.forEach(workOrder -> {
                workOrderLogService.saveDeleteWorkOrderLog(workOrder, courseScheduleMap.get(workOrder.getId()));
                teacherStudentRequester.notifyCancelTeacher(workOrder);
            });
            fishCardModifyService.deleteCardsAndSchedules(workOrders, courseSchedules);
        }
    }

    @Transactional
    public void deleteFishCardsByIds(FishCardDeleteParam fishCardDeleteParam) {
        //不会太多,一条条删也无所谓,以后真多了可以考虑批量删
        List<Long> workOrderIds = fishCardDeleteParam.getFishCardIds();
        for (Long id : workOrderIds) {
            WorkOrder workOrder = workOrderService.findOne(id);
            if (workOrder.getStatus() > FishCardStatusEnum.TEACHER_ASSIGNED.getCode()) {
                logger.error("所选课程包含已经上过课的id,请确认后再删除[{}]",workOrder.getId());
                continue;
            }
            teacherStudentRequester.notifyCancelTeacher(workOrder);
            CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(workOrder.getId());
            workOrderLogService.saveDeleteWorkOrderLog(workOrder, courseSchedule);
            fishCardModifyService.delete(id);
            courseScheduleService.delete(courseSchedule);
        }
        for(Long id:workOrderIds){
            WorkOrder workOrder = workOrderService.findOne(id);
            teacherStudentRequester.notifyCancelTeacher(workOrder);
        }
    }

}
