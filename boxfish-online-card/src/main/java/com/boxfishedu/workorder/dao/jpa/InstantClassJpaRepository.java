package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.Service;
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

public interface InstantClassJpaRepository extends JpaRepository<InstantClassCard,Long> {
    public Optional<InstantClassCard> findByWorkorderId(Long workOrderId);

    public Optional<InstantClassCard> findByStudentIdAndClassDateAndSlotId(Long studentId, Date classDate, Long slotId) ;

    @Transactional
    @Modifying
    @Query("update InstantClassCard icc set icc.requestTeacherTimes =icc.requestTeacherTimes+1 where icc.id= ?1")
    public void incrementrequestTeacherTimes(Long id);

    @Transactional
    @Modifying
    @Query("update InstantClassCard icc set icc.requestTeacherTimes =icc.requestTeacherTimes+1,status=?2 where icc.id= ?1")
    public void updateRequestTeacherTimesAndStatus(Long id,Integer status);

    @Transactional
    @Modifying
    @Query("update InstantClassCard icc set status=?2 where icc.id= ?1")
    public void updateStatus(Long id,Integer status);
}
