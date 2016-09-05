package com.boxfishedu.card.comment.manage.service.sdk;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.comment.manage.config.CommentCardManageUrl;
import com.boxfishedu.card.comment.manage.entity.form.TeacherForm;
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

    @Autowired
    Logger logger = LoggerFactory.getLogger(CommentCardManageSDK.class);

    @Autowired
    private CommentCardManageUrl commentCardManageUrl;

    public void teacherDecrementCount(Long teacherId) {
        restTemplate.exchange(
                createTeacherDecrementCountURI(teacherId),
                HttpMethod.POST,
                HttpEntity.EMPTY,
                Object.class);
    }

    private URI createTeacherDecrementCountURI(Long teacherId) {
        return UriComponentsBuilder
                .fromUriString("")
                .path("/" + teacherId)
                .build()
                .toUri();
    }

    public void freezeTeacherId(Long teacherId){
        logger.info("Accessing freezeTeacherId in CommentCardManageSDK......");
        restTemplate.exchange(
                createFreezeTeacherURI(teacherId),
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Object.class);
    }

    private URI createFreezeTeacherURI(Long teacherId){
        return UriComponentsBuilder
                .fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("/" + teacherId)
                .build()
                .toUri();
    }

    public void unfreezeTeacherId(Long teacherId){
        logger.info("Accessing unfreezeTeacherId in CommentCardManageSDK......");
        restTemplate.exchange(
                createUnfreezeTeacherURI(teacherId),
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Object.class
        );
    }

    private URI createUnfreezeTeacherURI(Long teacherId){
        return UriComponentsBuilder
                .fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("/" + teacherId)
                .build()
                .toUri();
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

    private URI createGetUncommentTeacherListURI(Pageable pageable, TeacherForm teacherForm){
        MultiValueMap paramMap = new LinkedMultiValueMap();
        return UriComponentsBuilder
                .fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("/")
                .queryParams(paramMap)
                .build()
                .toUri();
    }
}
