package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by oyjun on 16/2/29.
 */
public interface ServiceJpaRepository extends JpaRepository<Service, Long> {
    public List<Service> findByOrderId(Long orderId);

    public List<Service> findByOrderIdAndComboTypeAndCoursesSelected(Long orderId, String comboType, Integer courseSelected);

    //使用top1,是由于按照逻辑,根据查询条件只应该有一条结果返回
    public Service findTop1ByOrderIdAndSkuId(Long orderId, Long skuId);

    List<Service> findByOrderIdAndProductType(Long orderId, Integer productType);

    //鱼卡中心查询学生的接口
    @Query("select sv from Service sv where sv.studentId=?1 and sv.skuId!=?2 and sv.skuId!=?3")
    public List<Service> findAllServicesByUser(Long studentId, Long skuIdPlanner, Long skuIdAnswer);

    //鱼卡中心查询学生的接口
    @Query("select sv from Service sv where sv.studentId=?1 and sv.orderCode=?2 and sv.skuId!=?3 and sv.skuId!=?4")
    public List<Service> findServicesByUserAndCond(Long studentId, String orderCode, Long skuIdPlanner, Long skuIdAnswer);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select sv from  Service sv where sv.id=?1 ")
    public Service findByIdForUpdate(Long id);

    public Service findTop1ByOrderId(Long orderId);

    @Query("select s from Service s where s.studentId=?1 and s.productType=?2 and s.endTime>CURRENT_DATE")
    List<Service> getForeignCommentServiceCount(long studentId, int productType);

    @Query("select s from Service s where s.studentId=?1 and s.coursesSelected=?2")
    List<Service> getServiceSelectedStatus(long studentId, int coursesSelected);

    // 当前可用的点评, (有可用次数的, 在有效期以内的, 并且取截止时间最近的service)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Service s where s.studentId=?1 and s.productType=?2 and s.amount>0 and s.endTime>CURRENT_DATE order by s.endTime asc")
    Page<Service> getFirstAvailableForeignCommentService(long studentId, int productType, Pageable pageable);

    // 在有效期以内的外教点评总数
    @Query("select count(s) from Service s where s.studentId=?1 and s.productType=?2 and s.amount>0 and s.endTime>CURRENT_DATE")
    Integer getAvailableForeignCommentServiceCount(long studentId, int productType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public Service findById(Long id);

    // 外教点评带会员过期时间
    @Query("select s.studentId from Service s where s.productType=?1 and s.amount>0 and s.endTime between ?2 and ?3 and s.userType=?4")
    Set<Long> getAvailableForeignCommentService(Integer productType, Date from, Date to, Integer userType);

    @Query("select s.studentId from Service s where s.productType=1002")
    Set<Long> getForeignCommentStudentIds();

    /*********
     * 兼容老版本
     *************/
    Service findTop1ByOrderIdAndComboType(Long orderId, String comboType);

    List<Service> findByStudentIdAndCoursesSelected(Long studentId, Integer coursesSelected);

    List<Service> findByStudentIdAndComboTypeAndTutorTypeAndCoursesSelectedAndProductType(Long studentId, String comboType, String tutorType, Integer selectedFlag, Integer productType);

    List<Service> findByStudentIdAndComboTypeInAndTutorTypeAndCoursesSelectedAndProductType(Long studentId, String[] comboTypes, String tutorType, Integer selectedFlag, Integer productType);

    List<Service> findByStudentIdAndComboTypeAndCoursesSelectedAndProductType(Long studentId, String comboType, Integer selectedFlag, Integer productType);

    List<Service> findByStudentIdAndComboTypeInAndCoursesSelectedAndProductType(Long studentId, String[] comboType, Integer selectedFlag, Integer productType);

    List<Service> findByStudentIdAndCoursesSelectedAndProductType(Long studentId, Integer selectedFlag, Integer productType);
    
    //查找出学生所有状态的工单
    @Query("select distinct sv.studentId from Service sv")
    public List<Long> findDistinctUsersFromService();

    //初始化客服系统中外教点评
    @Query("SELECT s FROM Service s where s.productType = '1002'")
    List<Service> findByProductType();

    List<Service> findByStudentIdAndCoursesSelectedAndProductTypeAndComboTypeNotIn(Long studentId, int i, int value, List<String> fiteredComboTypes);
}
