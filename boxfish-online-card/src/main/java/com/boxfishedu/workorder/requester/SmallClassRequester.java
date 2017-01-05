package com.boxfishedu.workorder.requester;

import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * Created by hucl on 17/1/5.
 */
@Component
@SuppressWarnings("ALL")
public class SmallClassRequester {
    @Autowired
    private UrlConf urlConf;

    @Autowired
    private RestTemplate restTemplate;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取用户学习难度：
     *
     * @param userId
     * @return 如:"LEVEL_2"
     */
    public String fetchUserDifficultyInfo(Long userId) {
        String url = String.format("%s/core/difficulty/%s", urlConf.getCourse_wudaokou_recommend_service(), userId);
        String difficulty = null;
        try {
            difficulty = restTemplate.getForObject(url, String.class);
            logger.debug("@fetchUserDifficultyInfo获取用户课程难度信息,url[{}],结果[{}]", url, difficulty);
            logger.debug("");
        } catch (Exception ex) {
            logger.error("@fetchUserDifficultyInfo获取用户课程难度信息失败,url[{}]", url, ex);
            throw new BusinessException("获取用户难度信息失败");
        }
        return difficulty;
    }

    /**
     * 获取用户学习计数
     *
     * @param userId
     * @param difficultyLevel 课程难度级别 :LEVEL_3
     * @return
     */
    public Integer fetchUserStudyInfo(Long userId, String difficultyLevel) {
        String url = String.format("%s/core/counter/%s/%s"
                , urlConf.getCourse_wudaokou_recommend_service(), userId, difficultyLevel);
        Integer info = null;
        try {
            info = restTemplate.getForObject(url, Integer.class);
            logger.debug("@fetchUserStudyInfo获取用户学习计数,url[{}],结果[{}]", url,info);
        } catch (Exception ex) {
            logger.error("@fetchUserStudyInfo获取用户学习计数,url[{}]", url, ex);
            throw new BusinessException("获取用户学习计数");
        }
        return info;
    }

    public RecommandCourseView fetchSmallClassCNCourse(List<Long> studentIds, String difficultyLevel, Integer seq, TutorTypeEnum tutorTypeEnum){
        String userIdsStr=StringUtils.arrayToDelimitedString(studentIds.toArray(),"-");
        String url=null;
        switch (tutorTypeEnum){
            case CN:
                url = String.format("%s/promote_xp/%s/%s/%s"
                        , urlConf.getCourse_wudaokou_recommend_service(), userIdsStr, difficultyLevel,seq);
                break;
            case FRN:
                url=null;
                break;
            default:
                throw new BusinessException("不支持的类型");

        }

        RecommandCourseView info = null;
        try {
            info = restTemplate.getForObject(url, RecommandCourseView.class);
            logger.debug("@fetchUserStudyInfo获取用户学习计数,url[{}],结果[{}]", url,info);
        } catch (Exception ex) {
            logger.error("@fetchUserStudyInfo获取用户学习计数,url[{}]", url, ex);
            throw new BusinessException("获取用户学习计数");
        }
        return info;

    }


}
