package com.boxfishedu.workorder.web.controller.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.service.commentcard.SyncCommentCard2SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by ansel on 16/10/11.
 */
@RestController
@RequestMapping(value = "//comment/foreign")
public class SyncCommentCard2SystemController {

    @Autowired
    SyncCommentCard2SystemService syncCommentCard2SystemService;

    @RequestMapping(value = "/initialize/comment_card_2_system", method = RequestMethod.GET)
    public JsonResultModel syncCommentCards2System(){
        long count = syncCommentCard2SystemService.initializeCommentCard2System();
        JsonResultModel jsonResultModel = new JsonResultModel();
        jsonResultModel.setData(count);
        jsonResultModel.setReturnCode(200);
        jsonResultModel.setReturnMsg("success");
        return jsonResultModel;
    }
}
