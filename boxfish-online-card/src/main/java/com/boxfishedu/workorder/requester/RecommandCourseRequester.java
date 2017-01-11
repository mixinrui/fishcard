package com.boxfishedu.workorder.requester;

import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.RecommandedCourseService;
import com.boxfishedu.workorder.servicex.studentrelated.recommend.RecommendCourseType;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Created by hucl on 16/6/17.
 */
@Component
@SuppressWarnings("ALL")
public class RecommandCourseRequester {
    @Autowired
    private UrlConf urlConf;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    private RecommandedCourseService recommandedCourseService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 换课
     *
     * @param workOrder
     * @return
     */
    public RecommandCourseView changeCourse(WorkOrder workOrder) {
        Service service = workOrder.getService();
        String tutorType = service.getTutorType();
        logger.debug("@RecommandCourseRequester#changeCourse,参数tutorType[{}]", tutorType);
        if (Objects.equals(tutorType, TutorType.CN.name()) || Objects.equals(tutorType, TutorType.MIXED.name())) {
            return changeChineseCourse(workOrder);
        } else {
            // 终极梦想换课
            return changeForeignCourse(workOrder);
        }
    }


    /**
     * 核心素养
     *
     * @param workOrder
     * @return
     */
    public RecommandCourseView changeChineseCourse(WorkOrder workOrder) {

        URI url = createPromoteExchangeCourse(
                workOrder.getStudentId(), workOrder.getRecommendSequence(), workOrder.getCourseId());
        try {
            logger.info("@changeChineseCourse#request发起换课请求,url[{}]", url);
            RecommandCourseView recommandCourseView = restTemplate.postForObject(url, null, RecommandCourseView.class);
            logger.info("@changeChineseCourse#result获取换课结果,url[{}],结果;[{}]", url, JacksonUtil.toJSon(recommandCourseView));
            return recommandCourseView;
        } catch (Exception ex) {
            logger.error("@changeChineseCourse#exception#[{}]!!!!!!!!!!!!!!向推荐课发起更换请求失败[{}]", url, ex);
            throw new BusinessException("更换中教推荐课程失败");
        }
    }


    /**
     * 终极梦想
     *
     * @param workOrder
     * @return
     */
    public RecommandCourseView changeForeignCourse(WorkOrder workOrder) {

        URI url = createUltimateExchangeCourse(workOrder.getStudentId(), workOrder.getRecommendSequence(), workOrder.getCourseId());
        try {
            logger.info("@changeForeignCourse#request发起换课请求,url[{}]", url);
            RecommandCourseView recommandCourseView = restTemplate.postForObject(url, HttpEntity.EMPTY, RecommandCourseView.class);
            logger.info("@changeForeignCourse#result获取换课结果,url[{}],结果;[{}]", url, JacksonUtil.toJSon(recommandCourseView));
            return recommandCourseView;
        } catch (Exception ex) {
            logger.error("@changeForeignCourse#exception#[{}]!!!!!!!!!!!!!!向推荐课发起更换请求失败[{}]", url, ex);
            throw new BusinessException("更换外教推荐课程失败");
        }
    }


    public String getThumbNailPath(RecommandCourseView courseView) {
        return String.format("%s%s", urlConf.getThumbnail_server(), courseView.getCover());
    }


    //课程完成后,通知推荐课程服务
    public void notifyCompleteCourse(WorkOrder workOrder) {
        String url = String.format("%s/counter/user_id/%s/lesson_id/%s", urlConf.getCourse_recommended_service(),
                                   workOrder.getStudentId(), workOrder.getCourseId());
        logger.info("上课结束,通知推荐课url::::[{}]", url);
        threadPoolManager.execute(new Thread(() -> {
            try {
                restTemplate.postForObject(url, null, Object.class);
            } catch (Exception ex) {
                logger.error("上课结束通知推荐课服务失败", ex);
            }

        }));
    }


    /**
     * 核心素养
     *
     * @param workOrder
     * @param predicate
     * @return
     */
    public RecommandCourseView getPromoteRecommend(WorkOrder workOrder, Predicate<WorkOrder> predicate) {
        if (predicate.test(workOrder)) {
            return getPromoteRecommend(workOrder);
        } else {
            return RecommendCourseType.recommendCN(workOrder.getRecommendSequence());
        }
    }

    /**
     * 核心素养
     *
     * @param workorder
     * @return
     */
    public RecommandCourseView getPromoteRecommend(WorkOrder workorder) {
        return getPromoteRecommend(workorder.getStudentId(), workorder.getRecommendSequence());
    }

    /**
     * 核心素养
     *
     * @param workorder
     * @return
     */
    public RecommandCourseView getPromoteRecommend(Long studentId, int index) {
        return restTemplate.getForObject(
                createPromoteRecommend(studentId, index),
                RecommandCourseView.class);
    }

    /**
     * 终极梦想
     *
     * @param workOrder
     * @param predicate
     * @return
     */
    public RecommandCourseView getUltimateRecommend(WorkOrder workOrder, Predicate<WorkOrder> predicate) {
        if (predicate.test(workOrder)) {
            return getUltimateRecommend(workOrder);
        } else {
            return RecommendCourseType.recommendFRN(workOrder.getRecommendSequence());
        }
    }

