package com.boxfishedu.workorder.dao.jpa;



import com.boxfishedu.workorder.entity.mysql.WorkOrderGrab;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by oyjun on 16/2/29.
 */
public interface WorkOrderLogJpaRepository extends JpaRepository<WorkOrderGrab,Long> {


    /**   **/
    public Page<WorkOrderGrab> findByTeacherId(Long teacherId, Pageable pageable);

    /**   **/
    public List<WorkOrderGrab> findByTeacherId(Long teacherId);


}