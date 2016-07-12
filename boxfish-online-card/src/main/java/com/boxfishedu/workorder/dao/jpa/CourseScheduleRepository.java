/*
* Copyright (c) 2015 boxfish.cn. All Rights Reserved.
*/
package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

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
}
