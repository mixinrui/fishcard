package com.boxfishedu.card.fixes.entity.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by LuoLiBing on 16/11/3.
 */
public interface WorkOrderJpaRepository extends JpaRepository<WorkOrder, Long> {

    @Query(value = "select w from WorkOrder w where w.status<=30")
    List<WorkOrder> findNotFinishWorkOrder();
}
