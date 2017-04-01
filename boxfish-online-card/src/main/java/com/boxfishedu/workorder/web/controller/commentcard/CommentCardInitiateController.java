package com.boxfishedu.workorder.web.controller.commentcard;

import com.boxfishedu.workorder.service.commentcard.CommentCardInitiateService;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by ansel on 16/11/22.
 */
@RestController
@RequestMapping(value = "/comment_card/initiate")
public class CommentCardInitiateController {

    @Autowired
    private CommentCardInitiateService commentCardInitiateService;

    @RequestMapping(value = "/course/type_and_difficulty", method = RequestMethod.GET)
    public Object initiateCourseTypeAndDifficulty(){

        return "本次初始化个数为" + commentCardInitiateService.initiateCourseTypeAndDifficulty();
    }

    @RequestMapping(value = "/handle/update/course/type_and_difficulty", method = RequestMethod.GET)
    public Object handleUpdateCourseTypeAndDifficulty(){
        
        commentCardInitiateService.timeToUpdateTypeAndDifficulty();
        return new JsonResultModel();
    }
}
