package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.StStudentApplyRecords;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by jiaozijun on 16/12/13.
 */
public interface StStudentApplyRecordsJpaRepository extends JpaRepository<StStudentApplyRecords, Long> {

    public StStudentApplyRecords findByworkOrderIdAndApplyStatus(Long workOrderId, Integer applyStatus);
//    StStudentApplyRecords findBy

    public StStudentApplyRecords findTop1ByWorkOrderIdAndApplyStatus(Long workOrderId,Integer applyStatus);
}
