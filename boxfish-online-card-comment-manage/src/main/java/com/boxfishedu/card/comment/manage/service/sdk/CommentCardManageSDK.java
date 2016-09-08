package com.boxfishedu.card.comment.manage.service.sdk;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.comment.manage.config.CommentCardManageUrl;
import com.boxfishedu.card.comment.manage.entity.form.TeacherForm;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

/**
 * Created by ansel on 16/9/2.
 */
@Service
public class CommentCardManageSDK {

    @Autowired
    private RestTemplate restTemplate;

    private final static Logger logger = LoggerFactory.getLogger(CommentCardManageSDK.class);

    @Autowired
    private CommentCardManageUrl commentCardManageUrl;

    public void teacherDecrementCount(Long teacherId) {
        restTemplate.exchange(
                createTeacherDecrementCountURI(teacherId),
                HttpMethod.POST,
                HttpEntity.EMPTY,
                Object.class);
    }

    /**
     * 冻结老师
     * @param teacherId
     */
    public void freezeTeacherId(Long teacherId){
        logger.info("Accessing freezeTeacherId in CommentCardManageSDK......");
        restTemplate.exchange(
                createFreezeTeacherURI(teacherId),
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Object.class);
    }

    /**
     * 获取内部老师
     * @param paramMap
     * @return
     */
    public JsonResultModel getInnerTeacherId(Map paramMap){
        return restTemplate.postForObject(getInnerTeacherURI(), paramMap,JsonResultModel.class);
    }

    /**
     * token验证
     * @param accessToken
     * @return
     */
    public boolean checkToken(String accessToken) {
        JsonResultModel jsonResultModel = restTemplate.getForObject(
                createTokenCheckURI(accessToken), JsonResultModel.class);
        return (StringUtils.equals("ok", jsonResultModel.getData().toString()));
    }

    /**
     * 老师解冻
     * @param teacherId
     */
    public void unfreezeTeacherId(Long teacherId){
        logger.info("Accessing unfreezeTeacherId in CommentCardManageSDK......");
        restTemplate.exchange(
                createUnfreezeTeacherURI(teacherId),
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Object.class
        );
    }

    public JsonResultModel getNoCommentPage(Pageable pageable, TeacherForm teacherForm) {
        JsonResultModel jsonResultModel = restTemplate.getForObject(createNoCommentPageURI(
                pageable, teacherForm), JsonResultModel.class);
        return null;
//        return new PageImpl<>(JsonResultModuleUtils.getListFromPageResult(jsonResultModel, CommentTeacherInfo.class,
//                ((clazz, beanMap) -> ObjectUtils.convertObject(beanMap, CommentTeacherInfo.class)),
//                pageable, JsonResultModuleUtils.getTotalElements(jsonResultModel));
    }

    public JsonResultModel getTeacherOperations(Long teacherId){
        logger.info("Accessing getTeacherOperations in CommentCardManageSDK......");
        return JsonResultModel.newJsonResultModel(restTemplate.exchange(createGetTeacherOperationURI(teacherId),HttpMethod.GET,
                HttpEntity.EMPTY,
                Object.class));
    }

    private URI createGetTeacherOperationURI(Long teacherId){
        return UriComponentsBuilder
                .fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("/" + teacherId)
                .build()
                .toUri();
    }

    public JsonResultModel getTeacherTimes(Long teacherId){
        logger.info("Accessing getTeacherTimes in CommentCardManageSDK......");
        return JsonResultModel.newJsonResultModel(restTemplate.exchange(createGetTeacherTimesURI(teacherId),HttpMethod.GET,
                HttpEntity.EMPTY,
                Object.class));
    }

    private URI createGetTeacherTimesURI(Long teacherId){
        return UriComponentsBuilder
                .fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("/" + teacherId)
                .build()
                .toUri();
    }

    public JsonResultModel getUncommentTeacherList(Pageable pageable, TeacherForm teacherForm){
        logger.info("Accessing getUncommentTeacherList in CommentCardManageSDK......");
        return JsonResultModel.newJsonResultModel(restTemplate.exchange(createGetUncommentTeacherListURI(pageable,teacherForm),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                Object.class));
    }


    /************************************* 构建URI ********************************/

    private URI createFreezeTeacherURI(Long teacherId){
        return UriComponentsBuilder
                .fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("/" + teacherId)
                .build()
                .toUri();
    }


    private URI createUnfreezeTeacherURI(Long teacherId){
        return UriComponentsBuilder
                .fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("/" + teacherId)
                .build()
                .toUri();
    }


    private URI createTeacherDecrementCountURI(Long teacherId) {
        return UriComponentsBuilder
                .fromUriString("")
                .path("/" + teacherId)
                .build()
                .toUri();
    }

    private URI createGetUncommentTeacherListURI(Pageable pageable, TeacherForm teacherForm){
        MultiValueMap paramMap = new LinkedMultiValueMap();
        return UriComponentsBuilder
                .fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("/")
                .queryParams(paramMap)
                .build()
                .toUri();
    }

    private URI getInnerTeacherURI(){
        logger.info("Accessing getInnerTeacher in CommentCardSDK......");
        return UriComponentsBuilder.fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("/f_teacher_review/get_inner_f_review_teacher")
                .queryParam("")
                .build()
                .toUri();
    }

    private URI createTokenCheckURI(String accessToken) {
        return UriComponentsBuilder.fromUriString(commentCardManageUrl.getAuthenticationUrl())
                .path("/backend/login/checktoken/" + accessToken + "/out")
                .build()
                .toUri();
    }

    private URI createNoCommentPageURI(Pageable pageable, TeacherForm teacherForm) {
        MultiValueMap<String, String> paramMap = teacherForm.createValueMap();
        paramMap.add("page", pageable.getPageNumber() + "");
        paramMap.add("size", pageable.getPageSize() + "");
        return UriComponentsBuilder.fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("f_teacher_review/get_review_todaycount")
                .queryParams(paramMap)
                .build()
                .toUri();
    }
}
