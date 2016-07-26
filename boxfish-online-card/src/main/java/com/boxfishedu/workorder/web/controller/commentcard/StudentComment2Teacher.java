package com.boxfishedu.workorder.web.controller.commentcard;

import com.boxfishedu.workorder.servicex.CommonServeServiceX;
import com.boxfishedu.workorder.servicex.commentcard.StudentComment2TeacherServiceX;
import com.boxfishedu.workorder.web.param.Student2TeacherCommentParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hucl on 16/7/23.
 */
@CrossOrigin
@RestController
@RequestMapping("/comment/foreign")

public class StudentComment2Teacher {

    @Autowired
    private StudentComment2TeacherServiceX studentComment2TeacherServiceX;

    @Autowired
    private CommonServeServiceX commonServeServiceX;

    @RequestMapping(value = "/student", method = RequestMethod.POST)
    public JsonResultModel studentCommentForTeacher(@RequestBody Student2TeacherCommentParam student2TeacherCommentParam,Long userId) {
        commonServeServiceX.checkToken(student2TeacherCommentParam.getStudentId(),userId);
        studentComment2TeacherServiceX.studentComment2Teacher(student2TeacherCommentParam);
        return JsonResultModel.newJsonResultModel(null);
    }
}
