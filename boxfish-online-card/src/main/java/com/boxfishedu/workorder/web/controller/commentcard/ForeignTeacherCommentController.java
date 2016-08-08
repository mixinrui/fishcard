package com.boxfishedu.workorder.web.controller.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.common.exception.UseUpException;
import com.boxfishedu.workorder.common.exception.ValidationException;
import com.boxfishedu.workorder.dao.jpa.CommentCardJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.entity.mysql.CommentCardForm;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.commentcard.ForeignTeacherCommentCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

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
                commentCard.setAskVoiceId(commentCardForm.getAskVoiceId());
                commentCard.setAskVoicePath(commentCardForm.getAskVoicePath());
                commentCard.setVoiceTime(commentCardForm.getVoiceTime());
                return JsonResultModel.newJsonResultModel(foreignTeacherCommentCardService.foreignTeacherCommentCardAdd(commentCard));
            }
        }else {
                throw new UseUpException("学生的外教点评次数已经用尽,请先购买!");
        }
    }

    @RequestMapping(value = "query_all",method = RequestMethod.GET)
    public JsonResultModel queryCommentList(Pageable pageable, Long userId){
        return JsonResultModel.newJsonResultModel(foreignTeacherCommentCardService.foreignTeacherCommentQuery(pageable,userId));
    }

    @RequestMapping(value = "query_one",method = RequestMethod.GET)
    public JsonResultModel queryDetailComment(Long id,Long userId){
        CommentCard commentCard = foreignTeacherCommentCardService.foreignTeacherCommentDetailQuery(id,userId);
        if(commentCard == null){
            throw new ValidationException("所查找的点评不存在!");
        }else {
            return JsonResultModel.newJsonResultModel(commentCard);
        }
    }

    @RequestMapping(value = "query_no_answer")
    public JsonResultModel queryUnAnswer(){
        foreignTeacherCommentCardService.foreignTeacherCommentUnAnswer();
        return JsonResultModel.newJsonResultModel();
    }

    @RequestMapping(value = "/isAvailable", method = RequestMethod.GET)
    public JsonResultModel haveAvailableForeignCommentService(long userId) {
        return JsonResultModel.newJsonResultModel(
                Collections.singletonMap("available", serveService.haveAvailableForeignCommentService(userId)));
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public JsonResultModel getAvailableForeignCommentServiceCount(long userId) {
        return JsonResultModel.newJsonResultModel(serveService.getForeignCommentServiceCount(userId));
    }

    @RequestMapping(value = "test_teacher_comment", method = RequestMethod.POST)
    public JsonResultModel testTeacherComment(@RequestBody CommentCardForm commentCardForm, Long userId){
        return JsonResultModel.newJsonResultModel(foreignTeacherCommentCardService.testTeacherComment(commentCardForm,userId));
    }

    @RequestMapping(value = "test_query_all", method = RequestMethod.GET)
    public JsonResultModel testQueryAll(Pageable pageable){
        return JsonResultModel.newJsonResultModel(foreignTeacherCommentCardService.testQueryAll(pageable));
    }
}
