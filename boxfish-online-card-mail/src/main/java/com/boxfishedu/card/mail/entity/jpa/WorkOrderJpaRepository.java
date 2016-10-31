package com.boxfishedu.card.mail.entity.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by LuoLiBing on 16/10/31.
 */
@Repository
public class WorkOrderJpaRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> findNoCourseIdTimeOutList(Date startDate, Date endDate) {
        return jdbcTemplate.queryForList(
                "SELECT id,student_id,start_time from work_order where course_id is null and start_time BETWEEN ? and ? ORDER BY start_time ASC",
                new Object[]{startDate, endDate});
    }



}
