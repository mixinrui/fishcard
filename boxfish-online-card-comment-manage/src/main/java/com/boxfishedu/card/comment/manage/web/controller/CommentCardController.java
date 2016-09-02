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

    @RequestMapping(value = "/{id}/change/teacher/{teacherId}", method = RequestMethod.PUT)
    public Object changeTeacher(@PathVariable Long id, @PathVariable Long teacherId) {
        commentCardService.changeTeacher(id, teacherId);
        return JsonResultModel.newJsonResultModel();
    }
}
