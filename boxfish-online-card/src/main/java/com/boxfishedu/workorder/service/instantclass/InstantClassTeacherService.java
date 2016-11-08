package com.boxfishedu.workorder.service.instantclass;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.requester.InstantTeacherRequester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * Created by hucl on 16/11/7.
 */
@Component
public class InstantClassTeacherService {
    @Autowired
    private InstantTeacherRequester instantTeacherRequester;

    @Autowired
    private InstantClassJpaRepository instantClassJpaRepository;

    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    private CourseOnlineRequester courseOnlineRequester;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    public void dealFetchedTeachersAsync(InstantClassCard instantClassCard){
        instantClassJpaRepository.incrementrequestTeacherTimes(instantClassCard.getId());
        threadPoolManager.execute(new Thread(() -> {dealInstantFetchedTeachers(instantClassCard);}));
    }

    public void dealInstantFetchedTeachers(InstantClassCard instantClassCard){
        instantClassJpaRepository.incrementrequestTeacherTimes(instantClassCard.getId());
        Optional<List<Long>> teacherIdsOptional=Optional.empty();
        try {
            teacherIdsOptional=instantTeacherRequester.getInstantTeacherIds(instantClassCard);
        }
        catch (Exception ex){
            logger.error("@dealInstantFetchedTeachers#user:{}获取实时推荐教师失败,将匹配状态设置为未匹配上"
                    ,instantClassCard.getStudentId());
            this.updateNomatchStatus(instantClassCard);
        }
        if(CollectionUtils.isEmpty(teacherIdsOptional.get())){
            //没有教师的情况,需要返回无匹配
            this.updateNomatchStatus(instantClassCard);
        }
        //匹配上老师,则向教师推送抢单的消息
        courseOnlineRequester.notifyInstantClassMsg(instantClassCard,teacherIdsOptional.get());
    }

    private void updateNomatchStatus(InstantClassCard instantClassCard){
        instantClassJpaRepository.updateStatus(instantClassCard.getId(), InstantClassRequestStatus.NO_MATCH.getCode());
    }


}
