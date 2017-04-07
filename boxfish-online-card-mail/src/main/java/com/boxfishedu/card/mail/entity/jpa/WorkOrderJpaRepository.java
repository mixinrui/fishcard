package com.boxfishedu.card.mail.entity.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
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

//    @Query(value = "select new com.boxfishedu.workorder.entity.mysql.WorkOrderSameClass( wo1.id,wo2.id,wo1.teacherId,wo1.startTime )  from   WorkOrder wo1, WorkOrder wo2 " +
//            " where wo1.id>wo2.id and wo1.teacherId = wo2.teacherId and wo1.startTime=wo2.startTime    "
//            + " and wo1.isFreeze=0 and wo1.teacherId>0 and wo1.startTime BETWEEN NOW() AND ADDDATE(NOW(),30)  and   (  wo1.classType  not in ('SMALL','PBULIC')  or   wo1.classType is NULL )    "
//            + " and wo2.isFreeze=0 and wo2.teacherId>0 and wo2.startTime BETWEEN NOW() AND ADDDATE(NOW(),30)   and    (wo2.classType  not in ('SMALL','PBULIC') or   wo2.classType is NULL )  "
//    )
    public List<Map<String,Object>> getSameClassOfTeacher(){
        return jdbcTemplate.queryForList(
                " SELECT  wo1.id  id1  ,wo2.id id2 ,wo1.teacher_Id,wo1.start_Time    from   Work_Order wo1, Work_Order wo2  where wo1.id>wo2.id and wo1.teacher_Id = wo2.teacher_Id and wo1.start_Time=wo2.start_Time " +
                        " and wo1.is_Freeze=0 and wo1.teacher_Id>0 and wo1.start_Time BETWEEN NOW() AND ADDDATE(NOW(),30)  and   (  wo1.class_Type  not in ('SMALL','PBULIC')  or   wo1.class_Type is NULL) " +
                        " and wo2.is_Freeze=0 and wo2.teacher_Id>0 and wo2.start_Time BETWEEN NOW() AND ADDDATE(NOW(),30)   and    (wo2.class_Type  not in ('SMALL','PBULIC') or   wo2.class_Type is NULL)  ");
    }


}
