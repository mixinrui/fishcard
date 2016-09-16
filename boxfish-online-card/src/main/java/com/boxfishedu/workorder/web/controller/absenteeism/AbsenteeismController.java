package com.boxfishedu.workorder.web.controller.absenteeism;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.service.absenteeism.AbsenteeismService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by ansel on 16/9/14.
 */
@RestController
@RequestMapping(value = "/student_absence")
public class AbsenteeismController{

    @Autowired
    AbsenteeismService absenteeismService;

    //@RequestMapping(value = "/deduct_score/{studentId}" , method = RequestMethod.PUT)
    public JsonResultModel deductScore(@PathVariable Long studentId){
        return  absenteeismService.absenteeismDeductScore(studentId);
    }
}
