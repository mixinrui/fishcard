package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.workorder.common.exception.NotFoundException;
import com.boxfishedu.workorder.common.log.CommentCardLog;
import com.boxfishedu.workorder.dao.jpa.CommentCardJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.service.commentcard.sdk.CommentCardSDK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by ansel on 16/11/22.
 */
@Service
public class CommentCardInitiateService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CommentCardJpaRepository commentCardJpaRepository;

    @Autowired
    private CommentCardSDK commentCardSDK;

    public Object initiateCourseTypeAndDifficulty(){
        List<CommentCard> commentCardList = commentCardJpaRepository.initiateCourseTypeAndDifficulty();
        Map courseMap;
        int count = 0;
        for (CommentCard commentCard:commentCardList){
            logger.info("###initiateCourseTypeAndDifficulty1## 初始化点评卡{}",commentCard);
            try {
                courseMap = commentCardSDK.initiateTypeAndDifficulty(commentCard.getCourseId());
                if (Objects.nonNull(courseMap)){
                    commentCard.setCourseType(courseMap.get("type") == null?"":courseMap.get("type").toString());
                    commentCard.setCourseDifficulty(courseMap.get("difficulty") == null?"":courseMap.get("difficulty").toString());
                }
                commentCardJpaRepository.save(commentCard);
            }catch (NotFoundException notFound){
                logger.info("###initiateCourseTypeAndDifficulty2### 找不到对应课程信息 课程id为{}",commentCard.getCourseId());
                logger.error(new CommentCardLog()
                        .data(commentCardList)
                        .errorLevel()
                        .operation("外教点评:初始化课程难度和类型")
                        .toString());
                count += 1;
            }
        }
        logger.info("###initiateCourseTypeAndDifficutlty3### 初始化外教点评中的课程难度和类型,本次初始化个数为:"+ commentCardList.size() + ", 其中" + count + "个没有课程信息。");
        return commentCardList.size();
    }

    /**
     * 定时更新课程难度和类型
     */
    @Transactional
    public void timeToUpdateTypeAndDifficulty(){
        List<String> courseId = commentCardJpaRepository.getComCourseIdList();
        Map courseMap;
        int count = 0;
        for (String courseIdStr:courseId){
            logger.info("###timeToUpdateTypeAndDifficulty## 修改课程id为{}的相关信息",courseIdStr);
            try {
                courseMap = commentCardSDK.initiateTypeAndDifficulty(courseIdStr);
                if (Objects.nonNull(courseMap)){
                    commentCardJpaRepository.updateCourseTypeAndDifficulty(courseMap.get("type") == null?"":courseMap.get("type").toString(),
                            courseMap.get("difficulty") == null?"":courseMap.get("difficulty").toString(),courseIdStr);
                }
            }catch (NotFoundException notFound){
                logger.info("###timeToUpdateTypeAndDifficulty2### 找不到对应课程信息 课程id为{}",courseIdStr);
                logger.error(new CommentCardLog()
                        .data(courseId)
                        .errorLevel()
                        .operation("外教点评:定时更新课程难度和类型")
                        .toString());
                count += 1;
            }
        }
        logger.info("###timeToUpdateTypeAndDifficulty3### 更新课程难度和类型,本次更新个数为:"+ courseId.size() + ", 其中" + count + "个没有课程信息。");
    }
}
