package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.StStudentApplyRecords;
import com.boxfishedu.workorder.entity.mysql.StStudentApplyRecordsResult;
import com.google.common.base.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * Created by jiaozijun on 16/12/13.
 */
public interface StStudentApplyRecordsJpaRepository extends JpaRepository<StStudentApplyRecords, Long> {

    public StStudentApplyRecords findTop1ByWorkOrderIdAndApplyStatus(Long workOrderId,StStudentApplyRecords.ApplyStatus applyStatus);

    @Query(value = "select count(distinct s.studentId) from StStudentApplyRecords s where s.teacherId=?1 and s.applyTime>?2 and s.isRead=?3")
    Optional<Long> getUnreadInvitedNum(Long teacherId,Date date,StStudentApplyRecords.ReadStatus status);


    @Query(value = "select new com.boxfishedu.workorder.entity.mysql.StStudentApplyRecordsResult(s.studentId,s.applyTime,s.teacherId,s.isRead,count(s.workOrderId)) from StStudentApplyRecords s where s.teacherId=?1 and s.applyTime>?2  group by s.studentId")
    Page<StStudentApplyRecordsResult> getmyInviteList(Long teacherId, Date date, Pageable pageable);


    public Page<StStudentApplyRecords> findByApplyTimeGreaterThanAndTeacherIdAndStudentId(Date date ,Long teacherId,Long studentId, Pageable pageable);

    public StStudentApplyRecords findTop1ByStudentIdAndApplyStatusAndSkuIdAndTeacherIdNotNullOrderByApplyTimeDesc(Long studentId,StStudentApplyRecords.ApplyStatus applyStatus,Integer skuId);

    @Modifying
    @Query("update StStudentApplyRecords o set o.isRead= ?1    where o.teacherId= ?2 and o.studentId= ?3")
    int setFixedIsReadFor(StStudentApplyRecords.ReadStatus readStatus,Long teacherId, Long studentId);

    @Modifying
    @Query("update StStudentApplyRecords o set  o.isRead= ?1, o.applyStatus=?2  where  o.id in (?3) ")
    int setFixedIsReadAndApplyStatusFor(StStudentApplyRecords.ReadStatus readStatus, StStudentApplyRecords.ApplyStatus applyStatus ,Long [] ids);
}
