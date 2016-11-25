package com.boxfishedu.card.fixes.entity.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * Created by LuoLiBing on 16/11/23.
 */
public interface BaseTimeSlotJpaRepository extends JpaRepository<BaseTimeSlots, Long> {

    List<BaseTimeSlots> findByClassDateAndTeachingTypeAndClientType(Date classDate, Integer teachingType, Integer clientType);

    @Query(value = "select max(e.classDate) from BaseTimeSlots e")
    Date findMaxDate();
}
