package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.entity.mysql.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * Created by oyjun on 16/2/29.
 */
public interface ServiceJpaRepository extends JpaRepository<Service,Long> {
    public List<Service> findByOrderId(Long orderId);

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

    @Query("select s from Service s where s.studentId=?1 and s.productType=?2")
    List<Service> getForeignCommentServiceCount(long studentId, int productType);

    @Query("select s from Service s where s.studentId=?1 and s.coursesSelected=?2")
    List<Service> getServiceSelectedStatus(long studentId,int coursesSelected);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Service s where s.studentId=?1 and s.productType=?2 and s.amount>0")
    Page<Service> getFirstAvailableForeignCommentService(long studentId, int productType, Pageable pageable);

    @Query("select count(s) from Service s where s.studentId=?1 and s.productType=?2 and s.amount>0")
    Integer getAvailableForeignCommentServiceCount(long studentId, int productType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public Service findById(Long id);

    /*********兼容老版本*************/
    Service findTop1ByOrderIdAndComboType(Long orderId, String comboType);

    List<Service> findByStudentIdAndCoursesSelected(Long studentId,Integer coursesSelected);

    List<Service> findByStudentIdAndComboTypeAndTutorTypeAndCoursesSelectedAndProductType(Long studentId, String comboType,String tutorType,Integer selectedFlag,Integer productType);

    List<Service> findByStudentIdAndComboTypeInAndTutorTypeAndCoursesSelectedAndProductType(Long studentId, String[] comboTypes,String tutorType,Integer selectedFlag,Integer productType);

    List<Service> findByStudentIdAndComboTypeAndCoursesSelectedAndProductType(Long studentId, String comboType ,Integer selectedFlag,Integer productType);

    List<Service> findByStudentIdAndComboTypeInAndCoursesSelectedAndProductType(Long studentId, String[] comboType ,Integer selectedFlag,Integer productType);

    //查找出学生所有状态的工单
    @Query("select distinct sv.studentId from Service sv")
    public List<Long> findDistinctUsersFromService();

    //初始化客服系统中外教点评
    @Query("SELECT s FROM Service s where s.productType = '1002'")
    List<Service> findByProductType();
}
