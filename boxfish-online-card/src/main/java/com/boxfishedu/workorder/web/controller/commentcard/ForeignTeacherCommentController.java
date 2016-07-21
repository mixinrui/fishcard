package com.boxfishedu.workorder.web.controller.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.common.exception.UseUpException;
import com.boxfishedu.workorder.dao.jpa.CommentCardJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.common.bean.CommentCardStatus;
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
    public JsonResultModel addCommentCard(@RequestBody CommentCard commentCardForm) throws Exception {
        CommentCard commentCard=new CommentCard();
        BeanUtils.copyProperties(commentCardForm,commentCard);
        if(serveService.findFirstAvailableForeignCommentService(commentCard.getStudentId()).isPresent()){
            Service service= serveService.findFirstAvailableForeignCommentService(commentCard.getStudentId()).get();
            if(service.getAmount() <= 0){
                throw new UseUpException();
            }else {
                service.setAmount(service.getAmount() - 1);
                foreignTeacherCommentCardService.updateCommentAmount(service);
                commentCard.setService(service);
                return foreignTeacherCommentCardService.foreignTeacherCommentCardAdd(commentCard);
            }
        }else {
                throw new UseUpException();
        }
    }

    @RequestMapping(value = "update_answer", method = RequestMethod.PUT)
    public JsonResultModel updateCommentCard(@RequestBody CommentCard commentCardForm){
        CommentCard commentCard = commentCardJpaRepository.findByStudentIdAndQuestionIdAndCourseId(
                commentCardForm.getStudentId(),commentCardForm.getQuestionId(),commentCardForm.getCourseId());
        commentCardForm.setId(commentCard.getId());
        commentCardForm.setCreateTime(commentCard.getCreateTime());
        commentCardForm.setStudentAskTime(commentCard.getStudentAskTime());
        commentCardForm.setAskVoiceId(commentCard.getAskVoiceId());
        commentCardForm.setAskVoicePath(commentCard.getAskVoicePath());
        commentCardForm.setOrderId(commentCard.getOrderId());
        commentCardForm.setOrderCode(commentCard.getOrderCode());
        commentCardForm.setStatus(CommentCardStatus.UNREAD.getCode());
        Service service= serveService.findFirstAvailableForeignCommentService(commentCard.getStudentId()).get();
        service.setAmount(service.getAmount() - 1);
        foreignTeacherCommentCardService.updateCommentAmount(service);
        commentCardForm.setService(service);
        return foreignTeacherCommentCardService.foreignTeacherCommentCardUpdate(commentCardForm);
    }

    @RequestMapping(value = "query_all/{studentId}",method = RequestMethod.GET)
    public JsonResultModel queryCommentList(Pageable pageable,@PathVariable Long studentId){
        return JsonResultModel.newJsonResultModel(foreignTeacherCommentCardService.foreignTeacherCommentQuery(pageable,studentId));
    }

    @RequestMapping(value = "query_one/{id}",method = RequestMethod.GET)
    public JsonResultModel queryDetailComment(@PathVariable Long id){
        return JsonResultModel.newJsonResultModel(foreignTeacherCommentCardService.foreignTeacherCommentDetailQuery(id));
    }

    @RequestMapping(value = "update_status", method = RequestMethod.PUT)
    public JsonResultModel updateStatus(@RequestBody CommentCard commentCard){
        CommentCard commentCardTemp = commentCardJpaRepository.findByStudentIdAndQuestionIdAndCourseId(
                commentCard.getStudentId(),commentCard.getQuestionId(),commentCard.getCourseId()
        );
        commentCardTemp.setStatus(CommentCardStatus.OVERTIME.getCode());
        return JsonResultModel.newJsonResultModel(foreignTeacherCommentCardService.foreignTeacherCommentCardUpdate(commentCardTemp));
    }

    @RequestMapping(value = "query_no_answer")
    public JsonResultModel queryUnAnswer(){
        return JsonResultModel.newJsonResultModel(foreignTeacherCommentCardService.foreignTeacherCommentUnAnswer());
    }

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
