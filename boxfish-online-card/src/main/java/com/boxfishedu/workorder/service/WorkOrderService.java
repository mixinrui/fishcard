package com.boxfishedu.workorder.service;

import com.boxfishedu.card.bean.TeachingType;
import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.common.bean.FishCardChargebackStatusEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.exception.BoxfishException;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.base.BaseService;
import com.boxfishedu.workorder.service.studentrelated.TimePickerService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.servicex.courseonline.CourseOnlineServiceX;
import com.boxfishedu.workorder.web.param.FishCardFilterParam;
import com.boxfishedu.workorder.web.view.course.CourseView;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.boxfishedu.workorder.web.view.course.ServiceWorkOrderCombination;
import com.boxfishedu.workorder.web.view.fishcard.WorkOrderView;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by hucl on 16/3/31.
 */
@Component
public class WorkOrderService extends BaseService<WorkOrder, WorkOrderJpaRepository, Long> {
    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private ScheduleCourseInfoService scheduleCourseInfoService;

    @Autowired
    private ServeService serveService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CourseType2TeachingTypeService courseType2TeachingTypeService;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    @Autowired
    private TimePickerService timePickerService;

    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    CourseOnlineServiceX courseOnlineServiceX;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Page<WorkOrder> findByServiceIdOrderByStartTime(Long serviceId, Pageable pageable) {
        return jpa.findByServiceIdOrderByStartTime(serviceId, pageable);
    }

    public WorkOrder findByOrderIdAndServiceId(Long orderId, Long serviceId) {
        return jpa.findByOrderIdAndServiceId(orderId, serviceId);
    }

    public WorkOrder findByIdForUpdate(Long id) {
        return jpa.findByIdForUpdate(id);
    }

    public Page<WorkOrder> findByTeacherIdAndStatusAndCreateTimeBetween(Pageable pageable, Long teacherId, Integer status, Date beginDate, Date endDate) {
        return jpa.findByTeacherIdAndStatusAndCreateTimeBetween(pageable, teacherId, status, beginDate, endDate);
    }

    public Page<WorkOrder> findByTeacherIdAndStatusLessThanAndCreateTimeBetween(Pageable pageable, Long teacherId, Integer status, Date beginDate, Date endDate) {
        return jpa.findByTeacherIdAndStatusLessThanAndCreateTimeBetween(pageable, teacherId, status, beginDate, endDate);
    }

    public Page<WorkOrder> findByTeacherIdAndCreateTimeBetween(Pageable pageable, Long teacherId, Date beginDate, Date endDate) {
        return jpa.findByTeacherIdAndCreateTimeBetween(pageable, teacherId, beginDate, endDate);
    }

    public List<WorkOrder> findByTeacherIdAndCreateTimeBetween(Long teacherId, Date beginDate, Date endDate) {
        return jpa.findByTeacherIdAndCreateTimeBetween(teacherId, beginDate, endDate);
    }

    public Page<WorkOrder> findByTeacherIdAndStartTimeBetweenOrderByStartTime(Long teacherId, Date startDate, Date endDate, Pageable pageable) {
        return jpa.findByTeacherIdAndStartTimeBetweenOrderByStartTime(teacherId, startDate, endDate, pageable);
    }

    public List<WorkOrder> findByCourseType(String courseType) {
        return jpa.findByCourseType(courseType);
    }

    public WorkOrder findBySmallClassIdAndStudentId(Long smallClassId, Long studentId) {
        return jpa.findBySmallClassIdAndStudentId(smallClassId, studentId);
    }

    public List<WorkOrder> findBySmallClassId(Long smallClassId) {
        return jpa.findBySmallClassId(smallClassId);
    }

    @Transactional
    public void saveWorkOrderAndSchedule(WorkOrder workOrder, CourseSchedule courseSchedule) {
        this.save(workOrder);
        courseScheduleService.save(courseSchedule);
        logger.info("||||||鱼卡[{}]入库成功;排课表入库成功[{}]", workOrder.getId(), courseSchedule.getId());
    }

