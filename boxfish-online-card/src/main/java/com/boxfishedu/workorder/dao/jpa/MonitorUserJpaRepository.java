package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.MonitorUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * Created by ansel on 2017/3/21.
 */
public interface MonitorUserJpaRepository extends JpaRepository<MonitorUser, Long>{

    @Query("select mu from MonitorUser mu where mu.enabled = 1 and mu.userType = 'student'")
    List<MonitorUser> getEnabledUser();

    MonitorUser findByUserId(Long userId);

    MonitorUser findByUserIdAndEnabled(Long userId,Integer enabled);

    @Modifying
    @Query("update MonitorUser mu set mu.enabled = 1,mu.updateTime = ?1 where mu.userId = ?2")
    void enabledMonitorUser(Date updateTime,Long userId);

    @Modifying
    @Query("update MonitorUser mu set mu.enabled = 0,mu.updateTime = ?1 where mu.userId = ?2")
    void disabledMonitorUser(Date updateTime,Long userId);

    @Query("select min(mu.avgSum) from MonitorUser mu where mu.enabled = 1 and mu.userType = 'student'")
    Integer getMinAvgSum();

    @Modifying
    @Query("update MonitorUser mu set mu.avgSum = mu.avgSum + 1 where mu.id = ?1")
    void updateAvgSum(Long id);

    MonitorUser findTop1ByUserTypeAndEnabledOrderByAvgSum(String userType, Integer enabled);



}
