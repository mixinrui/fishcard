package com.boxfishedu.card.comment.manage.web.controller;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.comment.manage.entity.form.TeacherForm;
import com.boxfishedu.card.comment.manage.service.ForeignTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * Created by ansel on 16/9/2.
 */
@RestController
@RequestMapping("/comment_teacher/manage")
public class ForeignTeacherController {
    @Autowired
    ForeignTeacherService foreignTeacherService;

    @RequestMapping(value = "/freeze_teacher_id/{teacherId}", method = RequestMethod.PUT)
    public Object freezeTeacherId(@PathVariable Long teacherId){
        foreignTeacherService.freezeTeacherId(teacherId);
        return JsonResultModel.newJsonResultModel();
    }

    @RequestMapping(value = "/unfreeze_teacher_id/{teacherId}", method = RequestMethod.PUT)
    public Object unfreezeTeacherId(@PathVariable  Long teacherId){
        foreignTeacherService.unfreezeTeacherId(teacherId);
        return JsonResultModel.newJsonResultModel();
    }

    @RequestMapping(value = "/get/teacher_operations/{teacherId}", method = RequestMethod.GET)
    public Object getTeacherOperations(@PathVariable  Long teacherId){
        return foreignTeacherService.getTeacherOperations(teacherId);
    }

    @RequestMapping(value = "/get/teacher_times/{teacherId}", method = RequestMethod.GET)
    public Object getTeacherTimes(@PathVariable  Long teacherId){
        return foreignTeacherService.getTeacherTimes(teacherId);
    }

    @RequestMapping(value = "/page/comment", method = RequestMethod.GET)
    public Object getTeacherList(Pageable pageable, TeacherForm teacherForm){
        return foreignTeacherService.getTeacherList(pageable,teacherForm);
    }

    @RequestMapping(value = "/page/uncomment", method = RequestMethod.GET)
    public Object getUncommentTeacherList(Pageable pageable, TeacherForm teacherForm){
        return foreignTeacherService.getUncommentTeacherList(pageable,teacherForm);
    }
}