    @Transactional
    public void saveStatusForCardAndSchedule(WorkOrder workOrder) {
        this.saveStatusForCardAndSchedule(workOrder, FishCardStatusEnum.getDesc(workOrder.getStatus()));
    }

    @Transactional
    public void saveStatusForCardAndSchedule(WorkOrder workOrder, String desc) {
        this.dealStudentAbsent(workOrder, desc);

        CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(workOrder.getId());
        courseSchedule.setStatus(workOrder.getStatus());
        if (workOrder.statusFinished()) {
            if (workOrder.reachOverTime()) {
                if (workOrder.isCourseNotOver()) {
                    workOrder.setIsCourseOver((short) 1);
                    courseOnlineServiceX.completeCourse(workOrder, courseSchedule, workOrder.getStatus());
                }
            } else {
                workOrderLogService.saveWorkOrderLog(
                        workOrder, "未到下课时间,客户端上报正常完成,不做处理");
                throw new BusinessException("未到下课时间,客户端上报正常完成,不做处理");
            }
        }
        recordFishCardLog(workOrder, desc);
    }

    private void dealStudentAbsent(WorkOrder workOrder, String desc) {
        if (workOrder.isStudentAbsent()) {
            recordFishCardLog(workOrder, String.join(",", "学生已旷课,不允许覆盖状态", desc));
            throw new BusinessException("鱼卡[{}]已经旷课,不可更新数据库数据");
        }
    }

    private void recordFishCardLog(WorkOrder workOrder, String desc) {
        threadPoolManager.execute(new Thread(() -> {
            workOrderLogService.saveWorkOrderLog(
                    workOrder, desc);
        }));
    }

    public boolean isWorkOrderValid(Long workOrderId) throws BoxfishException {
        WorkOrder workOrder = jpa.findOne(workOrderId);
        if (null == workOrder) {
            return false;
        }
        Date endDate = workOrder.getEndTime();
        if (endDate.before(new Date())) {
            return false;
        }
        return true;
    }

    public List<WorkOrder> findByStatusAndClassDate(Integer status, Date classDate) {
        //return jpa.findByStatusAndClassDate(status, classDate);
        return null;
    }

    @Transactional
    public void updateWorkOrderAndSchedule(WorkOrder workOrder, CourseSchedule courseSchedule) {
        this.save(workOrder);
        courseScheduleService.save(courseSchedule);
    }

    @Transactional
    public void updateWorkOrdersAndSchedules(List<WorkOrder> workOrders, List<CourseSchedule> courseSchedules, SmallClass smallClass) {
        this.save(workOrders);
        courseScheduleService.save(courseSchedules);
        smallClassJpaRepository.save(smallClass);
    }

    private void batchUpdateCourseSchedule(
            Service service, List<WorkOrder> workOrders,
            Map<WorkOrder, CourseView> workOrderCourseViewMapParam) {
        // 重算hash值,不然会出错
        HashMap<WorkOrder, CourseView> workOrderCourseViewMap = Maps.newHashMap();
        workOrderCourseViewMapParam.forEach(workOrderCourseViewMap::put);

        List<CourseSchedule> courseSchedules = new ArrayList<>();
        for (WorkOrder workOrder : workOrders) {
            CourseSchedule courseSchedule = new CourseSchedule();
            if (workOrder.getTeacherId() != 0l) {
                courseSchedule.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
            } else {
                courseSchedule.setStatus(FishCardStatusEnum.CREATED.getCode());
            }
            courseSchedule.setStudentId(service.getStudentId());
            courseSchedule.setTeacherId(workOrder.getTeacherId());

            CourseView courseView = workOrderCourseViewMap.get(workOrder);
            if (null != courseView) {
                courseSchedule.setCourseId(courseView.getBookSectionId());
                courseSchedule.setCourseName(courseView.getName());
                courseSchedule.setCourseType(courseView.getCourseType().get(0));
            }
            courseSchedule.setClassDate(workOrder.getStartTime());
            courseSchedule.setStartTime(workOrder.getStartTime());
            courseSchedule.setTimeSlotId(workOrder.getSlotId());
            courseSchedule.setWorkorderId(workOrder.getId());
            courseSchedule.setSkuIdExtra(workOrder.getSkuIdExtra());
            courseSchedules.add(courseSchedule);
        }
        courseScheduleService.save(courseSchedules);
    }

