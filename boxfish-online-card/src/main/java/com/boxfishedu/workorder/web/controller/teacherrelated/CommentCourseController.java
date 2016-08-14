package com.boxfishedu.workorder.web.controller.teacherrelated;

import com.boxfishedu.workorder.servicex.studentrelated.CommentCourseInfoServiceX;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * Created by hucl on 16/8/14.
 */
@CrossOrigin
@RestController
@RequestMapping("/service/teacher")
public class CommentCourseController {

    @Autowired
    private CommentCourseInfoServiceX commentCourseInfoServiceX;

    @RequestMapping(value = "/course_info/{fishcard_id}")
    public JsonResultModel getFishCardCourseInfo(@PathVariable("fishcard_id") Long fishcardId) {
        return commentCourseInfoServiceX.getFishCardCourseInfo(fishcardId);
    }
}
