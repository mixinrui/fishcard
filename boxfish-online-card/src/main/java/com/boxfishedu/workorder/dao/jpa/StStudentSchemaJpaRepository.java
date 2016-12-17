package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.StStudentSchema;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by jiaozijun on 16/12/13.
 */
public interface StStudentSchemaJpaRepository extends JpaRepository<StStudentSchema, Long> {
    StStudentSchema findByStudentId(Long studentId);
    StStudentSchema findByStudentIdAndTeacherIdAndStSchema(Long studentId,Long teacherId, StStudentSchema.StSchema stSchema);

    // 查询schema  学生id  st_schema  sku_id
    public StStudentSchema findTop1ByStudentIdAndStSchemaAndSkuId(Long studentId,StStudentSchema.StSchema stSchema,StStudentSchema.CourseType courseType);
}
