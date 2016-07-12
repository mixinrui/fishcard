package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.entity.mysql.WorkOrderGrab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;


/**
 * Created by jiaozijun on 16/7/11.
 */
public interface WorkOrderGrabJpaRepository extends JpaRepository<WorkOrderGrab, Long> {

    // 根据教师id,flag查询该老师可以抢的鱼卡信息
    public List<WorkOrderGrab> findByTeacherIdAndFlagAndStartTimeGreaterThan(Long teacherId, String flag, Date date);

    // 抢单之后,给课程匹配相应的老师,并且标记flag为1表示抢单成功
    @Modifying
    @Query("update WorkOrderGrab o set o.flag = '1' , o.teacherId = ?1 where o.workorderId = ?2")
    int setFlagAndTeacherId(Long teacherId , Long workorderId);

}
