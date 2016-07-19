package com.boxfishedu.workorder.web.controller.comment;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.service.ServeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

/**
 * Created by LuoLiBing on 16/7/19.
 */
@CrossOrigin
@RestController
@RequestMapping("/comment/foreign")
public class ForeignCommentController {

    @Autowired
    private ServeService serveService;

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
