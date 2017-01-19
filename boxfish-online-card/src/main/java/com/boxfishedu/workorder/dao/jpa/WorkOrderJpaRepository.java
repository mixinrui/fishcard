package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

/**
 * Created by oyjun on 16/2/29.
 */
public interface WorkOrderJpaRepository extends JpaRepository<WorkOrder, Long> {

    public Page<WorkOrder> findByServiceIdOrderByStartTime(Long serviceId, Pageable pageable);


    //按照订单id查找鱼卡
    public Page<WorkOrder> findByOrderIdAndIsFreeze(Long orderId, Integer isFreeze, Pageable pageable);

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

    public List<WorkOrder> findByStudentId(Long studentId);

    @Query("select wo from  WorkOrder wo where wo.studentId =?1 and wo.startTime <current_timestamp  and wo.isFreeze!=1  order by wo.startTime desc ")
    public List<WorkOrder> findByStudentIdAfterNow(Long studentId);

    public List<WorkOrder> findByIsFreezeAndStartTimeBetweenAndParentIdNotNull(Integer isFreeze, Date startDate, Date endDate);

    public List<WorkOrder> findByNeedChangeTime(Integer needChangeTime);

    public List<WorkOrder> findByNeedChangeTimeAndStudentId(Integer needChangeTime, Long studentId);


    //查找出学生所有状态的工单
    @Query("select wo from  WorkOrder wo where wo.service.id in (?1) and (wo.startTime between ?2 and ?3) order by wo.startTime desc ")
    public Page<WorkOrder> findByQueryCondAllStatus(Long[] ids, Date beginDate, Date endDate, Pageable pageable);

    //查找出学生所有状态的工单
    @Query("select wo from  WorkOrder wo where wo.service.id in (?1) and (wo.startTime between ?2 and ?3) and status in (?4) order by wo.endTime desc ")
    public Page<WorkOrder> findByQueryCondSpecialStatus(Long[] ids, Date beginDate, Date endDate, Integer[] status, Pageable pageable);

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

    public List<WorkOrder> findByIsCourseOverAndStatusInAndOrderIdLessThanAndEndTimeBetween(Short isCourseOver, Integer[] statuses, long orderId, Date startDate, Date endDate);

    //按照订单id查找鱼卡
    public List<WorkOrder> findByOrderId(Long orderId);

    //按照订单ID  和  状态 查找鱼卡
    public List<WorkOrder> findByOrderIdAndStatus(Long orderId, int status);

    public List<WorkOrder> findByOrderIdAndStartTimeAfterOrderByStartTimeAsc(Long orderId, Date date);

    //状态为退卡状态 并且 剩余课程不为零
    @Query("select wo from WorkOrder wo where wo.status =?1 and wo.service.amount>0")
    public List<WorkOrder> findWorkOrderContainBackOrder(int status);

    public List<WorkOrder> findByStudentIdAndOrderIdAndStatusLessThan(Long studentId, Long orderId, Integer status);

    public List<WorkOrder> findByStudentIdAndStatusLessThan(Long studentId, Integer status);


    /**
     * begin 抢单接口
     **/

    //  获取未来两天内 未安排教师的鱼卡信息  teacherid =0
    public List<WorkOrder> findByTeacherIdAndStartTimeBetweenAndCreateTimeLessThanOrderByStartTime(Long teacherId, Date startDate, Date endDate, Date createTime);

    //  获取未来两天内 未安排教师的鱼卡信息 teacherid >0
    public List<WorkOrder> findByTeacherIdGreaterThanAndStartTimeBetween(Long teacherId, Date startDate, Date endDate);

    /**
     * end 抢单接口
     **/


    // 获取当前时间往前一整天(24小时)有换课纪录的鱼卡 状态30 updatetime不为空 teacherid>0    sendFlagCC  1 未发送
    //public List<WorkOrder> findByTeacherIdGreaterThanAndStatusAndUpdatetimeChangecourseBetween(Long teacherId,Integer status,Date begin ,Date end);
    public List<WorkOrder> findByTeacherIdGreaterThanAndStatusAndSendflagccAndUpdatetimeChangecourseNotNullAndStartTimeBetween(Long teacherId, Integer status, String sendFlagCC, Date begin, Date end);


    // 抢单之后,给课程匹配相应的老师
    @Modifying
    @Query("update WorkOrder o set o.teacherName= ?1 ,o.teacherId = ?2 ,o.status = ?3 ,updateTime=current_timestamp ,assignTeacherTime=current_timestamp   where o.id = ?4 and o.teacherId = ?5")
    int setFixedTeacherNameAndTeacherIdAndStatusFor(String teacherName, Long teacherId, Integer status, Long workorderId, Long teacherIdZero);


    @Query("select wo from WorkOrder wo where wo.id in (?1) ")
    public List<WorkOrder> findWorkOrderAll(Long[] ids);

    @Query("select wo from WorkOrder wo where wo.id in (?1) ")
    public List<WorkOrder> findWorkOrderAll(List<Long> ids);

    public List<WorkOrder> findByCourseType(String courseType);

    public List<WorkOrder> findByStudentIdAndStatusLessThanAndStartTimeAfter(Long studentId, Integer status, Date beginDate);


    @Modifying
    @Query("update WorkOrder o set o.updatetimeRecharge= current_timestamp ,o.statusRecharge= ?1 where o.id = ?2 and o.statusRecharge=?3")
    int setFixedStatusRechargeFor(Integer rechargeCodeAfter, Long id, Integer rechargeCodeBefore);

