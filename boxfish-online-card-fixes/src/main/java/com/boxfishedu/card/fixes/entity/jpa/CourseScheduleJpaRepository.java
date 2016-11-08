package com.boxfishedu.card.fixes.entity.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Created by LuoLiBing on 16/11/3.
 */
public interface CourseScheduleJpaRepository extends JpaRepository<CourseSchedule, Long> {
    Optional<CourseSchedule> findTop1ByWorkorderId(Long workOrderId);
}
