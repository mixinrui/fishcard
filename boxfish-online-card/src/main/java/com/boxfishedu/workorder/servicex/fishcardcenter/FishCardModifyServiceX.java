package com.boxfishedu.workorder.servicex.fishcardcenter;

import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.MessagePushTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.exception.NotFoundException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.WorkOrderConstant;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.service.fishcardcenter.FishCardModifyService;
import com.boxfishedu.workorder.service.studentrelated.TimePickerService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.web.param.StartTimeParam;
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
    private CourseOnlineRequester  courseOnlineRequester;

    @Autowired
    private TimePickerService timePickerService;

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
        workOrder.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
        workOrder.setTeacherName(teacherChangeParam.getTeacherName());
        courseSchedule.setTeacherId(teacherChangeParam.getTeacherId());
        courseSchedule.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());

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
        if(CollectionUtils.isEmpty(workOrders)){
            return;
        }
        workOrders.forEach(workOrder -> {
            fishCardModifyService.changeCourse(workOrder);
        });
    }

    public void changerderCourses(Long studentId) {
        List<WorkOrder> workOrders = fishCardModifyService.findByStudentIdAndStatusLessThan(studentId, FishCardStatusEnum.WAITFORSTUDENT.getCode());
        if(CollectionUtils.isEmpty(workOrders)){
            throw new NotFoundException();
        }
        workOrders.forEach(workOrder -> {
            fishCardModifyService.changeCourse(workOrder);
        });
    }

    public void changCourse(Long workOrderId) {
        WorkOrder workOrder = workOrderService.findOne(workOrderId);
        if(null==workOrder){
            return;
        }
        fishCardModifyService.changeCourse(workOrder);
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


    /**
     * 更改上课时间点
     * @param startTimeParam
     * @return
     */
    @Transactional
    public JsonResultModel  changeStartTime(StartTimeParam startTimeParam){
        Map<String,String> resultMap = Maps.newHashMap();
        // 检查日期合法化
        boolean  dataFlag = checkDate(startTimeParam);
        if(!dataFlag){
            resultMap.put("code","1");
            resultMap.put("msg","日期不合法");
            return  JsonResultModel.newJsonResultModel(resultMap);
        }

        //获取鱼卡信息
        WorkOrder workOrder =workOrderService.findByIdForUpdate(startTimeParam.getWorkOrderId())   ;
        CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(startTimeParam.getWorkOrderId());

        if(null == workOrder || courseSchedule ==null){
            resultMap.put("code","2");
            resultMap.put("msg","鱼卡或者课程信息不存在");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        Long teacherId = workOrder.getTeacherId();
        String startTime = DateUtil.Date2String(  workOrder.getStartTime());

        // 验证鱼卡状态 创建、分配课程、分配老师

        //获取结束时间
        startTimeParam.setEndDateFormat(     DateUtil.String2Date(startTimeParam.getEndDate()) );

        //记录老教师,时间
        Long oldTeacherId= workOrder.getTeacherId();
        String oldStartTime=StringUtils.EMPTY;
        if(null!=workOrder.getStartTime()){
            oldStartTime=DateUtil.date2SimpleString(workOrder.getStartTime());
        }
        String oldTeacherName= StringUtils.EMPTY;
        if(!StringUtils.isEmpty(workOrder.getTeacherName())){
            workOrder.getTeacherName();
        }

        //分配教师以后其实就已经是就绪,目前这两个状态有重叠
        if(workOrder.getStatus()==FishCardStatusEnum.CREATED.getCode() || workOrder.getStatus()==FishCardStatusEnum.COURSE_ASSIGNED.getCode() || workOrder.getStatus()==FishCardStatusEnum.TEACHER_ASSIGNED.getCode()){
            if(workOrder.getStatus()!=FishCardStatusEnum.CREATED.getCode()){
                workOrder.setStatus(FishCardStatusEnum.COURSE_ASSIGNED.getCode());
                courseSchedule.setStatus( FishCardStatusEnum.COURSE_ASSIGNED.getCode());
            }
            workOrder.setTeacherId(0L);
            workOrder.setTeacherName("");
            workOrder.setStartTime(startTimeParam.getBeginDateFormat() );
            workOrder.setEndTime(startTimeParam.getEndDateFormat() );
            workOrder.setSlotId(startTimeParam.getTimeslotId());
            workOrderService.save(workOrder);

            courseSchedule.setClassDate(DateUtil.String2SimpleDate(startTimeParam.getBeginDate()));
            logger.info("changeStartTime : [{}]",DateUtil.String2SimpleDate(startTimeParam.getBeginDate()));
            courseSchedule.setTeacherId(0L);
            courseSchedule.setTimeSlotId(startTimeParam.getTimeslotId() );

            courseScheduleService.save(courseSchedule);

            // 推送教师更换时间推送
            if(null!=teacherId && teacherId>0L){
                this.pushTeacherList(teacherId,startTime);
            }

        }else {
            resultMap.put("code","2");
            resultMap.put("msg","鱼卡状态不正确");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        List<CourseSchedule> courseSchedules = Lists.newArrayList();
        courseSchedules.add(courseSchedule);
        // 调用分配老师接口
        timePickerService.getRecommandTeachers(workOrder.getService(),courseSchedules);


        // 如果 含有 教室id  进行教室资源释放

        //通知师生运营释放教师资源
       // teacherStudentRequester.releaseTeacher(workOrder);
        teacherStudentRequester.notifyCancelTeacher(workOrder);
        //通知小马解散师生关系
        courseOnlineRequester.releaseGroup(workOrder);

        // 记录日志
        workOrderLogService.saveWorkOrderLog(workOrder,"更换换时间#旧的上课时间["+oldStartTime+"],旧的教师id["+oldTeacherId+"],旧的教师姓名["+oldTeacherName+"]");


        return new JsonResultModel().newJsonResultModel("OK");
    }




    /**
     * 判断日期合法性
     * @param startTimeParam
     * @return
     */
    private boolean checkDate(StartTimeParam startTimeParam){
        logger.info("checkDate :[{}]",startTimeParam.getBeginDate());
        // 日期为空判断
        if(startTimeParam == null || null ==startTimeParam.getBeginDate())
            return false;
        //日期长度判断
        if(startTimeParam.getBeginDate().length()<16)
            return false;

        String minutes = startTimeParam.getBeginDate().substring(14,16);
        if(minutes.equals("00") || minutes.equals("30")){
        }else {
            return false;
        }

        startTimeParam.setBeginDateFormat(DateUtil.String2Date(startTimeParam.getBeginDate()));

        // 大于现在的时间
        if(startTimeParam.getBeginDateFormat().before(new Date())){
            logger.info("checkDate :1");
            return false;
        }
        return true;

    }



    /**
     * 推送通知老师 更换时间
     * @param teahcerId  老师id
     * @param startTime  原来课程 开始时间
     */
    private  void pushTeacherList(Long teahcerId,String startTime) {
        logger.info("notiFyTeahcerchangeStartTime::begin");
        List list = Lists.newArrayList();
            startTime = DateUtil.Date2ForForeignDate( DateUtil.String2Date( startTime) );

            String pushTitle = WorkOrderConstant.SEND_TEACHER_CHANGETIME_BEGIN+startTime+WorkOrderConstant.SEND_TEACHER_CHANGETIME_END;
            Integer count = 1;
            Map map1 = Maps.newHashMap();
            map1.put("user_id", teahcerId);
            map1.put("push_title", pushTitle);

            JSONObject jo = new JSONObject();
            //jo.put("type", MessagePushTypeEnum.SEND_TEACHER_CHANGE_CLASSTIME_TYPE.toString());
            jo.put("count", count);
            jo.put("push_title", pushTitle);

            map1.put("data", jo);

            list.add(map1);


        teacherStudentRequester.pushTeacherListOnlineMsg(list);

        logger.info("notiFyTeahcerchangeStartTime::end");
    }

}