    //  获取未来两天内 未安排教师的鱼卡信息  teacherid =0
    @Query(value = "select wo from WorkOrder wo where wo.teacherId=?1 and wo.isFreeze=?2 and wo.startTime between ?3 and ?4 and wo.createTime<?5 and (wo.classType not in(?6) or wo.classType is null)    order by wo.startTime")
    public List<WorkOrder> findByTeacherIdAndIsFreezeAndStartTimeBetweenAndCreateTimeLessThanOrderByStartTime(Long teacherId, Integer isFreeze, Date startDate, Date endDate, Date createTime, List<String> classTypes);

    //查找出学生所有状态的工单
    @Query("select distinct wo.studentId from WorkOrder wo where wo.orderId<11111111")
    public List<Long> findDistinctUsersFromWorkOrder();

    public List<WorkOrder> findByStudentIdAndEndTimeLessThanOrderByStartTimeDesc(Long studentId, Date date);

    public List<WorkOrder> findByStudentIdAndOrderChannelAndStartTimeAfter(Long studentId, String orderChannel, Date date);

    /**
     * 查询旷课的、还未扣积分的学生
     **/
    @Query("select  wo from WorkOrder wo where wo.status = 51 and wo.deductScoreStatus is null and (wo.startTime between?1 and?2) and wo.orderChannel != ?3")
    List<WorkOrder> queryAbsentStudent(Date startTime, Date endTime, String param);

    List<WorkOrder> findByStudentIdAndComboTypeAndSkuIdAndStartTimeAfter(Long studentId, String comboType, Integer skuId, Date date);

    List<WorkOrder> findByStudentIdAndComboTypeInAndSkuIdAndStartTimeAfter(Long studentId, String[] comboType, Integer skuId, Date date);

    List<WorkOrder> findByStudentIdAndComboTypeAndStartTimeAfter(Long studentId, String comboType, Date date);

    List<WorkOrder> findByStudentIdAndComboTypeInAndStartTimeAfter(Long studentId, String[] comboType, Date date);

    public WorkOrder findTop1ByStudentIdAndSkuIdAndStartTimeAfterOrderByStartTime(Long studentId, Integer skuId, Date date);

    @Query("select wo.id from WorkOrder wo")
    public List<Long> findAllWorkOrderId();

    @Query("select wo.id from WorkOrder wo where wo.startTime<?1")
    public List<Long> findAllFinishedId(Date date);

    List<WorkOrder> findByIsFreezeAndIsCourseOverAndStatusLessThanAndStartTimeLessThan(Integer freezeFlag, Short isCourseOver, Integer status, Date startTime);

    // 获取兑换类型一个种类下最大的一个序号
    @Query("select max(w.seqNum) from WorkOrder w where w.studentId=?1 and w.comboType=?2 and w.skuIdExtra=?3")
    Optional<Integer> findMaxSeqNumByStudentIdComboTypeAndSkuIdExtra(Long studentId, String comboType, Integer skuIdExtra);

    // 查找48小时以内,没有推荐课程的鱼卡
    @Query("select w from WorkOrder w where w.startTime<?1 and w.courseId is null")
    List<WorkOrder> findWithinHoursCreatedWorkOrderList(Date endTime);

    //查找最新一节上过的课程
    Optional<WorkOrder> findTop1ByStudentIdAndStartTimeLessThanOrderByStartTimeDesc(Long studentId, Date date);


    @Query(value = "select min(wo.startTime) from WorkOrder wo where studentId=? and isFreeze=? and startTime>?")
    Optional<Date> findLatestClassDateByStudentId(Long studentId, Integer isFreeze, Date date);

    Optional<WorkOrder> findTop1ByStudentIdAndSkuIdAndIsFreezeAndStartTimeAfterOrderByStartTimeAsc(Long studentId, Integer skuId, Integer isFreeze, Date date);

    Optional<WorkOrder> findTop1ByStudentIdAndSkuIdAndIsFreezeAndStartTimeAfterAndClassTypeIsNullOrderByStartTimeAsc(Long studentId, Integer skuId, Integer isFreeze, Date date);

    @Query(value = "select distinct wo.skuId from WorkOrder wo where studentId=? and startTime>?")
    List<Integer> findDistinctSkuIds(Long studentId, Date startTime);

    List<WorkOrder> findByStudentIdAndStartTimeGreaterThanAndSkuIdAndIsFreeze(Long studentId, Date startTime, Integer skuId, Integer isFreeze);

    List<WorkOrder> findByTeacherIdAndIsFreezeAndStartTimeIn(Long teacherId, Integer isFreeze, List startTimes);

    @Query("select wo from  WorkOrder wo where wo.teacherId=?1 and (wo.endTime between ?2 and ?3) and status=?4 order by wo.endTime ")
    public List<WorkOrder> findByStartTime(Long teacherId, Date beginDate, Date endDate, Integer status);

    public List<WorkOrder> findByIdIn(List workOrderIds);

    List<WorkOrder> findByClassTypeAndStartTimeGreaterThan(String classType, Date date);

    List<WorkOrder> findByClassTypeAndSmallClassIdIsNotNullAndStartTimeGreaterThan(String classType, Date date);

    @Query("select wo from  WorkOrder wo where wo.studentId=?1 and (wo.startTime between ?2 and ?3) and wo.isFreeze=0  and wo.slotId in(?4) order by wo.startTime ")
    public List<WorkOrder> findByMyClasses(Long studentId, Date beginDate, Date endDate, List<Integer> slots);

    List<WorkOrder> findBySmallClassId(Long smallClassId);

    WorkOrder findBySmallClassIdAndStudentId(Long smallClassId, Long studentId);

    List<WorkOrder> findByClassTypeAndSmallClassIdIsNullAndStartTimeBetween(String name, Date date, Date deadDate);
}
