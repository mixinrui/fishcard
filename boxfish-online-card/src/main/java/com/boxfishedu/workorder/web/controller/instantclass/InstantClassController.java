package com.boxfishedu.workorder.web.controller.instantclass;

import com.boxfishedu.workorder.common.bean.instanclass.TeacherInstantClassStatus;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.servicex.instantclass.InstantClassServiceX;
import com.boxfishedu.workorder.servicex.instantclass.TeacherInstantClassServiceX;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.servicex.instantclass.grabordervalidator.GrabInstatntClassKeyGenerator;
import com.boxfishedu.workorder.servicex.studentrelated.validator.RepeatedSubmissionException;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.boxfishedu.workorder.web.param.TeacherInstantRequestParam;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.result.InstantClassResult;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

/**
 * Created by hucl on 16/11/3.
 * 即时上课
 */
@CrossOrigin
@RestController
@RequestMapping("/")
public class InstantClassController {

    @Autowired
    private InstantClassServiceX instantClassServiceX;

    @Autowired
    private TeacherInstantClassServiceX teacherInstantClassServiceX;

    @Autowired
    private @Qualifier("teachingServiceRedisTemplate")
    StringRedisTemplate redisTemplate;

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/service/student/instantclass", method = RequestMethod.POST)
    public JsonResultModel instantClass(@RequestBody InstantRequestParam instantRequestParam, Long userId) {
        return instantClassServiceX.instantClass(instantRequestParam);
    }

    @RequestMapping(value = "/service/teacher/instantclass", method = RequestMethod.POST)
    public JsonResultModel teacherInstantClass(@RequestBody TeacherInstantRequestParam teacherInstantRequestParam, Long userId) {
        try {
            JsonResultModel jsonResultModel= teacherInstantClassServiceX.teacherInstantClass(teacherInstantRequestParam);
            return jsonResultModel;
        }
        catch (Exception ex){
            logger.error("@teacherInstantClass抢课失败",ex);
            return JsonResultModel.newJsonResultModel(InstantClassResult
                    .newInstantClassResult(TeacherInstantClassStatus.FAIL_TO_MATCH));
        }
        finally {
            String key= GrabInstatntClassKeyGenerator.generateKey(teacherInstantRequestParam);
            //无论是否成功都删除当前用户的资源
            redisTemplate.delete(key);
        }
    }

    @RequestMapping(value = "/service/student/instant/timerange", method = RequestMethod.GET)
    public JsonResultModel timeRange(){
        String timeDesc=instantClassServiceX.timeRange();
        if(StringUtils.isEmpty(timeDesc)){
            return JsonResultModel.newJsonResultModel(null);
        }
        return JsonResultModel.newJsonResultModel(instantClassServiceX.timeRange()+" 开启");
    }
}