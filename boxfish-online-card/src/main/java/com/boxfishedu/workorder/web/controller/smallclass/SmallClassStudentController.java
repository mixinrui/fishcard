package com.boxfishedu.workorder.web.controller.smallclass;

import com.boxfishedu.workorder.dao.mongo.ConfigBeanMorphiaRepository;
import com.boxfishedu.workorder.servicex.smallclass.SmallClassServiceX;
import com.boxfishedu.workorder.servicex.smallclass.SmallClassStudentStatusServiceX;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by hucl on 17/1/9.
 */
@CrossOrigin
@RestController
@RequestMapping("/service/student/smallclass")
public class SmallClassStudentController {

    @Autowired
    private SmallClassStudentStatusServiceX smallClassStudentStatusServiceX;

    @Autowired
    private ConfigBeanMorphiaRepository configBeanMorphiaRepository;

    @Autowired
    private SmallClassServiceX smallClassServiceX;

    @RequestMapping(value = "/{smallclass_id}/status", method = RequestMethod.POST)
    public JsonResultModel status(
            @PathVariable("smallclass_id") Long smallClassId
            , @RequestBody Map<String, String> statusReport, Long userId) {

        smallClassStudentStatusServiceX.status(smallClassId, userId, statusReport);

        return JsonResultModel.newJsonResultModel("success");
    }

    @RequestMapping(value = "/publicTips", method = RequestMethod.GET)
    public JsonResultModel publicTips() {
        String tips = configBeanMorphiaRepository.getPublicWarning();
        //成为BOXFiSH学员 即可每天免费上课
        return JsonResultModel.newJsonResultModel(tips);
    }

    @RequestMapping(value = "/publicCoverTips", method = RequestMethod.GET)
    public JsonResultModel publicCoverTips() {
        String tips = configBeanMorphiaRepository.getPublicWarning();
        //成为BOXFiSH学员 即可每天免费上课
        return JsonResultModel.newJsonResultModel(configBeanMorphiaRepository.getPublicCoverTips());
    }

    @RequestMapping(value = "/{smallclass_id}/currentPageIndex", method = RequestMethod.GET)
    public JsonResultModel currentPageIndex(@PathVariable("smallclass_id") Long smallClassId) {
        return JsonResultModel.newJsonResultModel(smallClassServiceX.currentPageIndex(smallClassId));
    }
}
