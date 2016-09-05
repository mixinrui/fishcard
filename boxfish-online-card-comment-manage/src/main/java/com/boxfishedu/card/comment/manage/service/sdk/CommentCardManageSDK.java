package com.boxfishedu.card.comment.manage.service.sdk;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.comment.manage.config.CommentCardManageUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Created by ansel on 16/9/2.
 */
@Service
public class CommentCardManageSDK {

    @Autowired
    private RestTemplate restTemplate;

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
}
