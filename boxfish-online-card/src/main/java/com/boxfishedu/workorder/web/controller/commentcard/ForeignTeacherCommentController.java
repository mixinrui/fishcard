package com.boxfishedu.workorder.web.controller.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.dao.jpa.CommentCardJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.entity.mysql.CommentCardStatus;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.service.commentcard.ForeignTeacherCommentCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @RequestMapping(value = "add", method = RequestMethod.POST)
    public JsonResultModel addCommnetCard(@RequestBody CommentCard commentCardForm){
        Service service = new Service();
        service.setId(2L);
        service.setStudentId(1298782L);
        service.setStudentName("");
        service.setOrderId(180L);
        service.setOriginalAmount(8);
        service.setAmount(8);
        service.setSkuId(1L);
        service.setSkuName("中教在线授课");
        service.setValidityDay(365);
        service.setRoleId(1);
        service.setComboCycle(1);
        service.setCountInMonth(8);
        service.setOrderCode("16070115301593963077");
        commentCardForm.setService(service);
        return foreignTeacherCommentCardService.foreignTeacherCommentCardAdd(commentCardForm);
    }

    @RequestMapping(value = "updateAnswer", method = RequestMethod.PUT)
    public JsonResultModel updateCommnetCard(@RequestBody CommentCard commentCardForm){
        CommentCard commentCard = commentCardJpaRepository.findByStudentIdAndQuestionIdAndCourseId(
                commentCardForm.getStudentId(),commentCardForm.getQuestionId(),commentCardForm.getCourseId());
        commentCardForm.setId(commentCard.getId());
        commentCardForm.setCreateTime(commentCard.getCreateTime());
        commentCardForm.setStudentAskTime(commentCard.getStudentAskTime());
        commentCardForm.setAskVoiceId(commentCard.getAskVoiceId());
        commentCardForm.setAskVoicePath(commentCard.getAskVoicePath());
        commentCardForm.setOrderId(commentCard.getOrderId());
        commentCardForm.setOrderCode(commentCard.getOrderCode());
        commentCardForm.setStatus(CommentCardStatus.getCode("未读"));
        Service service = new Service();
        service.setId(2L);
        service.setStudentId(1298782L);
        service.setStudentName("");
        service.setOrderId(180L);
        service.setOriginalAmount(8);
        service.setAmount(8);
        service.setSkuId(1L);
        service.setSkuName("中教在线授课");
        service.setValidityDay(365);
        service.setRoleId(1);
        service.setComboCycle(1);
        service.setCountInMonth(8);
        service.setOrderCode("16070115301593963077");
        commentCardForm.setService(service);
        return foreignTeacherCommentCardService.foreignTeacherCommentCardUpdate(commentCardForm);
    }

    @RequestMapping(value = "query/{studentId}",method = RequestMethod.GET)
    public List<CommentCard> queryCommentList(@PathVariable Long studentId){
        return foreignTeacherCommentCardService.foreignTeacherCommentQuery(studentId);
    }

    @RequestMapping(value = "updateStatus", method = RequestMethod.PUT)
    public JsonResultModel updateStatus(@RequestBody CommentCard commentCard){
        CommentCard commentCardTemp = commentCardJpaRepository.findByStudentIdAndQuestionIdAndCourseId(
                commentCard.getStudentId(),commentCard.getQuestionId(),commentCard.getCourseId()
        );
        commentCardTemp.setStatus(CommentCardStatus.getCode("已读"));
        Service service = new Service();
        service.setId(2L);
        service.setStudentId(1298782L);
        service.setStudentName("");
        service.setOrderId(180L);
        service.setOriginalAmount(8);
        service.setAmount(8);
        service.setSkuId(1L);
        service.setSkuName("中教在线授课");
        service.setValidityDay(365);
        service.setRoleId(1);
        service.setComboCycle(1);
        service.setCountInMonth(8);
        service.setOrderCode("16070115301593963077");
        commentCardTemp.setService(service);
        return foreignTeacherCommentCardService.foreignTeacherCommentCardUpdate(commentCardTemp);
    }
}
