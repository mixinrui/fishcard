package com.boxfishedu.workorder.web.controller.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.common.exception.ValidationException;
import com.boxfishedu.workorder.dao.jpa.CommentCardJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.entity.mysql.CommentCardForm;
import com.boxfishedu.workorder.entity.mysql.UpdatePicturesForm;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.commentcard.ForeignTeacherCommentCardService;
import com.boxfishedu.workorder.service.commentcard.sdk.CommentCardSDK;
import com.boxfishedu.workorder.servicex.commentcard.CommentTeacherAppServiceX;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
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

    @Autowired
    CommentTeacherAppServiceX commentTeacherAppServiceX;

    @Autowired
    private CacheManager cacheManager;

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

    //@RequestMapping(value = "/isAvailable", method = RequestMethod.GET)
    public JsonResultModel haveAvailableForeignCommentService(Long userId) {
        return JsonResultModel.newJsonResultModel(
                Collections.singletonMap("available", serveService.haveAvailableForeignCommentService(userId)));
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public JsonResultModel getAvailableForeignCommentServiceCount(Long userId) {
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

    //测试换头像
    @RequestMapping(value = "/update/pic",method = RequestMethod.GET)
    public void updatePic(){
        String path = "www.baidu.com";
        Long studentId = 1299462l;
        UpdatePicturesForm updatePicturesForm = new UpdatePicturesForm();
        updatePicturesForm.setId(studentId);
        updatePicturesForm.setFigure_url(path);
        updatePicturesForm.setType("STUDENT");
        updatePicturesForm.setNickname("ansel");
        foreignTeacherCommentCardService.updateCommentCardsPictures(updatePicturesForm);
    }

    //获取初始化外教点评主页列表
    @RequestMapping(value = "/handle/course_type_difficulty", method = RequestMethod.GET)
    public Object testCourseTypeAndDifficulty(){
        commentTeacherAppServiceX.initializeCommentHomePage();
        return new JsonResultModel();
    }

    //all测试接口
    @RequestMapping(value = "/ansel/test", method = RequestMethod.GET)
    public Object anselTest(){
        return commentCardJpaRepository.getUncommentedCard(1299462l);
    }
}
