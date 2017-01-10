package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.BaseTimeSlots;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * Created by LuoLiBing on 16/11/23.
 */
public interface BaseTimeSlotJpaRepository extends JpaRepository<BaseTimeSlots, Long> {

    //@Cacheable(value = CacheKeyConstant.BASE_TIME_SLOTS, key = "T(java.util.Objects).hash(#classDate,#teachingType,#clientType)")
    List<BaseTimeSlots> findByClassDateAndTeachingTypeAndClientType(Date classDate, Integer teachingType, Integer clientType);

    @Query(value = "select max(e.classDate) from BaseTimeSlots e")
    Date findMaxDate();

    public Page<BaseTimeSlots> findByTeachingTypeAndClassDateBetween(Integer teachingType, Date beginDate, Date endDate, Pageable pageable);

    public List<BaseTimeSlots> findByTeachingTypeAndClassDateBetween(Integer teachingType, Date beginDate, Date endDate);
}
