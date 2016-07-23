package com.boxfishedu.workorder.web.controller.commentcard;

import com.boxfishedu.workorder.web.param.Student2TeacherCommentParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hucl on 16/7/23.
 */
@CrossOrigin
@RestController
@RequestMapping("/comment/foreign")

public class StudentComment2Teacher {
    @RequestMapping(value = "/student/comment", method = RequestMethod.POST)
    public JsonResultModel studentComment2Teacher(@RequestBody Student2TeacherCommentParam student2TeacherCommentParam) {
        student2TeacherCommentParam.getCommentCardId();
        return null;
    }
}
