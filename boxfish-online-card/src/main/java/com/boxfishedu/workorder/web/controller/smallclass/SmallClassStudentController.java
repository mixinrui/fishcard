package com.boxfishedu.workorder.web.controller.smallclass;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by hucl on 17/1/9.
 */
@CrossOrigin
@RestController
@RequestMapping("/service/student/smallclass")
public class SmallClassStudentController {

    @RequestMapping(value = "/{smallclass_id}/status", method = RequestMethod.POST)
    public JsonResultModel status(@RequestBody Map<String, String> statusReport) {
        return JsonResultModel.newJsonResultModel("success");
    }

}
