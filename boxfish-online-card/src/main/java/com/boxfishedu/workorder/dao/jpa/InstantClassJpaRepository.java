package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import javax.swing.text.html.Option;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface InstantClassJpaRepository extends JpaRepository<InstantClassCard, Long> {
    Optional<InstantClassCard> findByWorkorderId(Long workOrderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select icc from  InstantClassCard icc where icc.id=?1 ")
    InstantClassCard findForUpdate(Long id);

    Optional<InstantClassCard> findByStudentIdAndClassDateAndSlotId(Long studentId, Date classDate, Long slotId);

    @Transactional
    @Modifying
    @Query("update InstantClassCard icc set icc.requestTeacherTimes =icc.requestTeacherTimes+1,icc.requestMatchTeacherTime=?2,icc.updateTime=?2 where icc.id= ?1")
    void incrementrequestTeacherTimes(Long id, Date date);

    @Transactional
    @Modifying
    @Query("update InstantClassCard icc set icc.requestTeacherTimes =icc.requestTeacherTimes+1,status=?2 where icc.id= ?1")
    void updateRequestTeacherTimesAndStatus(Long id, Integer status);

    @Transactional
    @Modifying
    @Query("update InstantClassCard icc set icc.resultReadFlag = ?2 where icc.id= ?1")
    void updateReadFlag(Long id, Integer resultReadFlag );

    @Transactional
    @Modifying
    @Query("update InstantClassCard icc set icc.matchResultReadFlag = ?2 where icc.id= ?1")
    void updateMatchedReadFlag(Long id, Integer resultReadFlag );

    @Transactional
    @Modifying
    @Query("update InstantClassCard icc set icc.resultReadFlag = ?2 ,icc.status=?3  where icc.id= ?1")
    void updateReadFlagAnsStatus(Long id, Integer resultReadFlag ,Integer status);

    @Transactional
    @Modifying
    @Query("update InstantClassCard icc set status=?2 where icc.id= ?1")
    void updateStatus(Long id, Integer status);

    List<InstantClassCard> findByRequestMatchTeacherTimeBetweenAndStatusIn(Date startDate, Date endDate, Integer[] statuses);

    Optional<InstantClassCard> findTop1ByStudentIdAndCreateTimeAfterOrderByCreateTimeDesc(Long studentId,Date date);

    Optional<InstantClassCard> findTop1ByStudentIdAndRequestMatchTeacherTimeAfterOrderByCreateTimeDesc(Long studentId,Date date);

    @Query(value = "select max(icc) from InstantClassCard icc where studentId=? and status=?")
    Optional<InstantClassCard> findLatestMatchedInstantCard(Long studentId,Integer status);

    List<InstantClassCard> findByCreateTimeLessThanAndWorkorderId(Date deadLine, Long workOrderId);
}
