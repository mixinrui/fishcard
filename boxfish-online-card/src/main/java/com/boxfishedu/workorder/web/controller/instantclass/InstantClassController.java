package com.boxfishedu.workorder.web.controller.instantclass;

import com.boxfishedu.workorder.servicex.studentrelated.validator.RepeatedSubmissionException;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hucl on 16/11/3.
 * 即时上课
 */
@CrossOrigin
@RestController
@RequestMapping("/service/student")
public class InstantClassController {

    @RequestMapping(value = "/instantclass", method = RequestMethod.POST)
    public JsonResultModel instantClass(@RequestBody InstantRequestParam instantRequestParam, Long userId) {

        return null;
    }
}
