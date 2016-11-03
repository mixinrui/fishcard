package com.boxfishedu.workorder.web.controller.instantclass;

import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.servicex.instantclass.InstantClassServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.validator.RepeatedSubmissionException;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

/**
 * Created by hucl on 16/11/3.
 * 即时上课
 */
@CrossOrigin
@RestController
@RequestMapping("/service/student")
public class InstantClassController {

    @Autowired
    private InstantClassServiceX instantClassServiceX;

    @RequestMapping(value = "/instantclass", method = RequestMethod.POST)
    public JsonResultModel instantClass(@RequestBody InstantRequestParam instantRequestParam, Long userId) {
        instantClassServiceX.instantClass(instantRequestParam);
        return null;
    }
}
