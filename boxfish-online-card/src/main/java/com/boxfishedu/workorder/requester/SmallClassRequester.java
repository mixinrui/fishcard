package com.boxfishedu.workorder.requester;

import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Array;
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
        String url = String.format("%s/difficulty/%s", urlConf.getCourse_wudaokou_recommend_service(), userId);
        String difficulty = null;
        try {
            difficulty = restTemplate.getForObject(url, String.class);
            logger.debug("@fetchUserDifficultyInfo获取用户课程难度信息,url[{}],结果[{}]", url, difficulty);
        } catch (Exception ex) {
            logger.error("@fetchUserDifficultyInfo获取用户课程难度信息失败,url[{}]", url, ex);
            throw new BusinessException("获取用户难度信息失败");
        }
        //api返回的数据真是诡异,非得加上引号..
        if (difficulty.contains("\"")) {
            difficulty = StringUtils.trimLeadingCharacter(difficulty, '\"');
            difficulty = StringUtils.trimTrailingCharacter(difficulty, '\"');
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
        String url = String.format("%s/counter/%s/%s"
                , urlConf.getCourse_wudaokou_recommend_service(), userId, difficultyLevel);
        Integer info = null;
        try {
            info = restTemplate.getForObject(url, Integer.class);
            logger.debug("@fetchUserStudyInfo获取用户学习计数,url[{}],结果[{}]", url, info);
        } catch (Exception ex) {
            logger.error("@fetchUserStudyInfo获取用户学习计数,url[{}]", url, ex);
            throw new BusinessException("获取用户学习计数");
        }
        return info;
    }

    /**
     * 小班推荐－外教,中教：
     *
     * @param studentIds      小班课学生id列表
     * @param difficultyLevel 难度级别
     * @param seq             序列
     * @param tutorTypeEnum   中外教类型
     * @return
     */
    public RecommandCourseView fetchClassCourseByUserIds(
            List<Long> studentIds, String difficultyLevel, Integer seq, TutorTypeEnum tutorTypeEnum) {
        String userIdsStr = StringUtils.arrayToDelimitedString(studentIds.toArray(), "-");
        String url = null;
        switch (tutorTypeEnum) {
            case CN:
                url = String.format("%s/promote_xp/%s/%s/%s"
                        , urlConf.getCourse_wudaokou_recommend_service(), userIdsStr, difficultyLevel, seq);
                break;
            case FRN:
                url = String.format("%s/ultimate_xp/%s/%s/%s"
                        , urlConf.getCourse_wudaokou_recommend_service(), userIdsStr, difficultyLevel, seq);
                break;
            default:
                throw new BusinessException("不支持的类型");
        }

        RecommandCourseView info = null;
        try {
            info = restTemplate.getForObject(url, RecommandCourseView.class);
            logger.debug("@fetchClassCourseByUserIds获取用户课程成功,url[{}],结果[{}]", url, info);
        } catch (Exception ex) {
            logger.error("@fetchClassCourseByUserIds获取用户课程失败,url[{}]", url, ex);
            throw new BusinessException("获取用户课程失败");
        }
        return info;
    }

    public RecommandCourseView fetchSmallClassCourse(
            List<WorkOrder> workOrders, String difficultyLevel, Integer seq, TutorTypeEnum tutorTypeEnum) {
        List<Long> students = Lists.newArrayList();
        workOrders.forEach(workOrder -> {
            students.add(workOrder.getStudentId());
        });
        return this.fetchClassCourseByUserIds(students, difficultyLevel, seq, tutorTypeEnum);
    }

    public RecommandCourseView fetchSmallClassCNCourse(
            List<Long> studentIds, String difficultyLevel, Integer seq) {
        return this.fetchClassCourseByUserIds(studentIds, difficultyLevel, seq, TutorTypeEnum.CN);
    }

    /**
     * @param smallClass
     * @return 获取公开课
     */
    public RecommandCourseView getPublicCourse(SmallClass smallClass) {
        String url = String.format("%s/open/%s", urlConf.getCourse_wudaokou_recommend_service(), smallClass.getDifficultyLevel());
        RecommandCourseView recommandCourseView = null;
        try {
            recommandCourseView = restTemplate.getForObject(url, RecommandCourseView.class);
            logger.info("@getPublicCourse推荐课程成功,url[{}],结果[{}]", url, JacksonUtil.toJSon(recommandCourseView));
        } catch (Exception ex) {
            logger.error("@getPublicCourse推荐公开课失败url[{}]", url, ex);
            throw new BusinessException("推荐课程失败");
        }
        return recommandCourseView;
    }


}
