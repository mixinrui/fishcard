package com.boxfishedu.card.comment.manage.web.controller;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.comment.manage.entity.form.CommentCardForm;
import com.boxfishedu.card.comment.manage.service.CommentCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by LuoLiBing on 16/9/2.
 */
@RestController
@RequestMapping("/comment/manage")
public class CommentCardController {

    @Autowired
    private CommentCardService commentCardService;

    @RequestMapping(value = "/page")
    public Object commentCardPage(CommentCardForm commentCardForm, Pageable pageable) {
        return JsonResultModel.newJsonResultModel(
                commentCardService.findCommentCardByOptions(commentCardForm, pageable));
    }

}