    private List<CourseSchedule> batchUpdateCourseSchedule(List<WorkOrder> workOrders) {
        List<CourseSchedule> courseSchedules = new ArrayList<>();
        for (WorkOrder workOrder : workOrders) {
            CourseSchedule courseSchedule = new CourseSchedule();
            courseSchedule.setStatus(workOrder.getStatus());
            courseSchedule.setStudentId(workOrder.getStudentId());
            courseSchedule.setTeacherId(workOrder.getTeacherId());
            courseSchedule.setCourseId(workOrder.getCourseId());
            courseSchedule.setCourseName(workOrder.getCourseName());
            courseSchedule.setCourseType(workOrder.getCourseType());
            courseSchedule.setClassDate(DateUtil.date2SimpleDate(workOrder.getStartTime()));
            courseSchedule.setStartTime(workOrder.getStartTime());
            //TODO:此处如果是外教,需要修改roleId为外教的Id
            courseSchedule.setRoleId(workOrder.getSkuId().intValue());
            courseSchedule.setTimeSlotId(workOrder.getSlotId());
            courseSchedule.setWorkorderId(workOrder.getId());
            courseSchedule.setSkuIdExtra(workOrder.getSkuIdExtra());
            courseSchedule.setClassType(workOrder.getClassType());
            courseSchedule.setIsFreeze(0);
            // 设置上课类型, 1对1, 1对6, 公开课

            courseSchedules.add(courseSchedule);
        }
        return courseScheduleService.save(courseSchedules);
    }

    @Transactional
    public List<CourseSchedule> persistCardInfos(
            List<Service> services, List<WorkOrder> workOrders, Map<Integer,
            RecommandCourseView> recommandCoursesMap) {
        for (Service service : services) {
            service = serveService.findByIdForUpdate(service.getId());
            if (service.getCoursesSelected() == 1) {
                throw new BusinessException("您已选过课程,请勿重复选课");
            }
            service.setCoursesSelected(1);
            serveService.save(service);
        }
        this.save(workOrders);
        List<CourseSchedule> courseSchedules = batchUpdateCourseSchedule(workOrders);
        scheduleCourseInfoService.batchSaveCourseInfos(workOrders, courseSchedules, recommandCoursesMap);
        return courseSchedules;
    }

    @Transactional
    public void saveWorkordersAndSchedules(
            Service service, List<WorkOrder> workOrders,
            Map<WorkOrder, CourseView> workOrderCourseViewMap) {
        this.save(workOrders);
        batchUpdateCourseSchedule(service, workOrders, workOrderCourseViewMap);
    }

    @Transactional
    public void saveWorkordersAndSchedules(
            Service service, List<WorkOrder> chineseWorkOrders,
            Map<WorkOrder, CourseView> chineseworkOrderCourseViewMap,
            List<WorkOrder> foreignWorkOrders, Map<WorkOrder, CourseView> foreignworkOrderCourseViewMap
    ) {
        this.save(chineseWorkOrders);
        batchUpdateCourseSchedule(service, chineseWorkOrders, chineseworkOrderCourseViewMap);

        if (!CollectionUtils.isEmpty(foreignWorkOrders)) {
            this.save(foreignWorkOrders);
            batchUpdateCourseSchedule(service, foreignWorkOrders, foreignworkOrderCourseViewMap);
        }
    }

    //根据service查找出所有状态的工单
    public Page<WorkOrder> findByQueryCondAllStatus(FishCardFilterParam fishCardFilterParam, Long[] ids, Pageable pageable) {
        return jpa.findByQueryCondAllStatus(ids, fishCardFilterParam.getBeginDateFormat(),
                fishCardFilterParam.getEndDateFormat(), pageable);
    }

