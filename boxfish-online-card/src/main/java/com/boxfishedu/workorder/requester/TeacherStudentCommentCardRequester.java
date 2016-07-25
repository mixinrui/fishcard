package com.boxfishedu.workorder.requester;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.online.order.entity.OrderForm;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BoxfishException;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.param.ForeignTeacherSetCommentParam;
import com.boxfishedu.workorder.service.commentcard.CommentCardTeacherAppService;
import com.boxfishedu.workorder.service.commentcard.ForeignTeacherCommentCardService;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.web.param.FetchTeacherParam;
import com.boxfishedu.workorder.web.param.Student2TeacherCommentParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.teacher.PlannerAssignView;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

/**
 * Created by hucl on 16/6/17.
 */
@Component
public class TeacherStudentCommentCardRequester {
    @Autowired
    private UrlConf urlConf;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    private CommentCardTeacherAppService commentCardTeacherAppService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    public void sendStudentComment2Teacher(Student2TeacherCommentParam student2TeacherCommentParam) {
        CommentCard commentCard=commentCardTeacherAppService.findById(student2TeacherCommentParam.getCommentCardId());
        if(null==commentCard){
            throw new BusinessException("不存在对应的点评卡:["+student2TeacherCommentParam.getCommentCardId()+"]");
        }
        ForeignTeacherSetCommentParam foreignTeacherSetCommentParam=ForeignTeacherSetCommentParam.paramAdapter(student2TeacherCommentParam,commentCard);
        String url = String.format("%s/f_teacher_review/set_review", urlConf.getTeacher_service());
        logger.debug("<<<<<<<<<<<<<@[sendStudentComment2Teaxcher]向师生运营发起[[[[设置鱼卡的点评信息]]]],url[{}]", url);
        threadPoolManager.execute(new Thread(() -> {
            restTemplate.postForObject(url, foreignTeacherSetCommentParam, JsonResultModel.class);
        }));
    }


}
