package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.StStudentSchema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by jiaozijun on 16/12/13.
 */
public interface StStudentSchemaJpaRepository extends JpaRepository<StStudentSchema, Long> {
    List<StStudentSchema> findByStudentId(Long studentId);
    StStudentSchema findByStudentIdAndTeacherIdAndSkuId(Long studentId,Long teacherId, StStudentSchema.CourseType courseType);
    StStudentSchema findByStudentIdAndTeacherIdAndSkuIdAndStSchema(Long studentId,Long teacherId, StStudentSchema.CourseType courseType,StStudentSchema.StSchema stSchema);
    StStudentSchema findByStudentIdAndTeacherId(Long studentId,Long teacherId);
}
