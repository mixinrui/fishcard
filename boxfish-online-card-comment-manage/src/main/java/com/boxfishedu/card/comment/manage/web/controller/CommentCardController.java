package com.boxfishedu.card.comment.manage.web.controller;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.comment.manage.entity.form.CommentCardForm;
import com.boxfishedu.card.comment.manage.service.CommentCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

/**
 * Created by LuoLiBing on 16/9/2.
 */
@RestController
@RequestMapping("/comment/manage")
public class CommentCardController {

    @Autowired
    private CommentCardService commentCardService;

    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public Object commentCardPage(CommentCardForm commentCardForm, Pageable pageable) {
        return JsonResultModel.newJsonResultModel(
                commentCardService.findCommentCardByOptions(commentCardForm, pageable));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Object commentCard(@PathVariable Long id) {
        return JsonResultModel.newJsonResultModel(commentCardService.findCommentCardById(id));
    }

    /**
     * 换老师逻辑
     * @param id
     * @param teacherId
     * @return
     */
    @RequestMapping(value = "/{id}/change/teacher/{teacherId}", method = RequestMethod.PUT)
    public Object changeTeacher(@PathVariable Long id, @PathVariable Long teacherId) {
        commentCardService.changeTeacher(id, teacherId);
        return JsonResultModel.newJsonResultModel();
    }

    @RequestMapping(value = "/batch/change/teacher/{teacherId}", method = RequestMethod.PUT)
    public Object changeTeacherBatch(@PathVariable Long teacherId, Long[] ids) {
        commentCardService.changeTeacherBatch(ids, teacherId);
        return JsonResultModel.newJsonResultModel();
    }

    /**
     * 获取未点评统计数组
     * @return
     */
    @RequestMapping(value = "/notanswer/count", method = RequestMethod.GET)
    public Object notAnswerCount() {
        return JsonResultModel.newJsonResultModel(
                Collections.singletonMap("count", commentCardService.findNoAnswerCountsByAskTime()));
    }

}