    public Page<WorkOrder> findByQueryCondSpecialStatus(FishCardFilterParam fishCardFilterParam, Long[] ids, Pageable pageable) {
        return jpa.findByQueryCondSpecialStatus(ids, fishCardFilterParam.getBeginDateFormat(),
                fishCardFilterParam.getEndDateFormat(), fishCardFilterParam.getStatus(), pageable);
    }

    public void processFilterParam(FishCardFilterParam fishCardFilterParam, boolean isTeacher) {
        //学生
        if (!isTeacher) {
            processStudentFilterParam(fishCardFilterParam);
        } else {
            processTeacherFilterParam(fishCardFilterParam);
        }
        processDateParam(fishCardFilterParam);
    }

    public void processStudentFilterParam(FishCardFilterParam fishCardFilterParam) {
        if (null == fishCardFilterParam.getStudentId()) {
            throw new BusinessException("学生id为必选项,不应该为空");
        }

    }

    public void processTeacherFilterParam(FishCardFilterParam fishCardFilterParam) {
        if (null == fishCardFilterParam.getTeacherId()) {
            throw new BusinessException("教师id为必选项,不应该为空");
        }
    }

    public void processDateParam(FishCardFilterParam fishCardFilterParam) {
        if (null == fishCardFilterParam.getBeginDate()) {
            fishCardFilterParam.setBeginDateFormat(DateUtil.String2Date(ConstantUtil.EARLIEST_TIME));
        } else {
            fishCardFilterParam.setBeginDateFormat(DateUtil.String2Date(fishCardFilterParam.getBeginDate()));
        }
        if (null == fishCardFilterParam.getEndDate()) {
            fishCardFilterParam.setEndDateFormat(DateUtil.String2Date(ConstantUtil.LATEST_TIME));
        } else {
            fishCardFilterParam.setEndDateFormat(DateUtil.String2Date(fishCardFilterParam.getEndDate()));
        }

        if (null != fishCardFilterParam.getCreateBeginDate()) {
            fishCardFilterParam.setCreateBeginDateFormat(DateUtil.String2Date(fishCardFilterParam.getCreateBeginDate()));
        }

        if (null != fishCardFilterParam.getCreateEndDate()) {
            fishCardFilterParam.setCreateEndDateFormat(DateUtil.String2Date(fishCardFilterParam.getCreateEndDate()));
        }
    }

    //查找出教师所有状态的工单
    public List<WorkOrderView> findByQueryCondAllStatusForTeacher(Long teacherId, Date beginDate, Date endDate, Integer[] status) {
        String sqlOriginal = "select new com.boxfishedu.workorder.web.view.fishcard.WorkOrderView" +
                "(wo.id,sv.orderId, wo.studentId, sv.id, wo.studentName, wo.teacherId, wo.teacherName, wo.startTime, wo.endTime, wo.status, wo.courseId, wo.courseName, sv.skuId, sv.orderCode)" +
                " from  WorkOrder wo,Service sv where wo.service.id=sv.id and (wo.teacherId=? and wo.endTime between ? and ?) ";
        String statusSql = " and status=?";
        String orderPostFix = " order by wo.startTime";
        String sql = sqlOriginal;
        Query query = null;
        if (null != status) {
            sql = sql + statusSql + orderPostFix;
            query = entityManager.createQuery(sql).setParameter(1, teacherId).setParameter(2, beginDate).setParameter(3, endDate).setParameter(4, status[0]);
        } else {
            sql += orderPostFix;
            query = entityManager.createQuery(sql).setParameter(1, teacherId).setParameter(2, beginDate).setParameter(3, endDate);
        }
        List<WorkOrderView> workOrderViews = query.getResultList();
        return workOrderViews;
    }

