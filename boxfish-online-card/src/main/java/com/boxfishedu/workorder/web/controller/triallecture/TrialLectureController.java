package com.boxfishedu.workorder.web.controller.triallecture;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.servicex.tiallecture.TrialLectureServiceX;
import com.boxfishedu.workorder.web.param.TrialLectureModifyParam;
import com.boxfishedu.workorder.web.param.TrialLectureParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hucl on 16/5/23.
 */
@CrossOrigin
@RestController
@RequestMapping("/service")

public class TrialLectureController {
    @Autowired
    private TrialLectureServiceX trialLectureServiceX;

    @RequestMapping(value = "/trial", method = RequestMethod.POST)
    public JsonResultModel buildFishCard(@RequestBody TrialLectureParam trialLectureParam) {
        trialLectureServiceX.buildFishCard(trialLectureParam);
        JsonResultModel jsonResultModel = JsonResultModel.newJsonResultModel(null);
        return jsonResultModel;
    }

    @RequestMapping(value = "/trial", method = RequestMethod.PUT)
    public JsonResultModel modifyFishCard(@RequestBody TrialLectureModifyParam trialLectureModifyParam) {
        trialLectureServiceX.modifyFishCard(trialLectureModifyParam);
        return JsonResultModel.newJsonResultModel(null);
    }

    @RequestMapping(value = "/trial", method = RequestMethod.DELETE)
    public JsonResultModel deleteFishCard(@RequestBody TrialLectureParam trialLectureParam) {
        trialLectureServiceX.deleteFishCard(trialLectureParam);
        return JsonResultModel.newJsonResultModel(null);
    }
}
