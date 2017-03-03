package com.boxfishedu.workorder.web.controller.transfishcard;

import com.boxfishedu.workorder.service.transfishcard.ComputeFishCard;
import com.boxfishedu.workorder.service.transfishcard.ComputeFishCardOne2One;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hucl on 16/3/31.
 */
@CrossOrigin
@RestController
@RequestMapping("/tansfishcard")
public class TransFishcardController {
    @Autowired
    private ComputeFishCard computeFishCard;


    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public JsonResultModel getWorkorderById() {
        computeFishCard.compute();
        return JsonResultModel.newJsonResultModel(null);
    }

}
