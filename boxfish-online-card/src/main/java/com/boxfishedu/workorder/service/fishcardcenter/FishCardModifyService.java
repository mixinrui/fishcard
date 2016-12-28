package com.boxfishedu.workorder.service.fishcardcenter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.common.util.WorkOrderConstant;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mongo.ScheduleCourseInfo;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ScheduleCourseInfoService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.service.base.BaseService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.web.param.StartTimeParam;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by hucl on 16/7/8.
 */
@Component
@SuppressWarnings("ALL")
public class FishCardModifyService extends BaseService<WorkOrder, WorkOrderJpaRepository, Long> {

    @Autowired
    private RecommandCourseRequester recommandCourseRequester;

    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private ScheduleCourseInfoService scheduleCourseInfoService;

    @Autowired
    private UrlConf urlConf;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private DataCollectorService dataCollectorService;

    @Autowired
    private CourseOnlineRequester courseOnlineRequester;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    public void changeCourse(WorkOrder workOrder) {
        logger.debug("FishCardModifyService#changeCourse:开始换课,旧的鱼卡信息[{}]", JacksonUtil.toJSon(workOrder));
        String oldCourseName = workOrder.getCourseName();

        RecommandCourseView recommandCourseView = recommandCourseRequester.changeCourse(workOrder);
        workOrder.setCourseName(recommandCourseView.getCourseName());
        workOrder.setCourseId(recommandCourseView.getCourseId());
        workOrder.setCourseType(recommandCourseView.getCourseType());

        CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(workOrder.getId());
        courseSchedule.setCourseId(workOrder.getCourseId());
        courseSchedule.setCourseName(workOrder.getCourseName());
        courseSchedule.setCourseType(workOrder.getCourseType());

        //修改课程信息
        ScheduleCourseInfo scheduleCourseInfo = scheduleCourseInfoService.queryByWorkId(workOrder.getId());
        scheduleCourseInfo.setCourseType(workOrder.getCourseType());
        scheduleCourseInfo.setCourseId(workOrder.getCourseId());
        scheduleCourseInfo.setName(workOrder.getCourseName());
        scheduleCourseInfo.setEnglishName(recommandCourseView.getEnglishName());
        scheduleCourseInfo.setDifficulty(recommandCourseView.getDifficulty());
        scheduleCourseInfo.setPublicDate(recommandCourseView.getPublicDate());
        scheduleCourseInfo.setThumbnail(recommandCourseRequester.getThumbNailPath(recommandCourseView));

        //外教不参与师生互评 jiaozijun
       // if(TeachingType.WAIJIAO.getCode() != workOrder.getSkuId() ) {
            /** 换课更新  换课时间  jiaozijun **/
            workOrder.setUpdatetimeChangecourse(new Date());
            /** 换课 1 换课消息未发送 jiaozijun **/
            workOrder.setSendflagcc("1");
        //}

        scheduleCourseInfoService.updateCourseIntoScheduleInfo(scheduleCourseInfo);
        workOrderService.saveWorkOrderAndSchedule(workOrder, courseSchedule);
        workOrderLogService.saveWorkOrderLog(workOrder, "!更换课程信息,老课程[" + oldCourseName + "]");
    }

    public List<WorkOrder> findByStudentIdAndOrderIdAndStatusLessThan(Long studentId, Long orderId, Integer status) {
        return jpa.findByStudentIdAndOrderIdAndStatusLessThan(studentId, orderId, status);
    }

    public List<WorkOrder> findByStudentIdAndStatusLessThan(Long studentId, Integer status) {
        return jpa.findByStudentIdAndStatusLessThan(studentId, status);
    }

    public List<WorkOrder> findByStudentIdAndStatusLessThanAndStartTimeAfter(Long studentId, Integer status, Date beginDate) {
        return jpa.findByStudentIdAndStatusLessThanAndStartTimeAfter(studentId, status, beginDate);
    }

    public List<CourseSchedule> findCourseSchedulesByWorkOrders(List<Long> workOrderIds) {
        return courseScheduleService.findByWorkorderIdIn((Long[]) workOrderIds.toArray());
    }

    @Transactional
    public void deleteCardsAndSchedules(List<WorkOrder> workOrders, List<CourseSchedule> courseSchedules) {
        this.delete(workOrders);
        courseScheduleService.delete(courseSchedules);
    }


