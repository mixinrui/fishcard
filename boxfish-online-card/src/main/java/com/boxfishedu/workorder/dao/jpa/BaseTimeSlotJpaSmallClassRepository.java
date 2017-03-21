package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.BaseTimeSlotsSmallClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * Created by LuoLiBing on 16/11/23.
 */
public interface BaseTimeSlotJpaSmallClassRepository extends JpaRepository<BaseTimeSlotsSmallClass, Long> {

    //@Cacheable(value = CacheKeyConstant.BASE_TIME_SLOTS, key = "T(java.util.Objects).hash(#classDate,#teachingType,#clientType)")
    List<BaseTimeSlotsSmallClass> findByClassDateAndTeachingTypeAndClientType(Date classDate, Integer teachingType, Integer clientType);

    @Query(value = "select max(e.classDate) from BaseTimeSlotsSmallClass e")
    Date findMaxDate();

    Page<BaseTimeSlotsSmallClass> findByTeachingTypeAndClassDateBetween(Integer teachingType, Date beginDate, Date endDate, Pageable pageable);

    List<BaseTimeSlotsSmallClass> findByTeachingTypeAndClassDateBetween(Integer teachingType, Date beginDate, Date endDate);
}
