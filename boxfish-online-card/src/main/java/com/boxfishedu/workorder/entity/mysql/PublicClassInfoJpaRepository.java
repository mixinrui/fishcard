package com.boxfishedu.workorder.entity.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Created by LuoLiBing on 17/1/9.
 */
public interface PublicClassInfoJpaRepository extends JpaRepository<PublicClassInfo, Long> {

    Optional<PublicClassInfo> findByClassDateAndSlotIdAndStudentId(LocalDate classDate, Integer slotId, Long studentId);

    Optional<PublicClassInfo> findBySmallClassIdAndStudentId(Long smallClassId, Long studentId);

    @Query(value = "select count(p) from PublicClassInfo p where p.classDate between ?1 and ?2 and p.studentId=?3")
    Integer findByClassDateRangeAndStudentId(LocalDate from, LocalDate to, Long studentId);

    @Query(value = "select count(p) from PublicClassInfo p where p.classDate=?1 and p.studentId=?2")
    Integer findByClassDateAndStudentId(LocalDate classDate, Long studentId);

    @Modifying
    @Query("update PublicClassInfo c set c.status =?1 where c.smallClassId = ?2 and c.studentId=?3")
    void updateStatus(Integer status, Long smallClassId, Long studentId);
}