    //查找出教师特定状态的工单
    public List<WorkOrder> findByQueryCondSpecialStatusForTeacher(Long teacherId, Date beginDate, Date endDate, Integer status) {
        return jpa.findByQueryCondSpecialStatusForTeacher(teacherId, beginDate, endDate, status);
    }

    public List<WorkOrder> findByStudentIdAndStartTimeBetween(Long studentId, Date beginDate, Date endDate) {
        return jpa.findByStudentIdAndStartTimeBetween(studentId, beginDate, endDate);
    }

    public List<WorkOrder> findByStudentId(Long studentId) {
        if (null == studentId) {
            throw new BusinessException("参数不正确");
        }
        return jpa.findByStudentId(studentId);
    }

    public List<WorkOrder> findByStudentIdAfterNow(Long studentId) {
        if (null == studentId) {
            throw new BusinessException("参数不正确");
        }
        return jpa.findByStudentIdAfterNow(studentId);
    }


    public WorkOrder getLatestWorkOrderByStudentIdAndProductTypeAndTutorType(Long studentId, Integer productType, String tutorType) {
        String sql = "select wo from WorkOrder wo where wo.studentId=? and wo.service.productType=? and wo.service.tutorType=? order by wo.endTime desc";
        Query query = entityManager.createQuery(sql)
                .setParameter(1, studentId)
                .setParameter(2, productType)
                .setParameter(3, tutorType);
        query.setMaxResults(1);
        List resultList = query.getResultList();
        return (WorkOrder) (CollectionUtils.isEmpty(resultList) ? null : resultList.get(0));
    }


    /****************************
     * 兼容老版本
     ************************/
    public void batchSaveCoursesIntoCard(List<WorkOrder> workOrders, Map<Integer, RecommandCourseView> recommandCoursesMap) {
        for (WorkOrder workOrder : workOrders) {
            RecommandCourseView courseView = recommandCoursesMap.get(workOrder.getSeqNum());
            workOrder.setCourseId(courseView.getCourseId());
            workOrder.setCourseName(courseView.getCourseName());
            workOrder.setCourseType(courseView.getCourseType());
            workOrder.setStatus(FishCardStatusEnum.COURSE_ASSIGNED.getCode());
            workOrder.setSkuId(new Integer(courseType2TeachingTypeService.courseType2TeachingType(workOrder.getCourseType(), TutorType.resolve(workOrder.getService().getTutorType()))));
        }
    }

    @Transactional
    public void updateWorkStatusRechargeOrderByIds(List<WorkOrder> workOrders) {
        jpa.save(workOrders);
        logger.info("||||updateWorkStatusRechargeOrderByIds||鱼卡更新成功 ");
    }


    /**
     * 用于发起退款请求并发问题
     *
     * @param
     * @return
     */
    public int updateWorkFishRechargeOne(int rechargeStatus, Long workOrderId) {
        return jpa.setFixedStatusRechargeFor(rechargeStatus, workOrderId, FishCardChargebackStatusEnum.NEED_RECHARGEBACK.getCode());
    }


    public List<WorkOrder> getAllWorkOrdersByIds(Long[] ids) {
        return jpa.findWorkOrderAll(ids);
    }

    public String trimArry(Long[] ids) {
        StringBuffer sb = new StringBuffer();
        for (Long id : ids) {
            sb.append(id).append(",");
        }
        return sb.toString().substring(0, sb.toString().length() - 1);
    }

    public WorkOrder getLatestWorkOrderByStudentIdAndComboType(Long studentId, String comboType) {
        int teachingType = ComboTypeToRoleId.resolve(comboType).getValue();
        String sql = "select wo from WorkOrder wo where wo.studentId=? and wo.service.teachingType=? order by wo.endTime desc";
        Query query = entityManager.createQuery(sql)
                .setParameter(1, studentId)
                .setParameter(2, teachingType);
        query.setMaxResults(1);
        List resultList = query.getResultList();
        return (WorkOrder) (CollectionUtils.isEmpty(resultList) ? null : resultList.get(0));
    }

