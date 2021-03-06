package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.HolidayDay;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by jiaozijun on 16/2/29.
 */
public interface HolidayDayJpaRepository extends JpaRepository<HolidayDay, Long> {

}
