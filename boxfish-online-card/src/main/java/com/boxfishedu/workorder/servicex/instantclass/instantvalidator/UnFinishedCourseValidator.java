package com.boxfishedu.workorder.servicex.instantclass.instantvalidator;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.accountcardinfo.OnlineAccountService;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * Created by hucl on 16/11/4.
 * 30分钟内有未完成的课程需要等待才能进行
 */
@Order(5)
@Component
public class UnFinishedCourseValidator implements InstantClassValidator {
    @Autowired
    private OnlineAccountService onlineAccountService;

    @Autowired
    private InstantClassJpaRepository instantClassJpaRepository;

    private org.slf4j.Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Override
    public int preValidate() {
        InstantRequestParam instantRequestParam = ThreadLocalUtil.instantRequestParamThreadLocal.get();
        Optional<WorkOrder> passedCardOptional=workOrderJpaRepository
                .findTop1ByStudentIdAndStartTimeLessThanOrderByStartTimeDesc(instantRequestParam.getStudentId(),new Date());
        if(!passedCardOptional.isPresent()){
            return InstantClassRequestStatus.UNKNOWN.getCode();
        }
        Date begin=passedCardOptional.get().getStartTime();
        Date end=passedCardOptional.get().getEndTime();
        Date deadLine=DateUtil.addMinutes(begin,30);

        if(deadLine.before(new Date())){
            return InstantClassRequestStatus.UNKNOWN.getCode();
        }

        long minute=(deadLine.getTime()-new Date().getTime())/(1000*60);
        String beginStr=DateUtil.dateTrimYear(begin).substring(0,5);
        String endStr=DateUtil.dateTrimYear(end).substring(0,5);
        StringBuilder builder=new StringBuilder().append("已安排")
                .append(String.join("-",beginStr,endStr)).append("的课程;请等待外教发起邀请,或");
        if(minute!=0){
            builder.append(minute).append("分钟后再试");
        }
        else{
            long second= (deadLine.getTime()-new Date().getTime())/(1000);
            builder.append(second).append("秒钟后再试");
        }
        ThreadLocalUtil.unFinishedCourses30MinutesTips.set(builder.toString());
        return InstantClassRequestStatus.UNFINISHED_COURSE.getCode();
    }

}
