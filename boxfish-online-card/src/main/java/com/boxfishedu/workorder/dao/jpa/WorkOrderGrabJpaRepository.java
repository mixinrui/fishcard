package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.WorkOrderGrab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;


/**
 * Created by jiaozijun on 16/7/11.
 */
public interface WorkOrderGrabJpaRepository extends JpaRepository<WorkOrderGrab, Long> {

    // 根据教师id,flag查询该老师可以抢的鱼卡信息
    public List<WorkOrderGrab> findByTeacherId(Long teacherId);

    // 抢单之后,给课程匹配相应的老师,并且标记flag为1表示抢单-------------成功
    @Modifying
    @Query("update WorkOrderGrab o set o.flag = '1' , o.teacherId = ?1 where o.workorderId = ?2")
    int setFlagSuccessAndTeacherId(Long teacherId , Long workorderId);

    // 抢单之后,给课程匹配相应的老师,并且标记flag为2表示抢单-------------失败
    @Modifying
    @Query("update WorkOrderGrab o set o.flag = '2' , o.teacherId = ?1 where o.workorderId = ?2")
    int setFlagFailAndTeacherId(Long teacherId , Long workorderId);

    // 获取今天之前的数据
    public List<WorkOrderGrab> findByCreateTimeLessThan(Date date);


//    // 删除今天之前的数据
//    public int deleteByCreateTimeLessThan(Date date);


}
