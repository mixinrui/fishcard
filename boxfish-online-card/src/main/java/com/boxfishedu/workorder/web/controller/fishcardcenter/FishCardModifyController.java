package com.boxfishedu.workorder.web.controller.fishcardcenter;

import com.boxfishedu.workorder.servicex.fishcardcenter.FishCardModifyServiceX;
import com.boxfishedu.workorder.web.param.CourseChangeParam;
import com.boxfishedu.workorder.web.param.TeacherChangeParam;
import com.boxfishedu.workorder.web.param.fishcardcenetr.FishCardDeleteParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hucl on 16/5/10.
 * 供内部接口更换课程
 */
@CrossOrigin
@RestController
@RequestMapping("/backend/fishcard")
public class FishCardModifyController {
    @Autowired
    private FishCardModifyServiceX fishCardModifyServiceX;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/teacher", method = RequestMethod.PUT)
    public JsonResultModel changeTeacher(@RequestBody TeacherChangeParam teacherChangeParam) {
        return fishCardModifyServiceX.changeTeacher(teacherChangeParam);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public JsonResultModel deleteFishCard(@RequestBody FishCardDeleteParam fishCardDeleteParam){
        if(!CollectionUtils.isEmpty(fishCardDeleteParam.getFishCardIds())) {
            fishCardModifyServiceX.deleteFishCardsByIds(fishCardDeleteParam);
        }
        if(!CollectionUtils.isEmpty(fishCardDeleteParam.getStudentIds())){
            fishCardModifyServiceX.deleteFishCardsByStudentIds(fishCardDeleteParam);
        }
        return JsonResultModel.newJsonResultModel("success");
    }
}
