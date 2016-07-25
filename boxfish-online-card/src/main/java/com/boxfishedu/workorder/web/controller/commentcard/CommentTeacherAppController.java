package com.boxfishedu.workorder.web.controller.commentcard;

import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.servicex.CommonServeServiceX;
import com.boxfishedu.workorder.servicex.commentcard.CommentTeacherAppServiceX;
import com.boxfishedu.workorder.web.param.CommentCardSubmitParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hucl on 16/7/20.
 * 外教点评教师App相关操作
 */
@CrossOrigin
@RestController
@RequestMapping("/comment/foreign")
public class CommentTeacherAppController {

    @Autowired
    private CommentTeacherAppServiceX commentTeacherAppServiceX;

    @Autowired
    private CommonServeServiceX commonServeServiceX;

    @RequestMapping(value = "/teacher/{teacher_id}/list", method = RequestMethod.GET)
    public JsonResultModel list(@PathVariable("teacher_id") long teacherId,Long userId, Pageable pageable){
        commonServeServiceX.checkToken(teacherId,userId);
        Page<CommentCard> commentCardPage= commentTeacherAppServiceX.findByTeacherIdOrderByAssignTeacherTimeDesc(teacherId,pageable);
        return JsonResultModel.newJsonResultModel(commentCardPage);
    }

    @RequestMapping(value = "/card/{card_id}/detail", method = RequestMethod.GET)
    public JsonResultModel listOne(@PathVariable("card_id") Long cardId){
        CommentCard commentCard = commentTeacherAppServiceX.findById(cardId);
        return JsonResultModel.newJsonResultModel(commentCard);
    }

    @RequestMapping(value = "/card", method = RequestMethod.POST)
    public JsonResultModel submitComment(@RequestBody CommentCardSubmitParam commentCardSubmitParam,Long userId){
        commonServeServiceX.checkToken(commentCardSubmitParam.getTeacherId(),userId);
        commentTeacherAppServiceX.submitComment(commentCardSubmitParam);
        return JsonResultModel.newJsonResultModel(null);
    }

    @RequestMapping(value = "/update_status", method = RequestMethod.PUT)
    public JsonResultModel markReadFlag(){
        return null;
    }
}
