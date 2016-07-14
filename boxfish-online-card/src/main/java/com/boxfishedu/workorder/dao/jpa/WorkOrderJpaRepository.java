package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;

/**
 * Created by oyjun on 16/2/29.
 */
public interface WorkOrderJpaRepository extends JpaRepository<WorkOrder, Long> {

    public Page<WorkOrder> findByServiceIdOrderByStartTime(Long serviceId, Pageable pageable);

    public WorkOrder findByOrderIdAndServiceId(Long orderId, Long serviceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select wo from  WorkOrder wo where wo.id=?1 ")
    public WorkOrder findByIdForUpdate(Long id);

    public Page<WorkOrder> findByTeacherIdAndStartTimeBetweenOrderByStartTime(Long teacherId, Date startDate, Date endDate, Pageable pageable);

    public Page<WorkOrder> findByTeacherIdAndStatusAndCreateTimeBetween(Pageable pageable, Long teacherId, Integer status, Date beginDate, Date endDate);

    public Page<WorkOrder> findByTeacherIdAndStatusLessThanAndCreateTimeBetween(Pageable pageable, Long teacherId, Integer status, Date beginDate, Date endDate);

    public Page<WorkOrder> findByTeacherIdAndCreateTimeBetween(Pageable pageable, Long teacherId, Date beginDate, Date endDate);

    public List<WorkOrder> findByTeacherIdAndCreateTimeBetween(Long teacherId, Date beginDate, Date endDate);

    public List<WorkOrder> findByStudentIdAndStartTimeBetween(Long studentId, Date beginDate, Date endDate);

    //查找出学生所有状态的工单
    @Query("select wo from  WorkOrder wo where wo.service.id in (?1) and (wo.startTime between ?2 and ?3) order by wo.startTime desc ")
    public Page<WorkOrder> findByQueryCondAllStatus(Long[] ids, Date beginDate, Date endDate, Pageable pageable);

    //查找出学生所有状态的工单
    @Query("select wo from  WorkOrder wo where wo.service.id in (?1) and (wo.startTime between ?2 and ?3) and status=?4 order by wo.endTime desc ")
    public Page<WorkOrder> findByQueryCondSpecialStatus(Long[] ids, Date beginDate, Date endDate, Integer status, Pageable pageable);

    //查找出教师所有状态的工单
    @Query("select wo from  WorkOrder wo where wo.teacherId=?1 and (wo.endTime between ?2 and ?3) order by wo.startTime ")
    public List<WorkOrder> findByQueryCondAllStatusForTeacher(Long teacherId, Date beginDate, Date endDate);

    //查找出教师特定状态的工单
    @Query("select wo from  WorkOrder wo where wo.teacherId=?1 and (wo.endTime between ?2 and ?3) and status=?4 order by wo.endTime ")
    public List<WorkOrder> findByQueryCondSpecialStatusForTeacher(Long teacherId, Date beginDate, Date endDate, Integer status);

    public List<WorkOrder> findByStatusAndStartTimeBetween(Integer status, Date startDate, Date endDate);

    public List<WorkOrder> findByStatusInAndStartTimeBetween(Integer[] statuses, Date startDate, Date endDate);

    public List<WorkOrder> findByStatusLessThanAndEndTimeBetween(Integer status, Date startDate, Date endDate);

    public List<WorkOrder> findByStatusAndOrderIdLessThanAndEndTimeBetween(Integer status, long orderId, Date startDate, Date endDate);

    public List<WorkOrder> findByStatusInAndOrderIdLessThanAndEndTimeBetween(Integer[] statuses, long orderId, Date startDate, Date endDate);

    public List<WorkOrder> findByIsCourseOverAndStatusInAndOrderIdLessThanAndEndTimeBetween(Short isCourseOver,Integer[] statuses, long orderId, Date startDate, Date endDate);

    //按照订单id查找鱼卡
    public List<WorkOrder> findByOrderId(Long orderId);

    //按照订单ID  和  状态 查找鱼卡
    public List<WorkOrder> findByOrderIdAndStatus(Long orderId, int status);

    //状态为退卡状态 并且 剩余课程不为零
    @Query("select wo from WorkOrder wo where wo.status =?1 and wo.service.amount>0")
    public List<WorkOrder> findWorkOrderContainBackOrder(int status);

    public List<WorkOrder> findByStudentIdAndOrderIdAndStatusLessThan(Long studentId,Long orderId,Integer status);

    public List<WorkOrder> findByStudentIdAndStatusLessThan(Long studentId,Integer status);

}
