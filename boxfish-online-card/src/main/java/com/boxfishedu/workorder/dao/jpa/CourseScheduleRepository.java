/*
* Copyright (c) 2015 boxfish.cn. All Rights Reserved.
*/
package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created with Intellij IDEA
 * Author: boxfish
 * Date: 16/3/12
 * Time: 17:14
 */
public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, Long> {


    @Query(value = "select count(1) from CourseSchedule cs" +
            " where cs.teacherId =:teacherId" +
            " and cs.timeSlotId =:timeSlotId" +
            " and date_format(cs.classDate,'%Y%m%d') between date_format(:startDate,'%Y%m%d') and  date_format(:endDate,'%Y%m%d')")
    public Long exists(@Param("teacherId") Long teacherId, @Param("timeSlotId") Long timeSlotId,
                       @Param("startDate") Date classStartDate, @Param("endDate") Date classEndDate);


    public List<CourseSchedule> findByStatus(Integer status);

    public CourseSchedule findTop1ByStatusAndTimeSlotId(Integer status, Long timeSlotId);

    public List<CourseSchedule> findByStatusAndTimeSlotId(Integer status, Long timeSlotId);

    public CourseSchedule findByWorkorderId(Long workorderId);

    public List<CourseSchedule> findByWorkorderIdIn(Long[] workOrderIds);

    public CourseSchedule findByTeacherIdAndTimeSlotIdAndClassDate(Long teacherId, Long timeSlotId, Date classDate);

    public CourseSchedule findTop1ByWorkorderId(Long workOrderId);

    public List<CourseSchedule> findByTeacherIdAndClassDateBetween(Long teacherId, Date begin, Date end);

    public List<CourseSchedule> findByStudentIdAndClassDateBetweenOrderByClassDateAscTimeSlotIdAsc(Long teacherId, Date begin, Date end);

    List<CourseSchedule> findByClassDateAndTeacherId(Date classDate, Long teacherId);

    List<CourseSchedule> findByTeacherId(Long teacherId);

    @Query(value = "select s from CourseSchedule s where s.studentId=?1 and s.status>=40 and s.status<50")
    Page<CourseSchedule> findFinishCourseScheduleByStudentId(Long studentId, Pageable pageable);

    Page<CourseSchedule> findByStudentIdAndStatusBefore(Long studentId, Integer status, Pageable pageable);

    @Query(nativeQuery =true, value = "select 1 from course_schedule c where c.student_id=?1 limit 1")
    Integer checkIfHaveCourse(Long studentId);

    public CourseSchedule findTop1ByStudentIdAndTimeSlotIdAndClassDate(Long studentId, Integer timeSlotId, Date classDate);

    @Query(value = "select max(c.classDate) from CourseSchedule c where teacherId=?1")
    Optional<Date> findTop1ClassDateByTeacherId(Long teacherId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select wo from  CourseSchedule wo where wo.workorderId=?1 ")
    public CourseSchedule findByWorkOrderIdForUpdate(Long workorderId);

    @Modifying
    @Query("update CourseSchedule o set o.teacherId = ?1,o.status=?3 where o.workorderId = ?2 ")
    int setTeacherIdByWorkOrderId(Long teacherId , Long workorderId, Integer status);

    // Page<CourseSchedule> findByStudentId(Long studentId, Pageable pageable);

    @Query("select c from CourseSchedule c where c.classDate>=?2 and c.status<40 and c.studentId=?1 order by c.classDate,c.timeSlotId")
    Page<CourseSchedule> findByStudentIdAfterClassDate(Long studentId, Date classDate, Pageable pageable);

    @Query("select concat(c.classDate,' ',c.timeSlotId) from CourseSchedule c where c.studentId=?1 and c.classDate>=?2 and c.status<40")
    Set<String> findUnfinishByStudentIdAndAfterDate(Long studentId, Date afterDate);

    @Query("select concat(c.classDate,' ',c.timeSlotId) from CourseSchedule c where c.studentId=?1 and c.classDate=?2 and c.status<40")
    Set<String> findUnfinishByStudentIdAndCurrentDate(Long studentId, Date Date);
}
