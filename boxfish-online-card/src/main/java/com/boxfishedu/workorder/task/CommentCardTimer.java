package com.boxfishedu.workorder.task;

import com.boxfishedu.workorder.service.commentcard.ForeignTeacherCommentCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by ansel on 16/8/4.
 */
//@Component
@RestController
@EnableScheduling
public class CommentCardTimer {

    private Logger logger = LoggerFactory.getLogger(CommentCardTimer.class);

    @Autowired
    ForeignTeacherCommentCardService foreignTeacherCommentCardService;

    /**
      * 检查24小时内没有点评学生的外教。。每天凌晨0点定时执行
      * 测试时为5分钟检查一次
      * 针对线上的三台服务器,这里不适合使用定时任务,而使用 boxFish-online-card-timer项目
      */
//    @Scheduled(cron = "0 0/1 * * * ?")
//    @Scheduled(cron = "0/1 * * * * ?")
    public void judgeCommentCard(){

        logger.info("@CommentCardTimer>>>>>COMMENT_CARD_NO_ANSWER>>>>检查24小时和48小时内为点评的外教,判定重新分配或返还学生购买点评次数");
        foreignTeacherCommentCardService.foreignTeacherCommentUnAnswer();
        foreignTeacherCommentCardService.foreignTeacherCommentUnAnswer2();
        foreignTeacherCommentCardService.foreignUndistributedTeacherCommentCards();
    }

//    @Scheduled(cron = "0 0/1 * * * ?")
    //@RequestMapping(value = "/notify/member")
    public void notifyCommentCard() {
        foreignTeacherCommentCardService.notifyExpireCommentCards();
    }
}
