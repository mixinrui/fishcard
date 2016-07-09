package com.boxfishedu.workorder.service;


import com.boxfishedu.workorder.common.bean.CourseScheduleStatusEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.base.BaseService;
import com.boxfishedu.workorder.web.param.FetchTeacherParam;
import com.boxfishedu.workorder.web.view.fishcard.TeacherAlterView;
import com.boxfishedu.workorder.web.view.form.DateRangeForm;
import com.boxfishedu.workorder.web.view.teacher.MonthScheduleDataView;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by hucl on 16/4/9.
 */
@Component
public class CourseScheduleService extends BaseService<CourseSchedule,CourseScheduleRepository,Long> {

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private CourseScheduleRepository courseScheduleRepository;

    @Autowired
    private UrlConf urlConf;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ServiceSDK serviceSDK;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    public CourseSchedule findByWorkOrderId(Long workOrderId) {
        return jpa.findByWorkorderId(workOrderId);
    }

    public List<CourseSchedule> findByTeacherIdAndClassDateBetween(Long teacherId, DateRangeForm dateRangeForm) {
        return jpa.findByTeacherIdAndClassDateBetween(teacherId, dateRangeForm.getFrom(), dateRangeForm.getTo());
    }

    //从师生运营组批量获取教师列表,key为coursechedule的id;首次获取的时候将id设置为0L
    public Map<String,TeacherView> getTeacher(FetchTeacherParam fetchTeacherParam){
        return teacherStudentRequester.getTeacherList(fetchTeacherParam);
    }


    public Map<String, Map<String, CourseSchedule>> groupCourseScheduleByDate(List<CourseSchedule> courseSchedules) {
        Map<String, Map<String, CourseSchedule>> dailyScheduleMap = Maps.newHashMap();
        courseSchedules.forEach(courseSchedule -> {
            String day = DateUtil.simpleDate2String(courseSchedule.getClassDate());
            if (!dailyScheduleMap.containsKey(day)) {
                dailyScheduleMap.put(day, Maps.newHashMap());
            }
            dailyScheduleMap.get(day).put(courseSchedule.getTimeSlotId().toString(), courseSchedule);
        });
        return dailyScheduleMap;
    }

    public void wrapCourseIntoSlotList(List<MonthScheduleDataView> monthScheduleDataViews, Map<String, Map<String, CourseSchedule>> dailyScheduleMap) {
        monthScheduleDataViews.forEach(monthScheduleDataView -> {
            monthScheduleDataView.getDailyScheduleTime().forEach(dailyScheduleTimeView -> {
                String day = monthScheduleDataView.getDay();
                if (null != dailyScheduleMap.get(day)) {
                    CourseSchedule courseChedule = dailyScheduleMap.get(day).get(dailyScheduleTimeView.getSlotId());
                    if (null != courseChedule) {
                        dailyScheduleTimeView.setCourseId(courseChedule.getCourseId());
                        dailyScheduleTimeView.setCourseName(courseChedule.getCourseName());
                    }
                }
            });
        });
    }

    public List<CourseSchedule> findByStudentIdAndClassDateBetween(Long studentId, DateRangeForm dateRangeForm) {
        return jpa.findByStudentIdAndClassDateBetweenOrderByClassDateAscTimeSlotIdAsc(
                studentId, dateRangeForm.getFrom(), dateRangeForm.getTo());
    }

    public List<CourseSchedule> findByClassDateAndTeacherId(Date classDate, Long teacherId) {
        return jpa.findByClassDateAndTeacherId(classDate, teacherId);
    }

    public List<CourseSchedule> findByTeacherId(Long teacherId) {
        return jpa.findByTeacherId(teacherId);
    }

    /**
     * 处理工单与排课表, 应该在同一个事务里面...
     * @param courseSchedule
     * @param teacher
     */
    @Transactional
    public void handleWorkOrderAndCourseSchedule(CourseSchedule courseSchedule, TeacherView teacher) {
        // 没有分配到老师不处理
        if(teacher.getTeacherId() == CourseSchedule.NO_ASSIGN_TEACHER_ID.longValue()) {
            return;
        }
        // 修改排课表状态,保存排课表
        courseSchedule.setTeacherId(teacher.getTeacherId());
        if(courseSchedule.getWorkorderId() == null) {
            logger.error("排课表{}没有对应的工单", courseSchedule.getId());
        }
        courseSchedule.setStatus(CourseScheduleStatusEnum.ASSIGNEDTEACHER.value());
        save(courseSchedule);

        // 修改工单以及状态,保存工单
        WorkOrder workOrder = workOrderJpaRepository.findOne(courseSchedule.getWorkorderId());
        workOrder.setTeacherId(teacher.getTeacherId());
        workOrder.setTeacherName(teacher.getName());
        workOrder.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());

        workOrderJpaRepository.save(workOrder);

        // 创建群组
        serviceSDK.createGroup(workOrder);
    }

    public Page<CourseSchedule> findFinishCourseSchedulePage(Long userId, Pageable pageable) {
        return courseScheduleRepository.findByStudentIdAndStatus(
                userId, FishCardStatusEnum.COMPLETED.getCode(), pageable);
    }

    public Page<CourseSchedule> findUnfinishCourseSchedulePage(Long userId, Pageable pageable) {
        return courseScheduleRepository.findByStudentIdAndStatusBefore(
                userId, FishCardStatusEnum.COMPLETED.getCode(), pageable);
    }

    public List<TeacherAlterView> getOutNumOfTeacher(Date beginDate, Date endDate){
        String sql = "select new com.boxfishedu.workorder.web.view.fishcard.TeacherAlterView" +
                "(count(cs.id),cs.roleId)" +
                " from  CourseSchedule cs where (cs.status=? and cs.classDate between ? and ?) ";
        Query query = entityManager.createQuery(sql).setParameter(1,FishCardStatusEnum.COURSE_ASSIGNED.getCode()).setParameter(2, beginDate).setParameter(3, endDate);
        List<TeacherAlterView> teacherAlterViews=query.getResultList();
        return teacherAlterViews;
    }

    public CourseSchedule findTop1ByStudentIdAndTimeSlotIdAndClassDate(Long studentId,Integer timeSlotId,Date classDate){
        CourseSchedule courseSchedule=jpa.findTop1ByStudentIdAndTimeSlotIdAndClassDate(studentId,timeSlotId,classDate);
        return courseSchedule;
    }

    public Optional<Date> findMaxClassDateByTeacherId(Long teacherId) {
        return jpa.findTop1ClassDateByTeacherId(teacherId);
    }
}