    /****************
     * 兼容历史版本
     **********************/
    @Transactional
    public List<CourseSchedule> persistCardInfos(
            Service service, List<WorkOrder> workOrders, Map<Integer,
            RecommandCourseView> recommandCoursesMap) {
        service = serveService.findByIdForUpdate(service.getId());
        if (service.getCoursesSelected() == 1) {
            throw new BusinessException("您已选过课程,请勿重复选课");
        }
        service.setCoursesSelected(1);
        serveService.save(service);
        this.save(workOrders);
        List<CourseSchedule> courseSchedules = batchUpdateCourseSchedule(service, workOrders);
        scheduleCourseInfoService.batchSaveCourseInfos(workOrders, courseSchedules, recommandCoursesMap);
        return courseSchedules;
    }

    public List<CourseSchedule> batchUpdateCourseScheduleByWorkOrder(Service service, List<WorkOrder> workOrders) {
        List<CourseSchedule> courseSchedules = new ArrayList<>();
        for (WorkOrder workOrder : workOrders) {
            CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(workOrder.getId());
            courseSchedule.setStatus(workOrder.getStatus());
            courseSchedule.setStudentId(service.getStudentId());
            courseSchedule.setTeacherId(workOrder.getTeacherId());
            courseSchedule.setCourseId(workOrder.getCourseId());
            courseSchedule.setCourseName(workOrder.getCourseName());
            courseSchedule.setCourseType(workOrder.getCourseType());
            courseSchedule.setClassDate(DateUtil.date2SimpleDate(workOrder.getStartTime()));
            courseSchedule.setStartTime(workOrder.getStartTime());
            courseSchedule.setClassType(workOrder.getClassType());
            courseSchedule.setSmallClassId(workOrder.getSmallClassId());
            //TODO:此处如果是外教,需要修改roleId为外教的Id
            courseSchedule.setRoleId(workOrder.getSkuId().intValue());
            courseSchedule.setTimeSlotId(workOrder.getSlotId());
            courseSchedule.setWorkorderId(workOrder.getId());
            courseSchedule.setSkuIdExtra(workOrder.getSkuIdExtra());
            courseSchedule.setIsFreeze(0);
            courseSchedule.setClassType(workOrder.getClassType());
            if (org.apache.commons.lang3.StringUtils.equals(ClassTypeEnum.INSTNAT.toString(), workOrder.getClassType())) {
                courseSchedule.setInstantStartTtime(DateUtil.dateTrimYear(workOrder.getStartTime()));
                courseSchedule.setInstantEndTtime(DateUtil.dateTrimYear(workOrder.getEndTime()));
            }
            courseSchedules.add(courseSchedule);
        }
        return courseScheduleService.save(courseSchedules);
    }

    public List<CourseSchedule> batchUpdateCourseSchedule(Service service, List<WorkOrder> workOrders) {
        List<CourseSchedule> courseSchedules = new ArrayList<>();
        for (WorkOrder workOrder : workOrders) {
            CourseSchedule courseSchedule = new CourseSchedule();
            courseSchedule.setStatus(workOrder.getStatus());
            courseSchedule.setStudentId(service.getStudentId());
            courseSchedule.setTeacherId(workOrder.getTeacherId());
            courseSchedule.setCourseId(workOrder.getCourseId());
            courseSchedule.setCourseName(workOrder.getCourseName());
            courseSchedule.setCourseType(workOrder.getCourseType());
            courseSchedule.setClassDate(DateUtil.date2SimpleDate(workOrder.getStartTime()));
            courseSchedule.setStartTime(workOrder.getStartTime());
            courseSchedule.setClassType(workOrder.getClassType());
            courseSchedule.setSmallClassId(workOrder.getSmallClassId());
            //TODO:此处如果是外教,需要修改roleId为外教的Id
            courseSchedule.setRoleId(workOrder.getSkuId().intValue());
            courseSchedule.setTimeSlotId(workOrder.getSlotId());
            courseSchedule.setWorkorderId(workOrder.getId());
            courseSchedule.setSkuIdExtra(workOrder.getSkuIdExtra());
            courseSchedule.setIsFreeze(0);
            courseSchedule.setClassType(workOrder.getClassType());
            if (org.apache.commons.lang3.StringUtils.equals(ClassTypeEnum.INSTNAT.toString(), workOrder.getClassType())) {
                courseSchedule.setInstantStartTtime(DateUtil.dateTrimYear(workOrder.getStartTime()));
                courseSchedule.setInstantEndTtime(DateUtil.dateTrimYear(workOrder.getEndTime()));
            }
            courseSchedules.add(courseSchedule);
        }
        return courseScheduleService.save(courseSchedules);
    }