    /**
     * 终极梦想
     *
     * @param workorder
     * @return
     */
    public RecommandCourseView getUltimateRecommend(WorkOrder workorder) {
        return getUltimateRecommend(workorder.getStudentId(), workorder.getRecommendSequence());
    }

    /**
     * 终极梦想
     *
     * @param studentId
     * @param index
     * @return
     */
    public RecommandCourseView getUltimateRecommend(Long studentId, int index) {
        return restTemplate.getForObject(
                createUltimateRecommend(studentId, index),
                RecommandCourseView.class);
    }

    // 核心素养 123.56.13.168:8001/boxfish-wudaokou-recommend/recommend/core/promote/18826/9
    private URI createPromoteRecommend(Long studentId, int index) {
        URI uri = UriComponentsBuilder.fromUriString(urlConf.getCourse_wudaokou_recommend_service())
                                      .path(String.format("/promote/%s/%s", studentId, index))
                                      .build()
                                      .toUri();
        logger.info("recommendURL: [{}]", uri);
        return uri;
    }

    // 终极梦想 123.56.13.168:8001/boxfish-wudaokou-recommend/recommend/ultimate/18826/1
    private URI createUltimateRecommend(Long studentId, int index) {
        URI uri = UriComponentsBuilder.fromUriString(urlConf.getCourse_wudaokou_recommend_service())
                                      .path(String.format("/ultimate/%s/%s", studentId, index))
                                      .build()
                                      .toUri();
        logger.info("recommendURL: [{}]", uri);
        return uri;
    }


    // 终极梦想换课 123.56.13.168:8001/boxfish-wudaokou-recommend/recommend/ultimate/exchange/18826/L3NoYXJlL3N2bi9GdW5jdGlvbiDlhbPliIcvMzEyLuWmguS9leihqOi-vuaLheW_g-afkOS6i--8ny54bHN4
    private URI createUltimateExchangeCourse(Long studentId, int index, String lessonId) {
        URI uri = UriComponentsBuilder.fromUriString(urlConf.getCourse_wudaokou_recommend_service())
                                      .path(String.format("/ultimate/exchange/%s/%s/%s", studentId.toString(), index, lessonId))
                                      .build()
                                      .toUri();
        logger.info("recommendURL: [{}]", uri);
        return uri;
    }

    // 核心素养换课 123.56.13.168:8001/boxfish-wudaokou-recommend/recommend/core/exchange/promote/18826/1/L3NoYXJlL3N2bi9GdW5jdGlvbiDlhbPliIcvMzEyLuWmguS9leihqOi-vuaLheW_g-afkOS6i—8ny54bHN4
    private URI createPromoteExchangeCourse(Long studentId, int index, String lessonId) {
        URI uri = UriComponentsBuilder.fromUriString(urlConf.getCourse_wudaokou_recommend_service())
                                      .path(String.format("/exchange/promote/%s/%s/%s", studentId.toString(), index, lessonId))
                                      .build()
                                      .toUri();
        logger.info("recommendURL: [{}]", uri);
        return uri;
    }


    /**
     * 实时上课
     */
    public RecommandCourseView getInstantCourseView(Long studentId, int index, TutorType tutorType) {
        logger.debug("@getInstantCourseView->获取实时推荐课程");
        switch (tutorType) {
            case FRN:
                return this.getUltimateRecommend(studentId, index);
            case CN:
                return this.getPromoteRecommend(studentId, index);
            default:
                throw new BusinessException("不支持的参数类型");
        }
    }

    public RecommandCourseView getCourseViewDetail(String courseId) {
        String url = String.format("%s/%s", urlConf.getCourse_wudaokou_detail_service(), courseId);
        logger.debug("@getCourseViewDetail->获取课程详细信息courseId:[{}],url:[{}]", courseId, url);
        RecommandCourseView recommandCourseView = restTemplate.getForObject(url, RecommandCourseView.class);
        return recommandCourseView;
    }

    /***
     * 未匹配上教师的实时上课卡取消课程
     *
     * @param instantClassCard
     */
    public void cancelUnmatchedInstantCourse(InstantClassCard instantClassCard) {
        this.cancelUnmatchedInstantCourse(instantClassCard.getStudentId(), instantClassCard.getCourseId());
    }

    public void cancelUnmatchedInstantCourse(Long studentId, String courseId) {
        String url = String.format("%s/cancel/%s/%s", urlConf.getCourse_wudaokou_recommend_service(), studentId, courseId);
        try {
            logger.debug("@cancelUnmatchedInstantCourse#超时没有匹配上教师的实时上课卡课程取消,url[{}],学生[{}],课程[{}]", url, studentId, courseId);
            restTemplate.delete(url);
        } catch (Exception ex) {
            logger.error("@cancelUnmatchedInstantCourse#未匹配教师的推荐课取消失败,url[{}],学生[{}],课程[{}]", url, studentId, courseId);
        }

    }

}
