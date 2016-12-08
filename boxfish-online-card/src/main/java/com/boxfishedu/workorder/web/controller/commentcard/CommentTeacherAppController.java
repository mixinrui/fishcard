package com.boxfishedu.workorder.web.controller.commentcard;

import com.boxfishedu.workorder.common.bean.CommentCardTeacherAppTip;
import com.boxfishedu.workorder.dao.jpa.CommentCardStarJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.entity.mysql.CommentCardStar;
import com.boxfishedu.workorder.entity.mysql.CommentCardStarForm;
import com.boxfishedu.workorder.service.commentcard.CommentCardStarTeacherAppService;
import com.boxfishedu.workorder.service.commentcard.CommentCardTeacherAppService;
import com.boxfishedu.workorder.service.commentcard.ForeignTeacherCommentCardService;
import com.boxfishedu.workorder.servicex.CommonServeServiceX;
import com.boxfishedu.workorder.servicex.commentcard.CommentTeacherAppServiceX;
import com.boxfishedu.workorder.web.param.CommentCardSubmitParam;
import com.boxfishedu.workorder.web.param.commentcard.TeacherReadMsgParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * Created by hucl on 16/7/20.
 * 外教点评教师App相关操作
 */
@CrossOrigin
@RestController
@RequestMapping("/comment/foreign")
public class CommentTeacherAppController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CommentTeacherAppServiceX commentTeacherAppServiceX;

    @Autowired
    private CommonServeServiceX commonServeServiceX;

    @Autowired
    private CommentCardTeacherAppService commentCardTeacherAppService;

    @Autowired
    private ForeignTeacherCommentCardService foreignTeacherCommentCardService;

    @Autowired
    private CommentCardStarTeacherAppService commentCardStarTeacherAppService;

    @Autowired
    private CommentCardStarJpaRepository commentCardStarJpaRepository;

    @RequestMapping(value = "/teacher/answer_list", method = RequestMethod.GET)
    public JsonResultModel teacherAnswerList(Pageable pageable,Long userId){
        logger.info("###teacherAnswerList###");
        return JsonResultModel.newJsonResultModel(commentCardTeacherAppService.queryTeacherAnswerList(pageable,userId));
    }

    @RequestMapping(value = "/teacher/unanswer_list", method = RequestMethod.GET)
    public JsonResultModel teacherUnAnswerList(Pageable pageable,Long userId){
        logger.info("###teacherUnAnswerList###");
        return JsonResultModel.newJsonResultModel(commentCardTeacherAppService.queryTeacherUnanswerList(pageable,userId));
    }
    @RequestMapping(value = "/card/{card_id}/detail", method = RequestMethod.GET)
    public JsonResultModel listOne(@PathVariable("card_id") Long cardId){
        logger.info("###listOne###");
        CommentCard commentCard = commentTeacherAppServiceX.findById(cardId);
        return JsonResultModel.newJsonResultModel(commentCard);
    }

    @RequestMapping(value = "/card", method = RequestMethod.POST)
    public JsonResultModel submitComment(@RequestBody CommentCardSubmitParam commentCardSubmitParam,Long userId,String access_token){
        logger.info("###submitComment###");
        commonServeServiceX.checkToken(commentCardSubmitParam.getTeacherId(),userId);
        commentCardSubmitParam.setTeacherPicturePath(foreignTeacherCommentCardService.getUserPicture(access_token));
        //commentCardSubmitParam.setTeacherId(userId);//测试时使用,正式去掉
//        CommentCard commentCard = commentTeacherAppServiceX.checkCommentCard(commentCardSubmitParam.getCommentCardId(),userId);
//        if (commentCard == null || !Objects.equals(commentCard.getStatus(),CommentCardStatus.ASSIGNED_TEACHER.getCode())){
//            JsonResultModel jsonResultModel = new JsonResultModel();
//            jsonResultModel.setReturnMsg("Sorry! You do not have enough authorization.");
//            jsonResultModel.setData("Unauthorized");
//            jsonResultModel.setReturnCode(CommentCardTeacherAppTip.COMMENT_CARD_TIME_OUT.getCode());
//            return jsonResultModel;
//        }else{
//            commentTeacherAppServiceX.submitComment(commentCardSubmitParam);
//            return JsonResultModel.newJsonResultModel(null);
//        }
        commentTeacherAppServiceX.submitComment(commentCardSubmitParam);
        return JsonResultModel.newJsonResultModel();
    }

    @RequestMapping(value = "/teacher/read_message", method = RequestMethod.PUT)
    public JsonResultModel markReadFlag(@RequestBody TeacherReadMsgParam teacherReadMsgParam,Long userId){
        logger.info("###markReadFlag###");
        commonServeServiceX.checkToken(teacherReadMsgParam.getTeacherId(),userId);
        commentTeacherAppServiceX.markTeacherRead(teacherReadMsgParam);
        return JsonResultModel.newJsonResultModel(null);
    }

    @RequestMapping(value = "/check_teacher/{cardId}", method = RequestMethod.GET)
    public JsonResultModel checkTeacher(@PathVariable Long cardId,Long userId){
        JsonResultModel jsonResultModel = new JsonResultModel();
        CommentCard commentCard = commentTeacherAppServiceX.checkTeacher(cardId,userId);
        if (commentCard != null){
            logger.info("###checkTeacher1###" + commentCard.getStatus());
            switch (commentCard.getStatus()){
                case 100:
                case 200:
                case 300:
                    jsonResultModel.setReturnMsg("允许该外教进行点评。");
                    jsonResultModel.setData("OK");
                    jsonResultModel.setReturnCode(CommentCardTeacherAppTip.COMMENT_CARD_ADOPT.getCode());
                    break;
                case 400:
                case 600:
                    jsonResultModel.setReturnMsg("You have evaluated the answer; No need to do it again.");
                    jsonResultModel.setData("Unauthorized");
                    jsonResultModel.setReturnCode(CommentCardTeacherAppTip.COMMENTED.getCode());
                    break;
                case 500:
                    jsonResultModel.setReturnMsg("Warning! You did not assess the answer in 24 hours. If you should fail again, you would never have the chance to assess answers any more.");
                    jsonResultModel.setData("Unauthorized");
                    jsonResultModel.setReturnCode(CommentCardTeacherAppTip.COMMENT_CARD_TIME_OUT.getCode());
                    break;
            }
        }else {
            logger.info("###checkTeacher2### 点评卡不存在");
            jsonResultModel.setReturnMsg("Something’s wrong. There’s no such evaluation order actually…");
            jsonResultModel.setData("Unauthorized");
            jsonResultModel.setReturnCode(CommentCardTeacherAppTip.COMMENT_CARD_NON_EXISTENT.getCode());
        }
        return jsonResultModel;
    }

    @RequestMapping(value = "/count_teacher_unread", method = RequestMethod.GET)
    public com.boxfishedu.beans.view.JsonResultModel countTeacherUnreadCommentCards(Long userId){
        logger.info("###countTeacherUnreadCommentCards###");
        return foreignTeacherCommentCardService.countTeacherUnreadCommentCards(userId);
    }

    @RequestMapping(value = "/comment_student_star/{userId}", method = RequestMethod.POST)
    public JsonResultModel commentStar2Student(@RequestBody CommentCardStarForm commentCardStarForm,@PathVariable  Long userId,String access_token){
        JsonResultModel jsonResultModel;
        CommentCardStar commentCardStarOld = commentCardStarJpaRepository.findByCommentCardId(commentCardStarForm.getCommentCardId());
        if (Objects.equals(commentCardStarOld,null)){
            CommentCardStar commentCardStar = commentCardStarTeacherAppService.saveTeacher2StudentStar(commentCardStarForm,userId,access_token);
           jsonResultModel = commentCardStarTeacherAppService.showStarInfo(commentCardStar);
        }else {
            if (Objects.equals(commentCardStarOld.getCommentCardId(),commentCardStarForm.getCommentCardId())){
                jsonResultModel = new JsonResultModel();
                jsonResultModel.setData("已经评星成功，不需要再次评星!");
                jsonResultModel.setReturnCode(400);
                jsonResultModel.setReturnMsg("Commented!");
            }else {
                CommentCardStar commentCardStar = commentCardStarTeacherAppService.saveTeacher2StudentStar(commentCardStarForm,userId,access_token);
                jsonResultModel = commentCardStarTeacherAppService.showStarInfo(commentCardStar);
            }
        }

        return jsonResultModel;
    }
}
