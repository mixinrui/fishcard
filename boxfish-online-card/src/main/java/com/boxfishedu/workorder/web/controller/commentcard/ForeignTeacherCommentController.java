package com.boxfishedu.workorder.web.controller.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.common.exception.UnauthorizedException;
import com.boxfishedu.workorder.common.exception.UseUpException;
import com.boxfishedu.workorder.common.exception.ValidationException;
import com.boxfishedu.workorder.dao.jpa.CommentCardJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.common.bean.CommentCardStatus;
import com.boxfishedu.workorder.entity.mysql.CommentCardForm;
import com.boxfishedu.workorder.entity.mysql.FromTeacherStudentForm;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.commentcard.ForeignTeacherCommentCardService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * Created by ansel on 16/7/18.
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/comment_card")
public class ForeignTeacherCommentController {
    @Autowired
    ForeignTeacherCommentCardService foreignTeacherCommentCardService;

    @Autowired
    CommentCardJpaRepository commentCardJpaRepository;

    @Autowired
    ServeService serveService;

    @RequestMapping(value = "add", method = RequestMethod.POST)
    @Transactional
    public JsonResultModel addCommentCard(@RequestBody CommentCardForm commentCardForm, Long userId) throws Exception {
        CommentCard commentCard=CommentCard.getCommentCard(commentCardForm);
        if(serveService.findFirstAvailableForeignCommentService(userId).isPresent()){
            Service service= serveService.findFirstAvailableForeignCommentService(userId).get();
            if(service.getAmount() <= 0){
                throw new UseUpException("学生的外教点评次数已经用尽,请先购买!");
            }else {
                service.setAmount(service.getAmount() - 1);
                foreignTeacherCommentCardService.updateCommentAmount(service);
                commentCard.setStudentId(userId);
                commentCard.setService(service);
                commentCard.setOrderId(service.getOrderId());
                commentCard.setOrderCode(service.getOrderCode());
                return JsonResultModel.newJsonResultModel(foreignTeacherCommentCardService.foreignTeacherCommentCardAdd(commentCard));
            }
        }else {
                throw new UseUpException("学生的外教点评次数已经用尽,请先购买!");
        }
    }

    @RequestMapping(value = "update_student_question", method = RequestMethod.PUT)
    public JsonResultModel updateStudentQuestion(@RequestBody CommentCardForm commentCardForm, Long userId){
            CommentCard commentCard = foreignTeacherCommentCardService.foreignTeacherCommentDetailQuery(commentCardForm.getId(),userId);
            commentCard.setAskVoiceId(commentCardForm.getAskVoiceId());
            commentCard.setAskVoicePath(commentCardForm.getAskVoicePath());
            foreignTeacherCommentCardService.foreignTeacherCommentUpdateQuestion(commentCard);
            return new JsonResultModel();
    }

//    @RequestMapping(value = "update_student_question", method = RequestMethod.PUT)
//    public JsonResultModel updateStudentQuestion(@RequestBody CommentCardForm commentCardForm, Long userId){
//        if(!userId.equals(commentCardForm.getStudentId())){
//            throw new UnauthorizedException("用户认证失败!");
//        }else {
//            CommentCard commentCard = commentCardJpaRepository.findByStudentIdAndQuestionIdAndCourseId(
//                    commentCardForm.getStudentId(),commentCardForm.getQuestionId(),commentCardForm.getCourseId());
//            commentCard.setAskVoiceId(commentCardForm.getAskVoiceId());
//            commentCard.setAskVoicePath(commentCardForm.getAskVoicePath());
//            foreignTeacherCommentCardService.foreignTeacherCommentUpdateQuestion(commentCard);
//            return new JsonResultModel();
//        }
//    }

//    @RequestMapping(value = "update_teacher_answer", method = RequestMethod.PUT)
//    public JsonResultModel updateCommentCard(@RequestBody FromTeacherStudentForm fromTeacherStudentForm){
//           foreignTeacherCommentCardService.foreignTeacherCommentUpdateAnswer(fromTeacherStudentForm);
//           return new JsonResultModel();
//    }

    @RequestMapping(value = "query_all",method = RequestMethod.GET)
    public JsonResultModel queryCommentList(Pageable pageable, Long userId){
        return JsonResultModel.newJsonResultModel(foreignTeacherCommentCardService.foreignTeacherCommentQuery(pageable,userId));
    }

    @RequestMapping(value = "query_one",method = RequestMethod.GET)
    public JsonResultModel queryDetailComment(Long id, Long userId){
        CommentCard commentCard = foreignTeacherCommentCardService.foreignTeacherCommentDetailQuery(id,userId);
        if(commentCard == null){
            throw new ValidationException("所查找的点评不存在!");
        }else {
            return JsonResultModel.newJsonResultModel(commentCard);
        }
    }

    @RequestMapping(value = "update_notice_status", method = RequestMethod.PUT)
    public JsonResultModel updateStatus(Long id, Long userId){
        CommentCard commentCard = commentCardJpaRepository.findByIdAndStudentId(id,userId);
        if (commentCard == null){
            throw new ValidationException("所修改的点评不存在!");
        }else {
            foreignTeacherCommentCardService.foreignTeacherCommentUpdateStatusRead(commentCard);
            return JsonResultModel.newJsonResultModel();
        }
    }

//    @RequestMapping(value = "query_no_answer")
//    public JsonResultModel queryUnAnswer(){
//        foreignTeacherCommentCardService.foreignTeacherCommentUnAnswer();
//        return JsonResultModel.newJsonResultModel();
//    }

    @RequestMapping(value = "/isAvailable", method = RequestMethod.GET)
    public JsonResultModel haveAvailableForeignCommentService(long userId) {
        return JsonResultModel.newJsonResultModel(
                Collections.singletonMap("available", serveService.haveAvailableForeignCommentService(userId)));
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public JsonResultModel getAvailableForeignCommentServiceCount(long userId) {
        return JsonResultModel.newJsonResultModel(serveService.getAvailableForeignCommentServiceCount(userId));
    }
}
