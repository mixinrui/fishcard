package com.boxfishedu.workorder.service;

import com.boxfishedu.card.bean.TeachingType;
import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.common.bean.FishCardChargebackStatusEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.exception.BoxfishException;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.base.BaseService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.web.param.FishCardFilterParam;
import com.boxfishedu.workorder.web.view.course.CourseView;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.boxfishedu.workorder.web.view.course.ServiceWorkOrderCombination;
import com.boxfishedu.workorder.web.view.fishcard.WorkOrderView;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
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
    TeacherStudentRequester  teacherStudentRequester;

    @Autowired
    private WorkOrderLogService workOrderLogService;

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

    public List<WorkOrder> findByCourseType(String courseType){
        return jpa.findByCourseType(courseType);
    }

    @Transactional
    public void saveWorkOrderAndSchedule(WorkOrder workOrder,CourseSchedule courseSchedule){
        this.save(workOrder);
        courseScheduleService.save(courseSchedule);
        logger.info("||||||鱼卡[{}]入库成功;排课表入库成功[{}]",workOrder.getId(),courseSchedule.getId());
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

    private void batchUpdateCourseSchedule(Service service, List<WorkOrder> workOrders,
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
            //TODO:此处如果是外教,需要修改roleId为外教的Id
            courseSchedule.setRoleId(workOrder.getSkuId().intValue());
            courseSchedule.setTimeSlotId(workOrder.getSlotId());
            courseSchedule.setWorkorderId(workOrder.getId());
            courseSchedule.setSkuIdExtra(workOrder.getSkuIdExtra());
            courseSchedule.setIsFreeze(0);
            courseSchedules.add(courseSchedule);
        }
        return courseScheduleService.save(courseSchedules);
    }

    @Transactional
    public List<CourseSchedule> persistCardInfos(List<Service> services,List<WorkOrder> workOrders,Map<Integer,
            RecommandCourseView> recommandCoursesMap){
        for(Service service : services) {
            service = serveService.findByIdForUpdate(service.getId());
            if (service.getCoursesSelected() == 1) {
                throw new BusinessException("您已选过课程,请勿重复选课");
            }
            service.setCoursesSelected(1);
            serveService.save(service);
        }
        this.save(workOrders);
        List<CourseSchedule> courseSchedules=batchUpdateCourseSchedule(workOrders);
        scheduleCourseInfoService.batchSaveCourseInfos(workOrders,courseSchedules, recommandCoursesMap);
        return courseSchedules;
    }

    @Transactional
    public void saveWorkordersAndSchedules(Service service, List<WorkOrder> workOrders,
                                           Map<WorkOrder, CourseView> workOrderCourseViewMap) {
        this.save(workOrders);
        batchUpdateCourseSchedule(service, workOrders, workOrderCourseViewMap);
    }

    @Transactional
    public void saveWorkordersAndSchedules(Service service, List<WorkOrder> chineseWorkOrders,
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

        if(null != fishCardFilterParam.getCreateBeginDate()){
            fishCardFilterParam.setCreateBeginDateFormat(  DateUtil.String2Date(fishCardFilterParam.getCreateBeginDate()));
        }

        if(null != fishCardFilterParam.getCreateEndDate()){
            fishCardFilterParam.setCreateEndDateFormat(DateUtil.String2Date(fishCardFilterParam.getCreateEndDate()));
        }
    }

    //查找出教师所有状态的工单
    public List<WorkOrderView> findByQueryCondAllStatusForTeacher(Long teacherId, Date beginDate, Date endDate, Integer [] status) {
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


    /****************************兼容老版本************************/
    public void batchSaveCoursesIntoCard(List<WorkOrder> workOrders,Map<Integer, RecommandCourseView> recommandCoursesMap){
        for (WorkOrder workOrder : workOrders) {
            RecommandCourseView courseView=recommandCoursesMap.get(workOrder.getSeqNum());
            workOrder.setCourseId(courseView.getCourseId());
            workOrder.setCourseName(courseView.getCourseName());
            workOrder.setCourseType(courseView.getCourseType());
            workOrder.setStatus(FishCardStatusEnum.COURSE_ASSIGNED.getCode());
            workOrder.setSkuId(new Integer(courseType2TeachingTypeService.courseType2TeachingType(workOrder.getCourseType(), TutorType.resolve(workOrder.getService().getTutorType()))));
        }
    }

    @Transactional
    public void updateWorkStatusRechargeOrderByIds(List<WorkOrder> workOrders){
        jpa.save(workOrders);
        logger.info("||||updateWorkStatusRechargeOrderByIds||鱼卡更新成功 ");
    }


    /**
     * 用于发起退款请求并发问题
     * @param
     * @return
     */
    public int updateWorkFishRechargeOne(int rechargeStatus,Long workOrderId){
        return  jpa.setFixedStatusRechargeFor(rechargeStatus,workOrderId,FishCardChargebackStatusEnum.NEED_RECHARGEBACK.getCode());
    }


    public List<WorkOrder> getAllWorkOrdersByIds(Long[] ids){
        return jpa.findWorkOrderAll(ids);
    }

    public String trimArry(Long [] ids){
        StringBuffer sb = new StringBuffer();
        for(Long id :ids){
            sb.append(id).append(",");
        }
        return sb.toString().substring(0,sb.toString().length()-1);
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


    /**************** 兼容历史版本 **********************/
    @Transactional
    public List<CourseSchedule> persistCardInfos(Service service,List<WorkOrder> workOrders,Map<Integer,
            RecommandCourseView> recommandCoursesMap){
        service=serveService.findByIdForUpdate(service.getId());
        if(service.getCoursesSelected()==1){
            throw new BusinessException("您已选过课程,请勿重复选课");
        }
        service.setCoursesSelected(1);
        serveService.save(service);
        this.save(workOrders);
        List<CourseSchedule> courseSchedules=batchUpdateCourseSchedule(service, workOrders);
        scheduleCourseInfoService.batchSaveCourseInfos(workOrders,courseSchedules, recommandCoursesMap);
        return courseSchedules;
    }


    private List<CourseSchedule> batchUpdateCourseSchedule(Service service, List<WorkOrder> workOrders) {
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
            //TODO:此处如果是外教,需要修改roleId为外教的Id
            courseSchedule.setRoleId(workOrder.getSkuId().intValue());
            courseSchedule.setTimeSlotId(workOrder.getSlotId());
            courseSchedule.setWorkorderId(workOrder.getId());
            courseSchedule.setSkuIdExtra(workOrder.getSkuIdExtra());
            courseSchedule.setIsFreeze(0);
            courseSchedules.add(courseSchedule);
        }
        return courseScheduleService.save(courseSchedules);
    }

    // 根据订单id获取所有鱼卡信息
    public List<WorkOrder> getAllWorkOrdersByOrderId(Long orderId){
       return jpa.findByOrderId(orderId);
    }

    public List<WorkOrder> findByStudentIdAndOrderChannelAndStartTimeAfter(Long studentId,String orderChannel,Date date){
        return jpa.findByStudentIdAndOrderChannelAndStartTimeAfter(studentId,orderChannel,date);
    }

    public List<WorkOrder> getSelectedLeftAmount(Long studentId, ComboTypeEnum comboTypeEnum, TeachingType teachingType){
        return jpa.findByStudentIdAndComboTypeAndSkuIdAndStartTimeAfter(studentId,comboTypeEnum.toString(),teachingType.getCode(),new Date());
    }

    public List<WorkOrder> getSelectedLeftAmount(Long studentId, ComboTypeEnum comboTypeEnum){
        return jpa.findByStudentIdAndComboTypeAndStartTimeAfter(studentId,comboTypeEnum.toString(),new Date());
    }

    public WorkOrder getCardToStart(Long studentId){
//        return jpa.findTop1ByStudentIdAndStartTimeAfterOrderByStartTime(studentId,new Date());
        return null;
    }

    public List<WorkOrder> findFreezeCardsToUpdate() {
        return jpa.findByIsFreezeAndIsCourseOverAndStatusLessThanAndStartTimeLessThan(new Integer(1),new Short((short)0),FishCardStatusEnum.WAITFORSTUDENT.getCode(),new Date());
    }

    //课程类型发生变化后修改教师
    public void changeTeacherForTypeChanged(WorkOrder workOrder){
        logger.debug("@changeTeacherForTypeChanged#{}课程类型发生变化向师生运营发起换课请求",workOrder.getId());
        workOrderLogService.saveWorkOrderLog(workOrder,"课程类型发生变化,向师生运营发起换课请求");
        TeacherView teacherView=teacherStudentRequester.changeTeacherForTypeChanged(workOrder);
        if(teacherView.getTeacherId().equals(workOrder.getTeacherId())){
            workOrderLogService.saveWorkOrderLog(workOrder,"获取教师没有发生变化");
            return;
        }
        CourseSchedule courseSchedule=courseScheduleService.findByWorkOrderId(workOrder.getId());
        workOrder.setTeacherId(teacherView.getTeacherId());
        workOrder.setTeacherName(teacherView.getTeacherName());
        courseSchedule.setTeacherId(teacherView.getTeacherId());
        saveWorkOrderAndSchedule(workOrder,courseSchedule);
    }

}