    // 根据订单id获取所有鱼卡信息
    public List<WorkOrder> getAllWorkOrdersByOrderId(Long orderId) {
        return jpa.findByOrderId(orderId);
    }

    public List<WorkOrder> findByStudentIdAndOrderChannelAndStartTimeAfter(Long studentId, String orderChannel, Date date) {
        return jpa.findByStudentIdAndOrderChannelAndStartTimeAfter(studentId, orderChannel, date);
    }

    public String[] enums2StringAray(List<ComboTypeEnum> comboTypeEnums) {
        String[] comboTypes = new String[comboTypeEnums.size()];
        for (int i = 0; i < comboTypeEnums.size(); i++) {
            comboTypes[i] = comboTypeEnums.get(i).toString();
        }
        return comboTypes;
    }

    public List<WorkOrder> getSelectedLeftAmount(Long studentId, List<ComboTypeEnum> comboTypeEnums, TeachingType teachingType) {
        return jpa.findByStudentIdAndComboTypeInAndSkuIdAndStartTimeAfter(studentId, enums2StringAray(comboTypeEnums), teachingType.getCode(), new Date());
    }

    public List<WorkOrder> getSelectedLeftAmount(Long studentId, List<ComboTypeEnum> comboTypeEnums) {
        return jpa.findByStudentIdAndComboTypeInAndStartTimeAfter(studentId, enums2StringAray(comboTypeEnums), new Date());
    }

    public List<WorkOrder> getSelectedLeftAmountNew(Long studentId, List<ComboTypeEnum> comboTypeEnums) {
        return jpa.findByStudentIdAndComboTypeInAndStartTimeAfter(studentId, enums2StringAray(comboTypeEnums), new Date());
    }

    public WorkOrder getCardToStart(Long studentId) {
//        return jpa.findTop1ByStudentIdAndStartTimeAfterOrderByStartTime(studentId,new Date());
        return null;
    }

    public List<WorkOrder> findFreezeCardsToUpdate() {
        return jpa.findByIsFreezeAndIsCourseOverAndStatusLessThanAndStartTimeLessThan(new Integer(1), new Short((short) 0), FishCardStatusEnum.WAITFORSTUDENT.getCode(), new Date());
    }

    //课程类型发生变化后修改教师
    public Boolean changeTeacherForTypeChanged(WorkOrder workOrder) {
        logger.debug("@changeTeacherForTypeChanged#{}课程类型发生变化向师生运营发起判断是否换课请求", workOrder.getId());
        workOrderLogService.saveWorkOrderLog(workOrder, "课程类型发生变化,向师生运营发起判断是否换课请求");
        Boolean result = teacherStudentRequester.changeTeacherForTypeChanged(workOrder);
        if (BooleanUtils.isFalse(result)) {
            logger.debug("@changeTeacherForTypeChanged#[{}]#result#{}", workOrder.getId(), result.booleanValue());
            workOrderLogService.saveWorkOrderLog(workOrder, "课程类型变化,教师能上此种类型课程");
            return result;
        }
        logger.debug("@changeTeacherForTypeChanged#{}#result#{}", workOrder.getId(), result.booleanValue());
        WorkOrder oldWorkOrder = workOrder.clone();
        workOrderLogService.saveWorkOrderLog(workOrder, "课程类型变化,教师不能上此种类型课程,需要更换教师,旧教师:" + oldWorkOrder.getTeacherId());
        workOrder.setTeacherId(0l);
        workOrder.setTeacherName(null);
        CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(workOrder.getId());
        courseSchedule.setTeacherId(0l);
        saveWorkOrderAndSchedule(workOrder, courseSchedule);
        timePickerService.getRecommandTeachers(workOrder);
        return result;
    }


