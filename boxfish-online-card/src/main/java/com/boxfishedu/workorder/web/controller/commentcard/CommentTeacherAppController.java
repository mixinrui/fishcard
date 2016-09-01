package com.boxfishedu.workorder.web.controller.commentcard;

import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.service.commentcard.CommentCardTeacherAppService;
import com.boxfishedu.workorder.service.commentcard.ForeignTeacherCommentCardService;
import com.boxfishedu.workorder.servicex.CommonServeServiceX;
import com.boxfishedu.workorder.servicex.commentcard.CommentTeacherAppServiceX;
import com.boxfishedu.workorder.web.param.CommentCardSubmitParam;
import com.boxfishedu.workorder.web.param.commentcard.TeacherReadMsgParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private CommentCardTeacherAppService commentCardTeacherAppService;

    @Autowired
    private ForeignTeacherCommentCardService foreignTeacherCommentCardService;

    @RequestMapping(value = "/teacher/answer_list", method = RequestMethod.GET)
    public JsonResultModel teacherAnswerList(Pageable pageable,Long userId){
        return JsonResultModel.newJsonResultModel(commentCardTeacherAppService.queryTeacherAnswerList(pageable,userId));
    }

    @RequestMapping(value = "/teacher/unanswer_list", method = RequestMethod.GET)
    public JsonResultModel teacherUnAnswerList(Pageable pageable,Long userId){
        return JsonResultModel.newJsonResultModel(commentCardTeacherAppService.queryTeacherUnanswerList(pageable,userId));
    }
    @RequestMapping(value = "/card/{card_id}/detail", method = RequestMethod.GET)
    public JsonResultModel listOne(@PathVariable("card_id") Long cardId){
        CommentCard commentCard = commentTeacherAppServiceX.findById(cardId);
        return JsonResultModel.newJsonResultModel(commentCard);
    }

    @RequestMapping(value = "/card", method = RequestMethod.POST)
    public JsonResultModel submitComment(@RequestBody CommentCardSubmitParam commentCardSubmitParam,Long userId,String access_token){
        commonServeServiceX.checkToken(commentCardSubmitParam.getTeacherId(),userId);
        commentCardSubmitParam.setTeacherPicturePath(foreignTeacherCommentCardService.getUserPicture(access_token));
        //commentCardSubmitParam.setTeacherId(userId);//测试时使用,正式去掉
        commentTeacherAppServiceX.submitComment(commentCardSubmitParam);
        return JsonResultModel.newJsonResultModel(null);
    }

    @RequestMapping(value = "/teacher/read_message", method = RequestMethod.PUT)
    public JsonResultModel markReadFlag(@RequestBody TeacherReadMsgParam teacherReadMsgParam,Long userId){
        commonServeServiceX.checkToken(teacherReadMsgParam.getTeacherId(),userId);
        commentTeacherAppServiceX.markTeacherRead(teacherReadMsgParam);
        return JsonResultModel.newJsonResultModel(null);
    }

    @RequestMapping(value = "/check_teacher/{cardId}", method = RequestMethod.GET)
    public JsonResultModel checkTeacher(@PathVariable Long cardId,Long userId){
        JsonResultModel jsonResultModel = new JsonResultModel();
        if (commentTeacherAppServiceX.checkTeacher(cardId,userId) != null){
            jsonResultModel.setReturnMsg("允许该外教进行点评。");
            jsonResultModel.setData("OK");
            jsonResultModel.setReturnCode(HttpStatus.SC_OK);
        }else {
            jsonResultModel.setReturnMsg("禁止该外教进行点评!");
            jsonResultModel.setData("Unauthorized");
            jsonResultModel.setReturnCode(HttpStatus.SC_UNAUTHORIZED);
        }
        return jsonResultModel;
    }

    @RequestMapping(value = "count_teacher_unread", method = RequestMethod.GET)
    public com.boxfishedu.beans.view.JsonResultModel countTeacherUnreadCommentCards(Long userId){
        return foreignTeacherCommentCardService.countTeacherUnreadCommentCards(userId);
    }

}