    /**
     * 更改上课时间点
     * @param startTimeParam
     * @param checkTimeflag  是否需要验证时间
     * @return
     */
    @Transactional
    public Long  changeStartTimeFishCard(StartTimeParam startTimeParam, boolean checkTimeflag){
        String source = checkTimeflag ? "APP端":"后台";
        Map<String,String> resultMap = Maps.newHashMap();
        // 检查日期合法化
        boolean  dataFlag = checkDate(startTimeParam);
        if(!dataFlag){
            throw new BusinessException("日期不合法");
        }

        //获取鱼卡信息
        WorkOrder workOrder =workOrderService.findByIdForUpdate(startTimeParam.getWorkOrderId())   ;
        CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(startTimeParam.getWorkOrderId());

        if(null == workOrder || courseSchedule ==null){
            throw new BusinessException("课程信息不存在");
        }

        if(checkTimeflag) {
            boolean afterTomo = afterTomoDate(workOrder);
            if (!afterTomo) {
                throw new BusinessException("请提前48小时修改上课时间，有任何问题请电话联系客服～");
            }
        }

        Long teacherId = workOrder.getTeacherId();
        String startTime = DateUtil.Date2String(  workOrder.getStartTime());

        // 验证鱼卡状态 创建、分配课程、分配老师

        //获取结束时间
        startTimeParam.setEndDateFormat(     DateUtil.String2Date(startTimeParam.getEndDate()) );

        //记录老教师,时间
        Long oldTeacherId= workOrder.getTeacherId();
        String oldStartTime= StringUtils.EMPTY;
        if(null!=workOrder.getStartTime()){
            oldStartTime=DateUtil.Date2String (workOrder.getStartTime());
        }
        String oldTeacherName= StringUtils.EMPTY;
        if(!StringUtils.isEmpty(workOrder.getTeacherName())){
            workOrder.getTeacherName();
        }

        //分配教师以后其实就已经是就绪,目前这两个状态有重叠
        if(workOrder.getStatus()== FishCardStatusEnum.CREATED.getCode() || workOrder.getStatus()==FishCardStatusEnum.COURSE_ASSIGNED.getCode() || workOrder.getStatus()==FishCardStatusEnum.TEACHER_ASSIGNED.getCode()){


            //******************* 先  进行1 ,2  在进行跟换鱼卡信息
            // 1 通知师生运营释放教师资源
            teacherStudentRequester.notifyCancelTeacher(workOrder);
            //2 通知小马解散师生关系
            courseOnlineRequester.releaseGroup(workOrder);



            if(workOrder.getStatus()!=FishCardStatusEnum.CREATED.getCode()){
                workOrder.setStatus(FishCardStatusEnum.COURSE_ASSIGNED.getCode());
                courseSchedule.setStatus( FishCardStatusEnum.COURSE_ASSIGNED.getCode());
            }

            // 如果设置提醒 ,删除提醒操作
            if(null!=workOrder.getNeedChangeTime()){
                workOrder.setNeedChangeTime(null);
            }

            if(null!=courseSchedule.getNeedChangeTime()){
                courseSchedule.setNeedChangeTime(null);
            }

            workOrder.setTeacherId(0L);
            workOrder.setTeacherName("");
            workOrder.setStartTime(startTimeParam.getBeginDateFormat() );
            workOrder.setEndTime(startTimeParam.getEndDateFormat() );
            workOrder.setSlotId(startTimeParam.getTimeslotId());
            workOrder.setChangtimeTimes(workOrder.getChangtimeTimes()==null?(new Integer(1)):(workOrder.getChangtimeTimes()+1));// 换课次数
            workOrderService.save(workOrder);

            courseSchedule.setClassDate(DateUtil.String2SimpleDate(startTimeParam.getBeginDate()));
            courseSchedule.setStartTime(startTimeParam.getBeginDateFormat() );
            logger.info("changeStartTime : [{}] ,workOrderId : [{}]",DateUtil.String2SimpleDate(startTimeParam.getBeginDate()),workOrder.getId());
            courseSchedule.setTeacherId(0L);
            courseSchedule.setTimeSlotId(startTimeParam.getTimeslotId() );

            courseScheduleService.save(courseSchedule);

            // 推送教师更换时间推送
            if(null!=teacherId && teacherId>0L){
                this.pushTeacherList(teacherId,startTime);
                // 发送短信   this.sendShortMessage(teacherId,startTime,workOrder);
            }

        }else {
            throw new BusinessException("课程信息不能修改");
        }


        // 记录日志
        workOrderLogService.saveWorkOrderLog(workOrder,"更换换时间#旧的上课时间["+oldStartTime+"], #当前上课时间 ["+startTimeParam.getBeginDate()+"] ,#旧的教师id["+oldTeacherId+"],#旧的教师姓名["+oldTeacherName+"]"+",修改时间来源:"+source);

        return workOrder.getId();
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

//        String minutes = startTimeParam.getBeginDate().substring(14,16);
//        if(minutes.equals("00") || minutes.equals("30")){
//        }else {
//            return false;
//        }

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
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(teahcerId);
        map1.put("user_id", jsonArray);

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

    public static void main(String[] args) {
        List list= Lists.newArrayList();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObjectinner = new JSONObject();
        JSONArray jsonArray = new JSONArray();
    }

    /**
     * 开始时间从后天开始
     * @param workOrder
     * @return
     */
    private boolean afterTomoDate(WorkOrder workOrder){
        Date end  = DateUtil.addMinutes(new Date(),60*24*2);
        if(workOrder.getStartTime()  .after(end)){
            return true;
        }
        return false;
    }

}
