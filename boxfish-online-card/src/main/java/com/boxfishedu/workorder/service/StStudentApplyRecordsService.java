package com.boxfishedu.workorder.service;


import com.boxfishedu.workorder.dao.jpa.StStudentApplyRecordsJpaRepository;

import com.boxfishedu.workorder.entity.mysql.StStudentApplyRecords;

import com.boxfishedu.workorder.entity.mysql.StStudentApplyRecordsResult;
import com.boxfishedu.workorder.service.base.BaseService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import com.google.common.base.Optional;


/**
 * Created by hucl on 16/3/31.
 */
@Component
public class StStudentApplyRecordsService extends BaseService<StStudentApplyRecords, StStudentApplyRecordsJpaRepository, Long> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // 获取已经指定过老师的记录 AssignTeacherApplyStatusEnum
    public StStudentApplyRecords  getStStudentApplyRecordsBy(Long workOrderId,StStudentApplyRecords.MatchStatus matchStatus){
        return jpa.findTop1ByWorkOrderIdAndMatchStatusAndValid(workOrderId,matchStatus,StStudentApplyRecords.VALID.yes);
    }


   public Integer getUnreadInvitedNum(Long teacherId, Date date,Date beginDate,Date endDate){
       Optional<Long> unread = jpa.getUnreadInvitedNum(teacherId,date, StStudentApplyRecords.ReadStatus.no,StStudentApplyRecords.VALID.yes,StStudentApplyRecords.ApplyStatus.agree,   beginDate,endDate,StStudentApplyRecords.MatchStatus.wait2apply);
        return unread.isPresent()?unread.get().intValue():0;
    }
    public List<StStudentApplyRecords> getUnreadStStudentRecords(Long teacherId, Date date){
        return jpa.getUnreadStStudentRecords(teacherId,date, StStudentApplyRecords.ReadStatus.no,StStudentApplyRecords.VALID.yes);
    }




    public Page<StStudentApplyRecordsResult> getmyInviteList(Long teacherId, Date date,Date startTime,Date endTime, Pageable pageable){
        return  jpa.getmyInviteList(teacherId,date,StStudentApplyRecords.VALID.yes,StStudentApplyRecords.ApplyStatus.agree,startTime,endTime,StStudentApplyRecords.MatchStatus.wait2apply, pageable);
    }

    public Page<StStudentApplyRecords> getMyClassesByStudentId(Long teacherId,Long studentId,Date date,Date startTime,Date endTime,Pageable pageable){
        return  jpa.findByApplyTimeGreaterThanAndTeacherIdAndStudentIdAndValid(date,teacherId,studentId,StStudentApplyRecords.VALID.yes, StStudentApplyRecords.ApplyStatus.agree, startTime,endTime,StStudentApplyRecords.MatchStatus.wait2apply, pageable);
    }

    public List<StStudentApplyRecords> getMyClassesByStudentIdNoPage(Long teacherId,Long studentId,Date date,Date startTime,Date endTime){
        return  jpa.findByApplyTimeGreaterThanAndTeacherIdAndStudentIdAndValid(date,teacherId,studentId,StStudentApplyRecords.VALID.yes, StStudentApplyRecords.ApplyStatus.agree, startTime,endTime,StStudentApplyRecords.MatchStatus.matched);
    }

    public List<StStudentApplyRecords> getMyClassesByStudentId(Long teacherId,Long studentId,Date date,Date startTime,Date endTime){
        return  jpa.findByApplyTimeGreaterThanAndTeacherIdAndStudentIdAndValid(date,teacherId,studentId,StStudentApplyRecords.VALID.yes,StStudentApplyRecords.ApplyStatus.agree, startTime,endTime);
    }

    public StStudentApplyRecords findMyLastAssignTeacher(Long studentId,Integer skuId){
        return jpa.findTop1ByStudentIdAndApplyStatusAndSkuIdAndValidAndTeacherIdNotNullOrderByApplyTimeDesc(studentId,StStudentApplyRecords.ApplyStatus.agree,skuId,StStudentApplyRecords.VALID.yes);
    }

    public int upateReadStatusByStudentId(Long teacherId, Long studentId){
        return  jpa.setFixedIsReadFor(StStudentApplyRecords.ReadStatus.yes,teacherId,studentId,StStudentApplyRecords.VALID.yes);
    }

}
