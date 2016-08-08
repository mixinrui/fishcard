package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.List;

/**
 * Created by oyjun on 16/2/29.
 */
public interface ServiceJpaRepository extends JpaRepository<Service,Long> {
    public List<Service> findByOrderId(Long orderId);

    //使用top1,是由于按照逻辑,根据查询条件只应该有一条结果返回
    public Service findTop1ByOrderIdAndSkuId(Long orderId, Long skuId);

    Service findTop1ByOrderIdAndComboType(Long orderId, String comboType);

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
}
