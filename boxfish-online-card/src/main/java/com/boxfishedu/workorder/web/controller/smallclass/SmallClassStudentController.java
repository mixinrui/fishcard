package com.boxfishedu.workorder.web.controller.smallclass;

import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.servicex.smallclass.SmallClassStudentStatusServiceX;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

/**
 * Created by hucl on 17/1/9.
 */
@CrossOrigin
@RestController
@RequestMapping("/service/student/smallclass")
public class SmallClassStudentController {

    @Autowired
    private SmallClassStudentStatusServiceX smallClassStudentStatusServiceX;

    @RequestMapping(value = "/{smallclass_id}/status", method = RequestMethod.POST)
    public JsonResultModel status(
            @PathVariable("smallclass_id") Long smallClassId
            , @RequestBody Map<String, String> statusReport, Long userId) {

        smallClassStudentStatusServiceX.status(smallClassId, userId, statusReport);

        return JsonResultModel.newJsonResultModel("success");
    }

    @RequestMapping(value = "/publicTips", method = RequestMethod.GET)
    public JsonResultModel publicTips() {
        return JsonResultModel.newJsonResultModel("公开课预计于2月15号开放,欢迎使用");
    }

}