    /**
     * 获取需要提醒的学生数量
     *
     * @return
     */
    public Map<Long, List<WorkOrder>> getNotifyMessage() {
        List<WorkOrder> needNotifyWorkOrders = jpa.findByNeedChangeTime(20);
        if (CollectionUtils.isEmpty(needNotifyWorkOrders)) {
            return null;
        }
        Date now = new Date();

        needNotifyWorkOrders = needNotifyWorkOrders.stream().filter(workOrder ->
                (1 != workOrder.getIsFreeze()
                        &&
                        now.before(workOrder.getStartTime())
                )

        ).collect(Collectors.toList());

        Map<Long, List<WorkOrder>> notifyMaps = Maps.newHashMap();

        for (WorkOrder workOrder : needNotifyWorkOrders) {
            List<WorkOrder> workOrders = notifyMaps.get(workOrder.getStudentId());
            if (CollectionUtils.isEmpty(workOrders)) {
                notifyMaps.put(workOrder.getStudentId(), Lists.newArrayList(workOrder));
            } else {
                workOrders.add(workOrder);
                notifyMaps.put(workOrder.getStudentId(), workOrders);
            }
        }

        for (Long key : notifyMaps.keySet()) {
            notifyMaps.put(key, getSortOrders(notifyMaps.get(key)));
        }
        logger.info("getNotifyMessage@notifyMapsSize=[{}]", notifyMaps.size());
        return notifyMaps;
    }

    /**
     * 获取某个学生的通知信息
     *
     * @param studentId
     * @return
     */
    public List<WorkOrder> getNotifyMessageByStudentId(Long studentId) {
        if (null == studentId || studentId == 0) {
            return null;
        }
        List<WorkOrder> needNotifyWorkOrders = jpa.findByNeedChangeTimeAndStudentId(20, studentId);

        if (CollectionUtils.isEmpty(needNotifyWorkOrders)) {
            return null;
        }
        Date now = new Date();

        needNotifyWorkOrders = needNotifyWorkOrders.stream().filter(workOrder ->
                (1 != workOrder.getIsFreeze()
                        &&
                        now.before(workOrder.getStartTime())
                )

        ).collect(Collectors.toList());
        return getSortOrders(needNotifyWorkOrders);
    }


    private List<WorkOrder> getSortOrders(List<WorkOrder> workOrders) {
        workOrders.sort(new Comparator<WorkOrder>() {
            @Override
            public int compare(WorkOrder o1, WorkOrder o2) {
                if (o1.getStartTime().after(o2.getStartTime())) {
                    return 0;
                }
                return -1;
            }
        });

        return workOrders;
    }

    public List<WorkOrder> findByStartTimeMoreThanAndSkuIdAndIsFreeze(WorkOrder workOrder) {
        List<String> listClassTypes = Lists.newArrayList(ClassTypeEnum.PUBLIC.name(), ClassTypeEnum.SMALL.name());
        return jpa.findByStudentIdAndStartTimeGreaterThanAndSkuIdAndIsFreeze(workOrder.getStudentId(), workOrder.getStartTime(), workOrder.getSkuId(), 0, listClassTypes);
    }

    public List<WorkOrder> getMatchWorkOrders(Long teacherId, List startTimes) {
        return jpa.findByTeacherIdAndIsFreezeAndStartTimeIn(teacherId, 0, startTimes);
    }

    public List<WorkOrder> findByIdIn(List workOrderIds) {
        return jpa.findByIdIn(workOrderIds);
    }

}
