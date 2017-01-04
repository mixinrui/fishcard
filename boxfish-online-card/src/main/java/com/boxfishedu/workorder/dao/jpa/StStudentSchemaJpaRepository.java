package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.StStudentSchema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by jiaozijun on 16/12/13.
 */
public interface StStudentSchemaJpaRepository extends JpaRepository<StStudentSchema, Long> {
    List<StStudentSchema> findByStudentId(Long studentId);
    StStudentSchema findByStudentIdAndSkuId(Long studentId, StStudentSchema.CourseType courseType);
    StStudentSchema findByStudentIdAndTeacherIdAndSkuIdAndStSchema(Long studentId,Long teacherId, StStudentSchema.CourseType courseType,StStudentSchema.StSchema stSchema);
    StStudentSchema findByStudentIdAndTeacherId(Long studentId,Long teacherId);
    StStudentSchema findByStudentIdAndTeacherIdAndStSchema(Long studentId,Long teacherId, StStudentSchema.StSchema stSchema);
    // 查询schema  学生id  st_schema  sku_id
    StStudentSchema findTop1ByStudentIdAndStSchemaAndSkuId(Long studentId,StStudentSchema.StSchema stSchema,StStudentSchema.CourseType courseType);
    List<StStudentSchema> findByStSchema(StStudentSchema.StSchema stSchema);
}
