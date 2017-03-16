package com.boxfishedu.workorder.servicex.fishcardcenter;

import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.PublicClassInfoConstantStatus;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.bean.RoleEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.exception.NotFoundException;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.util.Collections3;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.ShortMessageCodeConstant;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.accountcardinfo.AccountCardInfoService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.service.accountcardinfo.OnlineAccountService;
import com.boxfishedu.workorder.service.fishcardcenter.FishCardModifyService;
import com.boxfishedu.workorder.service.smallclass.SmallClassLogService;
import com.boxfishedu.workorder.service.studentrelated.TimePickerService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.smallclass.SmallClassServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.AssignTeacherFixService;
import com.boxfishedu.workorder.web.param.MakeUpCourseParam;
import com.boxfishedu.workorder.web.param.StartTimeParam;
import com.boxfishedu.workorder.web.param.TeacherChangeParam;
import com.boxfishedu.workorder.web.param.fishcardcenetr.FishCardDeleteParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.course.CourseView;
import com.boxfishedu.workorder.web.view.fishcard.FishCardGroupsInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
    private AssignTeacherFixService assignTeacherFixService;


    @Autowired
    private CourseOnlineRequester courseOnlineRequester;

    @Autowired
    private AccountCardInfoService accountCardInfoService;

    @Autowired
    private TimePickerService timePickerService;

    @Autowired
    private UrlConf urlConf;

    @Autowired
    private RabbitMqSender rabbitMqSender;

    @Autowired
    private DataCollectorService dataCollectorService;

    @Autowired
    FishCardModifyService fishCardModifyService;

    @Autowired
    OnlineAccountService onlineAccountService;

    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;


    @Autowired
    private SmallClassServiceX smallClassServiceX;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private CourseScheduleRepository courseScheduleRepository;

    @Autowired
    private SmallClassLogService smallClassLogService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //选择五门课程供给学生选择
    public JsonResultModel getCoursesForUpdate(Long studentId) {
        List<CourseView> courseViews = serveService.getCoursesForUpdate(studentId, 5);
        return JsonResultModel.newJsonResultModel(courseViews);
    }

    public JsonResultModel changeTeacher(TeacherChangeParam teacherChangeParam) {
        //获取对应的workorder和courseschedule
        WorkOrder workOrder = workOrderService.findOne(teacherChangeParam.getWorkOrderId());
        if (workOrder.getIsCourseOver() == 1) {
            throw new BusinessException("课程已经结束,不能更换教师");
        }
        CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(teacherChangeParam.getWorkOrderId());

        if ((null == workOrder) || (null == courseSchedule)) {
            throw new BusinessException("无对应的记录,请检查所传参数是否合法");
        }

        WorkOrder oldWorkOrder = workOrder.clone();

        //对workorder和courseschedule做控制
        workOrder.setTeacherId(teacherChangeParam.getTeacherId());
        workOrder.setAssignTeacherTime(new Date());
        workOrder.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
        workOrder.setTeacherName(teacherChangeParam.getTeacherName());
        courseSchedule.setTeacherId(teacherChangeParam.getTeacherId());
        courseSchedule.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());

        //异步操作  // 设置指定老师申请失效
        assignTeacherFixService.disableAssignWorkOrderOut(workOrder.getId(), "换时间老师请假或者换老师");

        //通知师生运营更换老师
        teacherStudentRequester.notifyChangeTeacher(workOrder, teacherChangeParam);

        //更新新的教师到workorder和courseschedule,此处做事务控制
        workOrderService.updateWorkOrderAndSchedule(workOrder, courseSchedule);

        dataCollectorService.updateBothChnAndFnItemAsync(workOrder.getStudentId());

        //立即解散群组
        courseOnlineRequester.instantReleaseGroup(workOrder);

        //通知小马添加新的群组
        serviceSDK.createGroup(workOrder);

        changeTeacherLog(workOrder, oldWorkOrder, "1对1");
        //返回结果
        return JsonResultModel.newJsonResultModel(null);
    }

    /**
     * 小班课换老师
     *
     * @param teacherChangeParam
     * @return
     */
    @Transactional
    public JsonResultModel changeTeacherSmallClass(TeacherChangeParam teacherChangeParam) {

        // 1 获取小班课 课程最新信息
        SmallClass smallClass = smallClassJpaRepository.findOne(teacherChangeParam.getSmallClassId());

        Long oldTeacherId = smallClass.getTeacherId(); // 旧的老师ID

        // 2 获取小班课涉及的所有学生的课程
        List<WorkOrder> workOrders = workOrderJpaRepository.findBySmallClassId(smallClass.getId());

        // 3 小班课班长信息
        WorkOrder leaderWorkOrder = null;

        if (CollectionUtils.isEmpty(workOrders)) {
            return JsonResultModel.newJsonResultModel("该小班课不存在鱼卡信息");
        }

        // 4 如果上完课不进行更换老师
        for (WorkOrder wk : workOrders) {
            if (wk.getIsCourseOver() == 1) {
                throw new BusinessException("课程已经结束,不能更换教师");
            }
            if (wk.getId().longValue() == smallClass.getGroupLeaderCard().longValue()) {
                leaderWorkOrder = wk.clone();
            }
        }

        if (Objects.isNull(leaderWorkOrder)) {
            throw new BusinessException("该小班课没有班长,建班可能有问题");
        }

        // 抽取数据
        List<Long> ids = Collections3.extractToList(workOrders, "id");

        List<CourseSchedule> courseSchedules = courseScheduleRepository.findByWorkorderIdIn(ids);

        // 小班课全部进行换老师操作
        changeTeacherForWorkOrdersAndCourseSchedule(workOrders, courseSchedules, teacherChangeParam);


        // 5 班长进行换老师操作
        leaderWorkOrder.setTeacherId(teacherChangeParam.getTeacherId());
        leaderWorkOrder.setAssignTeacherTime(new Date());
        leaderWorkOrder.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
        leaderWorkOrder.setTeacherName(teacherChangeParam.getTeacherName());


        // 7 通知师生运营更换老师 传送班长课程信息
        teacherStudentRequester.notifyChangeTeacher(leaderWorkOrder, teacherChangeParam);


        //立即解散群组
        courseOnlineRequester.instantReleaseGroupForSmallClass(smallClass.getId(), workOrders);

        //通知小马添加新的群组
        List<Long> studentIds = Collections3.extractToList(workOrders, "studentId");
        smallClass.setAllStudentIds(studentIds);
        smallClass.setTeacherId(teacherChangeParam.getTeacherId());
        smallClass.setTeacherName(teacherChangeParam.getTeacherName());
        FishCardGroupsInfo fishCardGroupsInfo = courseOnlineRequester.buildsmallClassChatRoom(smallClass);
        smallClass.setGroupId(fishCardGroupsInfo.getGroupId());
        smallClass.setChatRoomId(fishCardGroupsInfo.getChatRoomId());


        //更新新的教师到workorder和courseschedule,此处做事务控制
        workOrderService.updateWorkOrdersAndSchedules(workOrders, courseSchedules, smallClass);

        // 小班课记录日志
        smallClassLogService.recordLog(smallClass, PublicClassInfoConstantStatus.TEACHER_ASSIGNED, null, RoleEnum.SYSTEM, String.format("小班课换老师或者老师请假操作操作,旧的老师ID:%s", oldTeacherId));

        //返回结果
        return JsonResultModel.newJsonResultModel(null);
    }


    private void changeTeacherForWorkOrdersAndCourseSchedule(List<WorkOrder> workOrders, List<CourseSchedule> courseSchedules, TeacherChangeParam teacherChangeParam) {
        workOrders.stream().forEach(workOrder -> {
            WorkOrder oldWorkOrder = workOrder.clone();
            workOrder.setTeacherId(teacherChangeParam.getTeacherId());
            workOrder.setAssignTeacherTime(new Date());
            workOrder.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
            workOrder.setTeacherName(teacherChangeParam.getTeacherName());

            //异步操作  // 设置指定老师申请失效
            assignTeacherFixService.disableAssignWorkOrderOut(workOrder.getId(), "换时间老师请假或者换老师");

            //调用首页接口
            dataCollectorService.updateBothChnAndFnItemAsync(workOrder.getStudentId());

            //鱼卡记录日志
            changeTeacherLog(workOrder, oldWorkOrder, "小班课");
        });


        courseSchedules.stream().forEach(courseSchedule -> {
            courseSchedule.setTeacherId(teacherChangeParam.getTeacherId());
            courseSchedule.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
        });

    }


    private void changeTeacherLog(WorkOrder workOrder, WorkOrder oldWorkOrder, String source) {
        WorkOrderLog workOrderLog = new WorkOrderLog();
        workOrderLog.setCreateTime(new Date());
        workOrderLog.setWorkOrderId(workOrder.getId());
        workOrderLog.setStatus(workOrder.getStatus());
        workOrderLog.setContent(String.format("%s更换教师:%s,旧的教师id%s", source
                , FishCardStatusEnum.getDesc(workOrder.getStatus()), oldWorkOrder.getTeacherId()));
        workOrderLogService.save(workOrderLog);
    }

    /**
     * 验证换老师的时候,验证原来老师是否处于联通 呼叫 上课  (31,32,33,34,35,37) 的状态中
     *
     * @param teacherChangeParam
     * @return
     */
    public JsonResultModel checkCurrentTeacherStatus(TeacherChangeParam teacherChangeParam) {
        if (null == teacherChangeParam.getWorkOrderId()) {
            throw new BusinessException("鱼卡id不存在");
        }
        List<WorkOrderLog> workOrderLogs = workOrderLogService.queryByWorkIdAndStaus(teacherChangeParam.getWorkOrderId());
        if (CollectionUtils.isEmpty(workOrderLogs)) {
            return JsonResultModel.newJsonResultModel(false);//不需要弹出提示 确认按钮
        }
        return JsonResultModel.newJsonResultModel(true);//需要弹出提示 确认按钮
    }

    public JsonResultModel checkCurrentTeacherStatusSmallClass(TeacherChangeParam teacherChangeParam) {
        if (null == teacherChangeParam.getSmallClassId()) {
            throw new BusinessException("小班课id不存在");
        }
        return JsonResultModel.newJsonResultModel(smallClassServiceX.checkChangeTeacherForSmallClass(teacherChangeParam.getSmallClassId()));
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
        if (CollectionUtils.isEmpty(workOrders)) {
            return;
        }
        workOrders.forEach(workOrder -> {
            fishCardModifyService.changeCourse(workOrder);
        });
    }

    public void changerderCourses(Long studentId) {
        if (!onlineAccountService.isMember(studentId)) {
            throw new NotFoundException();
        }
        try {
            List<WorkOrder> workOrders = fishCardModifyService.findByStudentIdAndStatusLessThan(studentId, FishCardStatusEnum.WAITFORSTUDENT.getCode());
            if (CollectionUtils.isEmpty(workOrders)) {
                throw new NotFoundException();
            }
            workOrders.stream()
                    // 小班课学生修改level难度, 不重新修改课程推荐
                    .filter(workOrder -> !StringUtils.equals(workOrder.getClassType(), ClassTypeEnum.SMALL.name()))
                    .forEach(workOrder -> {
                        Duration duration = Duration.between(LocalDateTime.now(ZoneId.systemDefault()),
                                DateUtil.convertLocalDateTime(workOrder.getStartTime()));
                        long hours = duration.toHours();
                        logger.info("workOrder startTime=[{}] and duration=[{}]", workOrder.getStartTime(), duration.toHours());
                        // 24小时以内如果有课,不再换课,无课则直接推荐
                        if (hours <= 24) {
                            if (StringUtils.isEmpty(workOrder.getCourseId())) {
                                fishCardModifyService.changeCourse(workOrder);
                            }
                        }
                        // 24小时以上的课,有课再换
                        else {
                            if (StringUtils.isNotEmpty(workOrder.getCourseId())) {
                                fishCardModifyService.changeCourse(workOrder);
                            }
                        }
                    });
        } catch (Exception ex) {
            logger.error("修改课程失败@changerderCourses", ex);

        }
        dataCollectorService.updateBothChnAndFnItemAsync(studentId);

    }

    public void changCourse(Long workOrderId) {
        WorkOrder workOrder = workOrderService.findOne(workOrderId);
        if (null == workOrder) {
            return;
        }
        fishCardModifyService.changeCourse(workOrder);
        dataCollectorService.updateBothChnAndFnItemAsync(workOrder.getStudentId());
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
            Long[] workOrderLongIds = new Long[workOrderIds.size()];
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
                logger.error("所选课程包含已经上过课的id,请确认后再删除[{}]", workOrder.getId());
                continue;
            }
            teacherStudentRequester.notifyCancelTeacher(workOrder);
            CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(workOrder.getId());
            workOrderLogService.saveDeleteWorkOrderLog(workOrder, courseSchedule);
            fishCardModifyService.delete(id);
            courseScheduleService.delete(courseSchedule);
        }
        for (Long id : workOrderIds) {
            WorkOrder workOrder = workOrderService.findOne(id);
            teacherStudentRequester.notifyCancelTeacher(workOrder);
        }
    }


    /**
     * 更改时间
     * <p>
     * <p>
     * 1 更改 鱼卡信息 和 课程信息
     * 2 分配老师
     * <p>
     * desc:    ********  解决问题,有可能分配老师 和 更改课程信息 导致 脏读 导致 课程鱼卡 classDate  和 StartTime 时间不一致问题
     *
     * @param startTimeParam
     * @param checkTimeflag
     * @return
     */
    public JsonResultModel changeStartTime(StartTimeParam startTimeParam, boolean checkTimeflag) {

        Long workOrderId = fishCardModifyService.changeStartTimeFishCard(startTimeParam, checkTimeflag);
        dataCollectorService.updateBothChnAndFnItemForCardId(workOrderId);
        WorkOrder workOrder = workOrderService.findOne(workOrderId) ;
        logger.info("changeStartTime 准备 分配老师 workOrderId {[]}", workOrder.getId());
        if (workOrder.getTeacherId() == 0) {
            logger.info("changeStartTime 满足 分配老师 条件 workOrderId {[]}", workOrder.getId());
            List<CourseSchedule> courseSchedules = Lists.newArrayList();
            CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(startTimeParam.getWorkOrderId());
            courseSchedules.add(courseSchedule);
            timePickerService.getRecommandTeachers(workOrder.getService(), courseSchedules);
        }
        return JsonResultModel.newJsonResultModel("OK");
    }

    /**
     *
     * @param makeUpCourseParam
     * @return
     */
    public JsonResultModel changeTeachers(MakeUpCourseParam makeUpCourseParam) {
        List<WorkOrder> workOrders = workOrderService.getAllWorkOrdersByIds(makeUpCourseParam.getWorkOrderIds());
        if (CollectionUtils.isEmpty(workOrders)) {
            return JsonResultModel.newJsonResultModel("鱼卡列表为空");
        }

        workOrders.stream().filter(
                workOrder -> (workOrder.getIsCourseOver() == 0 && workOrder.getIsFreeze() == 0)
        ).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(workOrders))
            return JsonResultModel.newJsonResultModel("没有满足可以进行更换的课程");

        workOrders.stream().forEach(workOrder -> {
            this.changeTeacherOnly(workOrder.getId());
        });

        return JsonResultModel.newJsonResultModel("OK");

    }


    public JsonResultModel changeTeacherOnly(Long workOrderId) {
        fishCardModifyService.changeTeacherOnlyFishCard(workOrderId);
        dataCollectorService.updateBothChnAndFnItemForCardId(workOrderId);
        WorkOrder workOrder = workOrderService.findOne(workOrderId);
        logger.info("changeTeacherOnly 准备 分配老师 workOrderId {[]}", workOrder.getId());
        if (workOrder.getTeacherId() == 0) {
            logger.info("changeTeacherOnly 满足 分配老师 条件 workOrderId {[]}", workOrder.getId());
            List<CourseSchedule> courseSchedules = Lists.newArrayList();
            CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(workOrderId);
            courseSchedules.add(courseSchedule);
            timePickerService.getRecommandTeachers(workOrder.getService(), courseSchedules);
        }
        return JsonResultModel.newJsonResultModel("OK");
    }

    /**
     * 短信通知老师 课程取消
     *
     * @param teahcerId
     * @param startTime
     * @param workOrder
     */
    private void sendShortMessage(Long teahcerId, String startTime, WorkOrder workOrder) {
        Map map = Maps.newHashMap();
        map.put("user_id", teahcerId);
        map.put("template_code", ShortMessageCodeConstant.SMS_TEA_NOTITY_CLASS_CANCEL_CODE);
        JSONObject jo = new JSONObject();
        jo.put("startTime", workOrder.getStartTime());
        jo.put("courseName", workOrder.getCourseName());
        jo.put("cancelReason", ShortMessageCodeConstant.CANCELREASON);
        map.put("data", jo);

        rabbitMqSender.send(map, QueueTypeEnum.SHORT_MESSAGE);
    }
}
