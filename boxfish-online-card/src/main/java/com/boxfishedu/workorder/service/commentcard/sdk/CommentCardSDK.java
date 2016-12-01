package com.boxfishedu.workorder.service.commentcard.sdk;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.common.config.CommentCardUrlConf;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.util.RestTemplateForCommentCard;
import com.boxfishedu.workorder.entity.mysql.PushToStudentAndTeacher;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by ansel on 16/7/26.
 */
@Service
public class CommentCardSDK {

    private Logger logger = LoggerFactory.getLogger(CommentCardSDK.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier("comment_card")
    private RestTemplate templateForCommentCard;

    @Autowired
    private CommentCardUrlConf commentCardUrlConf;

    @Autowired
    private UrlConf urlConf;

    public JsonResultModel setTeacherAbsence(Long teacherId,Long studentId, Long id){
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("teacherId", Objects.toString(teacherId));
        paramMap.put("studentId", Objects.toString(studentId));
        paramMap.put("fishCardId",Objects.toString(id));
        return restTemplate.postForObject(createTeacherAbsenceURI(), paramMap,JsonResultModel.class);
    }

    public String getUserPicture(String access_token){
        return restTemplate.getForObject(createGetPictureURI(access_token),String.class);
    }

    public JsonResultModel pushToStudentAndTeacher(Long userId, String title, String type){
        PushToStudentAndTeacher pushToStudentAndTeacher = new PushToStudentAndTeacher();
        pushToStudentAndTeacher.setUser_id(userId);
        pushToStudentAndTeacher.setPush_title(title);
        Map<String,String> map = new HashMap<>();
        map.put("type",type);
        pushToStudentAndTeacher.setData(map);
        return restTemplate.postForObject(createPushURI(), Arrays.asList(pushToStudentAndTeacher),JsonResultModel.class);
    }

    public Map commentTypeAndDifficulty(String courseId){
        return templateForCommentCard.getForObject(getCourseTypeAndDifficulty(courseId),Map.class);
    }

    public Map initiateTypeAndDifficulty(String courseId){
        return templateForCommentCard.getForObject(initiateCourseTypeAndDifficulty(courseId),Map.class);
    }

    public JsonResultModel getInnerTeacherId(Map paramMap){
        return restTemplate.postForObject(getInnerTeacherURI(), paramMap,JsonResultModel.class);
    }

    public Object addScore2Student(Long studentId,String accessToken, Integer point){
        return restTemplate.postForObject(getAddScoreURI(studentId,accessToken,point),null,Object.class);
    }

    private URI createTeacherAbsenceURI(){
        logger.info("Accessing createTeacherAbsenceURI in CommentCardSDK......");
        return UriComponentsBuilder.fromUriString(commentCardUrlConf.getTeacherAbsenceUrl())
                .path("/f_teacher_review/set_truant")
                .queryParam("")
                .build()
                .toUri();
    }

    private URI createGetPictureURI(String access_token){
        logger.info("Accessing createGetPictureURI in CommentCardSDK......");
        return UriComponentsBuilder.fromUriString(commentCardUrlConf.getGetPictureUrl())
                .path("/user/me")
                .queryParam("access_token",access_token)
                .build()
                .toUri();
    }

    private URI createPushURI(){
        logger.info("Accessing createPushURI in CommentCardSDK......");
        return UriComponentsBuilder.fromUriString(commentCardUrlConf.getPushInfoIrl())
                .path("/teaching/callback/push")
                .queryParam("")
                .build()
                .toUri();
    }

    private URI getInnerTeacherURI(){
        logger.info("Accessing getInnerTeacher in CommentCardSDK......");
        return UriComponentsBuilder.fromUriString(commentCardUrlConf.getInnerTeacherUrl())
                .path("/f_teacher_review/get_inner_f_review_teacher")
                .queryParam("")
                .build()
                .toUri();
    }

    private URI getCourseTypeAndDifficulty(String courseId){
        logger.info("Accessing getCourseTypeAndDifficulty in CommentCardSDK......");
        String url = "/boxfish-wudaokou-course/course/info/" + courseId;
        return UriComponentsBuilder.fromUriString(urlConf.getCourse_type_and_difficulty())
                .path(url)
                .queryParam("")
                .build()
                .toUri();
    }

    private URI initiateCourseTypeAndDifficulty(String courseId){
        logger.info("Accessing initiateCourseTypeAndDifficulty in CommentCardSDK......");
        String url = "/boxfish-wudaokou-course/course/info/" + courseId;
        return UriComponentsBuilder.fromUriString(urlConf.getCourse_type_and_difficulty())
                .path(url)
                .queryParam("")
                .build()
                .toUri();
    }

    private URI getAddScoreURI(Long studentId,String accessToken, Integer point){
        logger.info("Accessing getAddScoreURI in CommentCardSDK......");
        return UriComponentsBuilder.fromUriString(commentCardUrlConf.getScoreUrl())
                .path("/statistic/user/score/" + studentId)
                .queryParam("access_token",accessToken)
                .queryParam("lesson_id","外教点评")
                .queryParam("channel","online")
                .queryParam("score",point)
                .build()
                .toUri();
    }
}
