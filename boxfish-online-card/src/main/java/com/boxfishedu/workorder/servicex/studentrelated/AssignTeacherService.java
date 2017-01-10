package com.boxfishedu.workorder.servicex.studentrelated;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.bean.AssignTeacherApplyStatusEnum;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.Collections3;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.common.util.WorkOrderConstant;
import com.boxfishedu.workorder.dao.jpa.StStudentApplyRecordsJpaRepository;
import com.boxfishedu.workorder.dao.jpa.StStudentSchemaJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.*;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.requester.TeacherPhotoRequester;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.StStudentApplyRecordsService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.studentrelated.TimePickerService;
import com.boxfishedu.workorder.servicex.assignTeacher.AssignTeacherServiceX;
import com.boxfishedu.workorder.web.filter.ParentRelationGetter;
import com.boxfishedu.workorder.web.param.ScheduleBatchReqSt;
import com.boxfishedu.workorder.web.param.StTeacherInviteParam;
import com.boxfishedu.workorder.web.param.StudentTeacherParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.fishcard.FishCardGroupsInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jiaozijun on 16/12/14.
 */
@Component
public class AssignTeacherService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private StStudentApplyRecordsService stStudentApplyRecordsService;

    @Autowired
    private TeacherPhotoRequester teacherPhotoRequester;

    @Autowired
    private StStudentSchemaJpaRepository stStudentSchemaJpaRepository;


    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private TimePickerService timePickerService;

    @Autowired
    private ServeService serveService;

    @Autowired
    private StStudentApplyRecordsJpaRepository stStudentApplyRecordsJpaRepository;

    @Autowired
    private AssignTeacherServiceX assignTeacherServiceX;


    /**
     * 1 判断按钮是否出现
     *
     * @param workOrderId
     * @return
     */
    public JsonResultModel checkAssignTeacherFlag(Long workOrderId) {
        List<WorkOrder> workOrders = this.getAssignTeacherList(workOrderId);
        if (CollectionUtils.isEmpty(workOrders)) {
            return JsonResultModel.newJsonResultModel(false);
        } else {
            return JsonResultModel.newJsonResultModel(true);
        }

    }


    public List<WorkOrder> getAssignTeacherList(Long workOrderId) {
        WorkOrder workOrder = workOrderService.findOne(workOrderId);//刚刚上过的课的鱼卡
        if (workOrder == null) {
            throw new BusinessException("课程信息有误");
        }

        Boolean isActive = teacherStudentRequester.checkTeacherIsFreeze(workOrder.getTeacherId());
        if (!isActive || isActive == null) {
            logger.info("getAssignTeacherList 老师被冻结或者调用 师生运营接口失败");
            return null;
        }

        //判断旧鱼卡是否已经上完课
        if (workOrder.getStartTime().after(new Date())) {
            throw new BusinessException("该课程尚未结束");
        }


        //该学生 是否 同类型课 是否指定过老师
        StStudentSchema stStudentSchema = stStudentSchemaJpaRepository.findTop1ByStudentIdAndStSchemaAndSkuId(workOrder.getStudentId(), StStudentSchema.StSchema.assgin, StStudentSchema.CourseType.getEnum(workOrder.getSkuId()));
        //用于判断 是否为同类型最后一节课
        List<WorkOrder> listWorkOrders = workOrderService.findByStartTimeMoreThanAndSkuIdAndIsFreeze(workOrder);

        // 未指定过该类型课的老师   或者 指定该类型课的老师 和 改刚刚上过课的老师 不是一个人
        if (null == stStudentSchema || !stStudentSchema.getTeacherId().equals(workOrder.getTeacherId())) {
            if (CollectionUtils.isEmpty(listWorkOrders)) {
                return null;
            } else {
                return listWorkOrders;
            }
        } else {
            return null;
        }
    }


    //2.1  指定这位老师上课
    @Transactional
    public JsonResultModel matchCourseInfoAssignTeacher(Long oldWorkOrderId, Integer skuIdParameter, Long studentId, Long teacherId) {
        logger.info("matchCourseInfoAssignTeacher oldWorkOrderId:[{}],skuIdParameter:[{}],studentId:[{}],teacherId:[{}]", oldWorkOrderId, skuIdParameter, studentId, teacherId);
        Integer skuId = skuIdParameter;
        if (null != oldWorkOrderId) {
            WorkOrder workOrder = workOrderService.findOne(oldWorkOrderId);
            skuId = workOrder.getSkuId();
        }

        if(skuId==null || teacherId==null || teacherId==0){
            throw new BusinessException("数据参数不合法");
        }

        // 该学生
        // 同类型(中外教)
        // 其他教师接受邀请(待确认)设为无效                                     后续版本添加推送消息 通知其他教师 邀请取消
        int num = stStudentApplyRecordsJpaRepository.setFixedValidFor(StStudentApplyRecords.VALID.no, studentId, teacherId, StStudentApplyRecords.VALID.yes, StStudentApplyRecords.MatchStatus.wait2apply,skuId);
        logger.info("matchCourseInfoAssignTeacher redonum:[{}]", num);

        //分配
        assignTeacherServiceX.insertOrUpdateSchema(studentId, teacherId, skuId, StStudentSchema.StSchema.assgin);
        assignTeacherServiceX.maualAssign(teacherId, studentId, skuId);

        return JsonResultModel.newJsonResultModel(null);
    }

    //2 获取指定老师带的课程列表
    public JsonResultModel getAssginTeacherCourseList(Long oldWorkOrderId, Long studentId, Long teacherId, Pageable pageable) {
        WorkOrder workOrder = workOrderService.getOne(oldWorkOrderId);
        if (null == workOrder || null == workOrder.getSkuId()) {
            throw new BusinessException("课程信息有误");
        }
        Date startTime = DateTime.now().plusHours(48).toDate();
        Page<CourseSchedule> courseSchedulePage = courseScheduleService.findAssignCourseScheduleByStudentId(studentId, startTime, workOrder.getSkuId(), pageable);
        trimPage(courseSchedulePage);
        return JsonResultModel.newJsonResultModel(courseSchedulePage);
    }


    //2.2 获取指定老师带的课程列表
    public JsonResultModel getAssginTeacherCourseListnew(Long oldWorkOrderId, Long studentId, Long teacherId, Long orderId, Pageable pageable) {
        Page<CourseSchedule> courseSchedulePage = courseScheduleService.findAssignCourseScheduleByStudentId(orderId, pageable);
        trimPage(courseSchedulePage);
        return JsonResultModel.newJsonResultModel(courseSchedulePage);
    }


    // 2.1 组装向师生运营发送的消息内容   teacherId 指定的老师id
    public ScheduleBatchReqSt makeScheduleBatchReqSt(Long oldWorkOrderId, Long teacherId) {
        ScheduleBatchReqSt scheduleBatchReqSt = new ScheduleBatchReqSt();
        List<WorkOrder> workOrders = this.getAssignTeacherList(oldWorkOrderId);
        if (CollectionUtils.isEmpty(workOrders))
            return null;

        // 获取和该 workOrders 未冻结 开始时间相同 并且为指定老师的鱼卡信息
        List startTimelist = Lists.newArrayList();
        workOrders.forEach(w -> {
            startTimelist.add(w.getStartTime());
        });
        List<WorkOrder> workOrdersB = workOrderService.getMatchWorkOrders(teacherId, startTimelist);

        return scheduleBatchReqSt;
    }

    /**
     * 加工课程是否匹配情况
     *
     * @param page
     */
    private void trimPage(Page<CourseSchedule> page) {
        List<CourseSchedule> courseSchedules = (List<CourseSchedule>) page.getContent();
        List<Long> workOrderIds = Collections3.extractToList(courseSchedules, "workorderId");
        List<StStudentApplyRecords> stStudentApplyRecordses = stStudentApplyRecordsJpaRepository.findByWorkOrderIdIn(workOrderIds);

        Map<Long, StStudentApplyRecords.MatchStatus> map = Collections3.extractToMap(stStudentApplyRecordses, "workOrderId", "matchStatus");
        ((List<CourseSchedule>) page.getContent()).forEach(courseSchedule -> {
            courseSchedule.setMatchStatus(map.get(courseSchedule.getWorkorderId()) == StStudentApplyRecords.MatchStatus.matched ? 1 : 0);
        });
    }


    //3 开始上课界面接口
    public CourseInfo getCourseInfo(Long workOrderId) {
        //1 获取鱼卡信息
        WorkOrder workOrder = workOrderService.findOne(workOrderId);
        if (null == workOrder) {
            throw new BusinessException("课程信息有误");
        }

        CourseInfo courseInfo = new CourseInfo();
        //2 获取是否本课为指定老师
        StStudentSchema stStudentSchema = stStudentSchemaJpaRepository.findByStudentIdAndTeacherIdAndSkuIdAndStSchema(workOrder.getStudentId(), workOrder.getTeacherId(), StStudentSchema.CourseType.getEnum(workOrder.getSkuId()), StStudentSchema.StSchema.assgin);
        if (null != stStudentSchema) {
            courseInfo.setAssignFlag(true);// 指定老师
        } else {
            courseInfo.setAssignFlag(false);// 非指定老师
        }


        //3 获取教师信息 老师头像 老师姓名
        if (workOrder.getTeacherId() != null && workOrder.getTeacherId() > 0) {
            courseInfo.setTeacherId(workOrder.getTeacherId());   // 设置教师ID
            Map<String, String> teacherInfoMap = teacherPhotoRequester.getTeacherInfo(workOrder.getTeacherId());
            courseInfo.setTeacherId(workOrder.getTeacherId());
            if (!CollectionUtils.isEmpty(teacherInfoMap)) {
                courseInfo.setTeacherImg(teacherInfoMap.get("figure_url"));// 教师头像url
            }

            courseInfo.setTeacherName(getTeacherName(workOrder.getTeacherId())); // 教师姓名

        }


        //4 获取课程信息 词义数 阅读量 听力时长
        if (!StringUtils.isEmpty(workOrder.getCourseId())) {
            courseInfo.setCourseId(workOrder.getCourseId());
            Map<String, Integer> courseMap = teacherPhotoRequester.getCourseInfo(workOrder.getCourseId());
            if (!CollectionUtils.isEmpty(courseMap)) {
//                private Integer wordNum; //词义数
//                private Integer readNum; //阅读量
//                private Integer listenNum;//积分
                courseInfo.setWordNum(courseMap.get("multiwordCount")); //词义数
                courseInfo.setReadNum(courseMap.get("readWordCount"));//阅读量
                courseInfo.setScoreNum(courseMap.get("score"));//积分

            }
        }


        return courseInfo;

    }


    //4 查看我的邀请数量(学生的数量 未读)
    public Integer getMyInvited(Long teacherId) {
        if (null == teacherId) {
            throw new BusinessException("您非法登陆,或者已经掉线");
        }
        Date baseDate = DateTime.now().minusHours(48).toDate();
        Date beginDate = DateTime.now().toDate();
        Date endDate = DateUtil.getNextWeekSunday(DateTime.now().toDate());
        return stStudentApplyRecordsService.getUnreadInvitedNum(teacherId, baseDate, beginDate, endDate);
    }

    // 5 老师端上课邀请列表
    public Page<StStudentApplyRecordsResult> getmyInviteList(Long teacherId, Pageable pageable) {

        Date baseDate = DateTime.now().minusHours(48).toDate();
        Date beginDate = DateTime.now().toDate();
        Date endDate = DateUtil.getNextWeekSunday(DateTime.now().toDate());
        logger.info("getmyInviteList teacherId:[{}],baseDate:[{}],beginDate:[{}],endDate:[{}],", teacherId,
                DateUtil.Date2String24(baseDate), DateUtil.Date2String24(beginDate), DateUtil.Date2String24(endDate));

        Page<StStudentApplyRecordsResult> results = stStudentApplyRecordsService.getmyInviteList(teacherId, baseDate, beginDate, endDate, pageable);

        if (results == null || null == results.getContent())
            return results;

        ((List<StStudentApplyRecordsResult>) results.getContent()).stream().forEach(rs -> {
            Map<String, String> teacherInfoMap = teacherPhotoRequester.getTeacherInfo(rs.getStudentId());
            if (!CollectionUtils.isEmpty(teacherInfoMap)) {
                rs.setStudentImg(teacherInfoMap.get("figure_url"));
                rs.setStudentName(teacherInfoMap.get("nickname"));
                rs.setSystemTime(DateTime.now().toDate());
            }
        });

        return results;
    }

    // 6 5的详情  学生的邀请
    @Transactional
    public Page<StStudentApplyRecords> getMyClassesByStudentId(Long teacherId, Long studentId, Pageable pageable) {
        Date baseDate = DateTime.now().minusHours(48).toDate();
        Date beginDate = DateTime.now().toDate();
        Date endDate = DateUtil.getNextWeekSunday(DateTime.now().toDate());

        Page<StStudentApplyRecords> results = stStudentApplyRecordsService.getMyClassesByStudentId(teacherId, studentId, baseDate, beginDate, endDate, pageable);
        // 更新已读状态
        List fishcardids = Lists.newArrayList();
        if (results != null && null != results.getContent()) {
            ((List<StStudentApplyRecords>) results.getContent()).stream().forEach(st -> {
                fishcardids.add(st.getWorkOrderId());
            });
        }

        if (!CollectionUtils.isEmpty(fishcardids)) {
            List<WorkOrder> workOrders = workOrderService.findByIdIn(fishcardids);
            ((List<StStudentApplyRecords>) results.getContent()).stream().forEach(st -> {
                workOrders.stream().forEach(workOrder -> {
                    if (workOrder.getId().equals(st.getWorkOrderId())) {
                        st.setStartTime(workOrder.getStartTime());
                        st.setEndTime(workOrder.getEndTime());
                        st.setTimeSlotId(workOrder.getSlotId());
                    }
                });

            });

        }

        // 排序
        // this.getSortOrders(  ((List<StStudentApplyRecords>) results.getContent()));
        int readNum = stStudentApplyRecordsService.upateReadStatusByStudentId(teacherId, studentId);
        logger.info("getMyClassesByStudentId:num:[{}],teacherId:[{}],studentId:[{}]", readNum, teacherId, studentId);
        return results;
    }


    // 11 检查是否还有申请记录
    @Transactional
    public List<StStudentApplyRecords> checkMyClassesByStudentId(Long teacherId, Long studentId) {
        Date baseDate = DateTime.now().minusHours(48).toDate();
        Date beginDate = DateTime.now().toDate();
        Date endDate = DateUtil.getNextWeekSunday(DateTime.now().toDate());
        List<StStudentApplyRecords> results = stStudentApplyRecordsService.getMyClassesByStudentId(teacherId, studentId, baseDate, beginDate, endDate);
        return results;
    }

    public JSONObject getMyLastAssignTeacher(Long studentId, Integer skuId) {
        if (studentId == null || null == skuId) {
            throw new BusinessException("课程信息有误");
        }

        StStudentSchema stStudentSchema = stStudentSchemaJpaRepository.findTop1ByStudentIdAndStSchemaAndSkuId(studentId, StStudentSchema.StSchema.assgin, StStudentSchema.CourseType.getEnum(skuId));

//        StStudentApplyRecords stStudentApplyRecords = stStudentApplyRecordsService.findMyLastAssignTeacher(studentId, skuId);

        JSONObject jo = new JSONObject();
        if (stStudentSchema == null) {
            jo.put("hasAssignTeacher", false);
            return jo;
        }

        jo.put("hasAssignTeacher", true);

        Boolean isActive = teacherStudentRequester.checkTeacherIsFreeze(stStudentSchema.getTeacherId());
        if (!isActive || isActive == null) {
            logger.info("getAssignTeacherList 老师被冻结或者调用 师生运营接口失败");
            jo.put("hasAssignTeacher", false);
        }


        Map<String, String> teacherInfoMap = teacherPhotoRequester.getTeacherInfo(stStudentSchema.getTeacherId());
        if (!CollectionUtils.isEmpty(teacherInfoMap)) {
            jo.put("teacherImg", teacherInfoMap.get("figure_url"));
        }

        jo.put("teacherId", stStudentSchema.getTeacherId());
        jo.put("sku_id", stStudentSchema.getSkuId().ordinal());


        jo.put("teacherName", getTeacherName(stStudentSchema.getTeacherId()));
        return jo;

    }


    /**
     * 老师端 接受上课邀请 只处理申请时间在48小时+1分钟(网络等其他延迟时间)
     *
     * @param stTeacherInviteParam
     * @return
     */
    @Transactional
    public JsonResultModel acceptInvitedCourseByStudentId(StTeacherInviteParam stTeacherInviteParam, Long teacherId) {
        List<StStudentApplyRecords> stStudentApplyRecordsList = stStudentApplyRecordsJpaRepository.getAllStStudentRecords( StStudentApplyRecords.VALID.yes,stTeacherInviteParam.getIds());

        if (CollectionUtils.isEmpty(stStudentApplyRecordsList)) {
            throw new BusinessException("没有查询到匹配的课程");
        }

        Date now = new Date();
        stStudentApplyRecordsList.stream().filter(stStu ->
                checkTimeOutForInvitedClass(stStu, now)
        ).collect(Collectors.toList());

        //已经全部超时
        if (CollectionUtils.isEmpty(stStudentApplyRecordsList)) {
            throw new BusinessException("课程已经全部超时");
            //return JsonResultModel.newJsonResultModel(null);
        }


        List<Long> ids = Collections3.extractToList(stStudentApplyRecordsList, "id");
        logger.info("acceptInvitedCourseByStudentId 能够接受的id [{}]", ids.toArray());


        Map<Long, Long> teachers = Collections3.extractToMap(stStudentApplyRecordsList, "teacherId", "teacherId");

        Map<Long, Long> students = Collections3.extractToMap(stStudentApplyRecordsList, "studentId", "studentId");

        Date baseDate = DateTime.now().minusHours(48).toDate();
        Date beginDate = DateTime.now().toDate();
        Date endDate = DateUtil.getNextWeekSunday(DateTime.now().toDate());

        // 需要更新 agree 状态的数据
        List<StStudentApplyRecords> resultsAll = stStudentApplyRecordsService.getMyClassesByStudentIdNoPage(teacherId, stStudentApplyRecordsList.get(0).getStudentId(), baseDate, beginDate, endDate);

        List<Long> idsneedAgree = Collections3.extractToList(resultsAll, "id");
        if (null == teachers.get(teacherId) || teachers.size() > 1 || null == students || students.size() > 1) {
            logger.info("acceptInvitedCourseByStudentId teacherId:[{}], teachers :[{}] ,students :[{}]", teacherId, JSON.toJSONString(teachers), JSON.toJSONString(students));
            throw new BusinessException("您的访问不安全,请联系客服");
        }

        // 判断是否为指定老师的数据  不是指定老师的数据  数据设置成失效
        StStudentSchema stStudentSchema = stStudentSchemaJpaRepository.findTop1ByStudentIdAndStSchemaAndSkuId(stStudentApplyRecordsList.get(0).getStudentId(), StStudentSchema.StSchema.assgin, StStudentSchema.CourseType.getEnum(stStudentApplyRecordsList.get(0).getSkuId()));
        if (null == stStudentSchema) {
            logger.info("acceptInvitedCourseByStudentId->studentId:[{}],teacherId:[{}]", stStudentApplyRecordsList.get(0).getStudentId(), stStudentApplyRecordsList.get(0).getTeacherId());
            // 数据设置为失效
            stStudentApplyRecordsJpaRepository.setFixedValidFor(StStudentApplyRecords.VALID.no, idsneedAgree);
            return JsonResultModel.newJsonResultModel(null);
        }


        // 对接受的课程进行 设置 老师已经同意 ----------->>>> 该老师对应该学生的记录 都变成agreee  只能接受一次
        int updateNum = stStudentApplyRecordsJpaRepository.setFixedApplyStatusFor(StStudentApplyRecords.ApplyStatus.agree, idsneedAgree);

        logger.info("acceptInvitedCourseByStudentId: updateNum [{}] ,ids:[{}]", updateNum, stTeacherInviteParam.getIds());
        // 未匹配上的进行匹配

        stStudentApplyRecordsList.stream().filter(sts -> sts.getMatchStatus().equals(StStudentApplyRecords.MatchStatus.wait2apply)).collect(Collectors.toList());

        Long studentId = stStudentApplyRecordsList.get(0).getStudentId();
        //List<Long> courseScheleIds = Collections3.extractToList(stStudentApplyRecordsList, "courseScheleId");
        List<Long> workOrderIds = Collections3.extractToList(stStudentApplyRecordsList, "workOrderId");
        return assignTeacherServiceX.teacherAccept(teacherId, studentId, workOrderIds);
    }


    /**
     * 判断是否在有效期内
     * 48小时+1分钟
     *
     * @param applyRecords
     * @return
     */
    private boolean checkTimeOutForInvitedClass(StStudentApplyRecords applyRecords, Date now) {
        if (null == applyRecords || null == applyRecords.getApplyTime()) {
            return false;
        }
        if (DateUtil.addMinutes(applyRecords.getApplyTime(), 60 * 24 * 2 + 1).after(now)) {
            return true;
        }
        return false;
    }


    // 9 app换个老师
    @Transactional
    public JsonResultModel changeATeacher(StudentTeacherParam studentTeacherParam) {
        if (null == studentTeacherParam.getOrderId() || null == studentTeacherParam.getTeacherId()
                || null == studentTeacherParam.getStudentId()) {
            throw new BusinessException("数据参数不全");
        }

        Service service = serveService.findTop1ByOrderId(studentTeacherParam.getOrderId());

        if (null == service) {
            logger.info("changeATeacher 订单id:[{}]", studentTeacherParam.getOrderId());
            throw new BusinessException("数据有误");
        }

        logger.info("changeATeacher studentTeacherParam:getStudentId:[{}],getOrderId:[{}],getTeacherId:[{}]", studentTeacherParam.getStudentId(), studentTeacherParam.getOrderId(), studentTeacherParam.getTeacherId());

        String tutorType = service.getTutorType();
        Integer SkuId = 0;
        logger.debug("changeATeacher,参数tutorType[{}]", tutorType);
        if (Objects.equals(tutorType, TutorType.CN.name()) || Objects.equals(tutorType, TutorType.MIXED.name())) {
            SkuId = 1; //中教
        } else {
            SkuId = 2;   // 外教
        }


        // 该学生 同类型课程的 未被接受的  设为无效
        int num = stStudentApplyRecordsJpaRepository.setFixedValidFor(StStudentApplyRecords.VALID.no, studentTeacherParam.getStudentId(), SkuId, StStudentApplyRecords.VALID.yes, StStudentApplyRecords.MatchStatus.wait2apply);
        logger.info("changeATeacher redonum:[{}]", num);

        //更新schema模式为自由模式  un_assign
        assignTeacherServiceX.insertOrUpdateSchema(studentTeacherParam.getStudentId(), studentTeacherParam.getTeacherId(), SkuId, StStudentSchema.StSchema.un_assgin);

        JsonResultModel jsonResultModel = teacherStudentRequester.notifyAssignTeacher(studentTeacherParam);

        if (null != jsonResultModel && HttpStatus.OK.value() == jsonResultModel.getReturnCode()) {
            logger.info("changeATeacher studentTeacherParam:getStudentId:[{}],getOrderId:[{}],getTeacherId:[{}]", studentTeacherParam.getStudentId(), studentTeacherParam.getOrderId(), studentTeacherParam.getTeacherId());
            // 获取订单数据
            List<WorkOrder> workOrders = workOrderService.getAllWorkOrdersByOrderId(studentTeacherParam.getOrderId());
            List<Long> workOrderIds = Collections3.extractToList(workOrders, "id");

            // 获取课程数据
            List<CourseSchedule> courseSchedules = courseScheduleService.findByWorkorderIdIn(workOrderIds);

            // 分配老师
            timePickerService.getRecommandTeachers(service, courseSchedules);
        }

        return JsonResultModel.newJsonResultModel("OK");
    }


    public String getTeacherName(Long teacherId) {
        if (null == teacherId)
            return "";
        return teacherStudentRequester.getTeacherName(teacherId);
    }


    /**
     * 给老师进行消息推送
     *
     * @param teahcerId
     */
    public void pushTeacherList(Long teahcerId) {
        logger.info("pushTeacherList::begin");

        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObjectData = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        String pushTitle = WorkOrderConstant.SEND_ASSIGN_TEACHER;

        jsonArray.add(teahcerId);
        jsonObject.put("user_id", jsonArray);
        jsonObject.put("push_title", pushTitle);
        jsonObjectData.put("push_title", pushTitle);
        jsonObjectData.put("type", "ASSIGNTEACHER");
        jsonObjectData.put("user_id", teahcerId);
        jsonObject.put("data", jsonObjectData);

        teacherStudentRequester.pushTeacherListOnlineMsgnew(jsonObject);
        logger.info("pushTeacherList::end");
    }

}
