package com.boxfishedu.workorder.web.controller.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.common.exception.ValidationException;
import com.boxfishedu.workorder.dao.jpa.CommentCardJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.entity.mysql.CommentCardForm;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.commentcard.ForeignTeacherCommentCardService;
import com.boxfishedu.workorder.service.commentcard.sdk.CommentCardSDK;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    CommentCardSDK commentCardSDK;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public JsonResultModel addCommentCard(@RequestBody CommentCardForm commentCardForm, Long userId, String access_token) throws Exception {
        return JsonResultModel.newJsonResultModel(foreignTeacherCommentCardService.foreignTeacherCommentCardAdd(commentCardForm,userId,access_token));
    }

    @RequestMapping(value = "/query_all",method = RequestMethod.GET)
    public JsonResultModel queryCommentList(Pageable pageable, Long userId){
        return JsonResultModel.newJsonResultModel(foreignTeacherCommentCardService.foreignTeacherCommentQuery(pageable,userId));
    }

    @RequestMapping(value = "/query_one",method = RequestMethod.GET)
    public JsonResultModel queryDetailComment(Long id,Long userId){
        CommentCard commentCard = foreignTeacherCommentCardService.foreignTeacherCommentDetailQuery(id,userId);
        if(commentCard == null){
            throw new ValidationException("所查找的点评不存在!");
        }else {
            return JsonResultModel.newJsonResultModel(commentCard);
        }
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public JsonResultModel getAvailableForeignCommentServiceCount(long userId) {
        return JsonResultModel.newJsonResultModel(serveService.getForeignCommentServiceCount(userId));
    }

    @RequestMapping(value = "/count_student_unread", method = RequestMethod.GET)
    public JsonResultModel countStudentUnreadCommentCards(Long userId){
        return foreignTeacherCommentCardService.countStudentUnreadCommentCards(userId);
    }

    //强制换老师
    @RequestMapping(value = "/force_to_change_teacher",method = RequestMethod.PUT)
    public JsonResultModel forceToChangeTeacher(@RequestBody CommentCardForm commentCardForm){
        foreignTeacherCommentCardService.forceToChangeTeacher(commentCardForm.getFromTeacherId(),commentCardForm.getToTeacherId());
        return JsonResultModel.newJsonResultModel();
    }

    //@RequestMapping(value = "/isAvailable", method = RequestMethod.GET)
    public JsonResultModel haveAvailableForeignCommentService(long userId) {
        return JsonResultModel.newJsonResultModel(
                Collections.singletonMap("available", serveService.haveAvailableForeignCommentService(userId)));
    }

    //测试用接口
    @RequestMapping(value = "/test_teacher_comment", method = RequestMethod.POST)
    public JsonResultModel testTeacherComment(@RequestBody CommentCardForm commentCardForm, Long userId,String access_token){
        return JsonResultModel.newJsonResultModel(foreignTeacherCommentCardService.testTeacherComment(commentCardForm,userId,access_token));
    }

    //测试用接口
    @RequestMapping(value = "/test_query_all", method = RequestMethod.GET)
    public JsonResultModel testQueryAll(Pageable pageable){
        return JsonResultModel.newJsonResultModel(foreignTeacherCommentCardService.testQueryAll(pageable));
    }
    
}
