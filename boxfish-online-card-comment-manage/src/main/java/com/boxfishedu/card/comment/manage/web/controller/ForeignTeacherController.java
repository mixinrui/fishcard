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
@RequestMapping("/comment/manage")
public class ForeignTeacherController {
    @Autowired
    ForeignTeacherService foreignTeacherService;

    /**
     * 冻结老师
     * @param teacherId
     * @return
     */
    @RequestMapping(value = "/freeze_teacher_id/{teacherId}", method = RequestMethod.PUT)
    public Object freezeTeacherId(@PathVariable Long teacherId){
        foreignTeacherService.freezeTeacherId(teacherId);
        return JsonResultModel.newJsonResultModel();
    }

    /**
     * 解冻老师
     * @param teacherId
     * @return
     */
    @RequestMapping(value = "/unfreeze_teacher_id/{teacherId}", method = RequestMethod.PUT)
    public Object unfreezeTeacherId(@PathVariable  Long teacherId){
        foreignTeacherService.unfreezeTeacherId(teacherId);
        return JsonResultModel.newJsonResultModel();
    }

    @RequestMapping(value = "/get/teacher_operations/{teacherId}", method = RequestMethod.GET)
    public Object getTeacherOperations(@PathVariable  Long teacherId){
        return foreignTeacherService.getTeacherOperations(teacherId);
    }

    /**
     * 已收到点评老师列表
     * @param pageable
     * @param teacherForm
     * @return
     */
    @RequestMapping(value = "/teacher/page/comment", method = RequestMethod.GET)
    public Object getTeacherList(Pageable pageable, TeacherForm teacherForm){
        return JsonResultModel.newJsonResultModel(
                foreignTeacherService.commentTeacherPage(pageable,teacherForm));
    }

    @RequestMapping(value = "/teacher/page/uncomment", method = RequestMethod.GET)
    public Object getUncommentTeacherList(Pageable pageable, TeacherForm teacherForm){
        return JsonResultModel.newJsonResultModel(
                foreignTeacherService.uncommentTeacherPage(pageable,teacherForm));
    }

    @RequestMapping(value = "/teacher/{teacherId}/teacher_times/logs", method = RequestMethod.GET)
    public Object teacherTimesLogs(Pageable pageable, @PathVariable Long teacherId) {
        return JsonResultModel.newJsonResultModel(
                foreignTeacherService.commentCountSetLogPage(pageable, teacherId));
    }
}
