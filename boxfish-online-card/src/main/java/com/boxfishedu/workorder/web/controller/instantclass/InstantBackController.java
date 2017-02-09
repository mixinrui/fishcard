package com.boxfishedu.workorder.web.controller.instantclass;

import com.boxfishedu.workorder.servicex.instantclass.InstantClassServiceX;
import com.boxfishedu.workorder.web.param.FishCardFilterParam;
import com.boxfishedu.workorder.web.param.InstantCardFilterParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by hucl on 17/2/9.
 */
@CrossOrigin
@RestController
@RequestMapping("/backend/fishcard")
public class InstantBackController {
    @Autowired
    private InstantClassServiceX instantClassServiceX;

    @RequestMapping(value = "/limit/list", method = RequestMethod.GET)
    public JsonResultModel listFishCardsByLimitUser(InstantCardFilterParam instantCardFilterParam, Pageable pageable) {
        return instantClassServiceX.listInstantCardsByCond(instantCardFilterParam, pageable);
    }
}
