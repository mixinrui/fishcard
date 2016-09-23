package com.boxfishedu.workorder.requester;

import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.RecommandedCourseService;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.boxfishedu.workorder.web.view.course.RecommandCourseViews;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

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

    public RecommandCourseView getRecommandCourse(WorkOrder workOrder, Integer index) {
        String url = String.format("%s/core/online/%s/%s", urlConf.getCourse_recommended_service(), workOrder.getStudentId(), index);
        logger.debug("@<-<-<-<-<-<-向推荐课发起获取推荐课的请求,url:[{}]", url);
        RecommandCourseView recommandCourseView = null;
        try {
            recommandCourseView = restTemplate.postForObject(url, null, RecommandCourseView.class);
            if (null == recommandCourseView) {
                throw new BusinessException();
            }
            logger.info("@->->->->->->->获取推荐课成功,返回值:{}", JacksonUtil.toJSon(recommandCourseView));
        } catch (Exception ex) {
            logger.error("!!!!!!!!!!!!!!向推荐课发起请求失败[{}]", ex.getMessage(), ex);
            throw new BusinessException("获取推荐课程失败");
        }
        return recommandCourseView;
    }

    public RecommandCourseView getRecomendCourse(WorkOrder workOrder, TutorType tutorType) {
        try {
            String type;
            switch (tutorType) {
                case CN: type = "chinese"; break;
                case FRN: type = "foreigner"; break;
                default: throw new BusinessException("未知类型的课程");
            }
            RecommandCourseView recommandCourseView = restTemplate.postForObject(
                    createRecommendUri(workOrder.getStudentId(), type),
                    HttpEntity.EMPTY,
                    RecommandCourseView.class);
            logger.info("@->->->->->->->获取推荐课成功,返回值:{}", JacksonUtil.toJSon(recommandCourseView));
            return recommandCourseView;
        } catch (Exception ex) {
            logger.error("!!!!!!!!!!!!!!向推荐课发起请求失败[{}]", ex.getMessage(), ex);
            throw new BusinessException("获取推荐课程失败");
        }
    }

    public RecommandCourseView changeCourse(WorkOrder workOrder) {
        String tutorType=workOrder.getService().getTutorType();
        logger.debug("@RecommandCourseRequester#changeCourse,参数tutorType[{}]",tutorType);
        if(Objects.equals(tutorType, TutorType.CN.name())) {
            return changeChineseCourse(workOrder);
        }
        else if(Objects.equals(tutorType,TutorType.FRN.name())){
            return changeForeignCourse(workOrder);
        }
        else {
            return changeOverAllCourse(workOrder);
        }
    }

    //目前为中教的换课
    public RecommandCourseView changeOverAllCourse(WorkOrder workOrder) {
        String url = String.format("%s/core/exchange/online/%s/%s/%s", urlConf.getCourse_recommended_service(),
                workOrder.getStudentId(), workOrder.getSeqNum(), workOrder.getCourseId());
        try {
            logger.info("@changeCourse#request发起换课请求,url[{}]", url);
            RecommandCourseView recommandCourseView = restTemplate.postForObject(url, null, RecommandCourseView.class);
            logger.info("@changeCourse#result获取换课结果,url[{}],结果;[{}]", url, JacksonUtil.toJSon(recommandCourseView));
            return recommandCourseView;
        } catch (Exception ex) {
            logger.error("@changeCourse#exception#[{}]!!!!!!!!!!!!!!向推荐课发起更换请求失败[{}]", url, ex);
            throw new BusinessException("更换推荐课程失败");
        }
    }


    //目前为中教的换课
    public RecommandCourseView changeChineseCourse(WorkOrder workOrder) {
        String url = String.format("%s/core/exchange/chinese/%s/%s", urlConf.getCourse_recommended_service(),
                workOrder.getStudentId(), workOrder.getCourseId());
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



    //目前为中教的换课
    public RecommandCourseView changeForeignCourse(WorkOrder workOrder) {
        String url = String.format("%s/core/exchange/foreigner/%s/%s", urlConf.getCourse_recommended_service(),
                workOrder.getStudentId(), workOrder.getCourseId());
        try {
            logger.info("@changeForeignCourse#request发起换课请求,url[{}]", url);
            RecommandCourseView recommandCourseView = restTemplate.postForObject(url, null, RecommandCourseView.class);
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


    public RecommandCourseView getRecommandCourse(WorkOrder workOrder) {
        return getRecommandCourse(workOrder, recommandedCourseService.getCourseIndex(workOrder));
    }

    public List<RecommandCourseView> getBatchRecommandCourse(Long studentId) {
        try {
            RecommandCourseViews overAllRecommandViews = restTemplate.getForObject(
                    createOverAllRecommend(studentId), RecommandCourseViews.class);
            return overAllRecommandViews.getSingle();
        } catch (Exception e) {
            throw new BusinessException("调用课程推荐失败");
        }
    }

    public RecommandCourseView getDreamRecommandCourse(WorkOrder workOrder) {
        return restTemplate.postForObject(
                createDreamRecommend(workOrder.getStudentId(), recommandedCourseService.getCourseIndex(workOrder)),
                HttpEntity.EMPTY,
                RecommandCourseView.class);
    }


    public List<RecommandCourseView> getBatch8DreamRecommandCourse(Long studentId) {
        RecommandCourseViews recommandCourseViews = restTemplate.getForObject(
                create8BatchDreamRecommend(studentId), RecommandCourseViews.class);
        return recommandCourseViews.getSingle();
    }


    //课程完成后,通知推荐课程服务
    public void notifyCompleteCourse(WorkOrder workOrder) {
        String url = String.format("%s/core/counter/user_id/%s/lesson_id/%s", urlConf.getCourse_recommended_service(),
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

    private URI createRecommendUri(Long studentId, String tutorType) {
        return UriComponentsBuilder
                .fromUriString(urlConf.getCourse_recommended_service())
                .path("/core/online/" + tutorType + "/" + studentId)
                .build()
                .toUri();
    }


    /*************** 兼容老版本 ***************/
    public RecommandCourseView getForeignRecomandCourse(WorkOrder workOrder) {
        try {
            RecommandCourseView recommandCourseView = restTemplate.postForObject(
                    createForeignRecommendUri(workOrder.getStudentId()),
                    HttpEntity.EMPTY,
                    RecommandCourseView.class);
            logger.info("@->->->->->->->获取推荐课成功,返回值:{}", JacksonUtil.toJSon(recommandCourseView));
            return recommandCourseView;
        } catch (Exception ex) {
            logger.error("!!!!!!!!!!!!!!向推荐课发起请求失败[{}]", ex.getMessage(), ex);
            throw new BusinessException("获取推荐课程失败");
        }
    }


    private URI createForeignRecommendUri(Long studentId) {
        return UriComponentsBuilder
                .fromUriString(urlConf.getCourse_recommended_service())
                .path("/core/online/foreigner/" + studentId)
                .build()
                .toUri();
    }


    private URI createOverAllRecommend(Long studentId) {
        return UriComponentsBuilder.fromUriString(urlConf.getCourse_recommended_service())
                .path("/core/online/" + studentId)
                .build()
                .toUri();

    }

    private URI createDreamRecommend(Long studentId, int index) {
        return UriComponentsBuilder.fromUriString(urlConf.getCourse_recommended_service())
                .path(String.format("/online/ultimate/%s/%s", studentId.toString(), index))
                .build()
                .toUri();
    }

    private URI create8BatchDreamRecommend(Long studentId) {
        return UriComponentsBuilder.fromUriString(urlConf.getCourse_recommended_service())
                .path(String.format("/online/ultimate/%s", studentId.toString()))
                .build()
                .toUri();
    }
}
