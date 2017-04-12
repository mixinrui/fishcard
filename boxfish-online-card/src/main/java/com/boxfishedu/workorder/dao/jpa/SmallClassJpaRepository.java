package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.MonitorTeacherIdASCForm;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * Created by LuoLiBing on 17/1/9.
 */
public interface SmallClassJpaRepository extends JpaRepository<SmallClass, Long> {

    List<SmallClass> findByClassDateAndSlotIdAndClassType(Date classDate, Integer slotId, String classType);

    @Query("select s from SmallClass s order by s.classDate")
    Page<SmallClass> findPage(Pageable pageable);

    List<SmallClass> findByClassDateAndClassType(Date classDate, String classType);

    @Query("select s from SmallClass s where s.startTime between ?1 and ?2 and s.classType=?3")
    List<SmallClass> findByStartTimeRange(Date from, Date to, String classType);

    @Query("select s from SmallClass s where s.startTime between ?1 and ?2 and s.classType=?3 and s.difficultyLevel=?4")
    List<SmallClass> findByStartTimeRangeLevel(Date from, Date to, String classType,String level);


    List<SmallClass> findByClassTypeAndStartTimeGreaterThan(String classType, Date date);

    List<SmallClass> findByClassTypeAndStartTimeLessThan(String classType, Date date);

    @Query("select s from SmallClass s where s.endTime between ?1 and ?2")
    List<SmallClass> findPublicAndSmallClassForDestroy(Date from, Date to);

    @Query("select s from SmallClass s where s.id >= 1882 and s.id <= 1893")
    List<SmallClass> findMockData();

    SmallClass findById(Long id);

    @Query("select sc from SmallClass sc where sc.id in (select muc.classId from MonitorUserCourse muc where muc.startTime between ?1 and ?2 and muc.classType = ?3 and muc.userId = ?4)" +
            " and sc.startTime between ?1 and ?2 and sc.classType = ?3")
    Page<SmallClass> findMonitorUserCourse(Date startTime, Date endTime,String classType, Long studentId,Pageable pageable);

    //小班课换老师操作
    @Modifying
    @Query("update SmallClass o set o.teacherId= ?1 ,o.teacherName= ?2 ,o.groupId  = ?3,o.chatRoomId = ?4   where o.id =  ?5 ")
    int setFixedTeacherIdAndTeacherNameAndGroupIdAndChatRoomIdFor(Long teacherId ,String teacherName,String groupId,Long chatRoomId, Long id);

}
