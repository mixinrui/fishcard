package com.boxfishedu.workorder.web.controller.balancecenter;

import com.boxfishedu.workorder.servicex.balancecenter.FishCardBalanceQueryServiceX;
import com.boxfishedu.workorder.web.param.FishCardFilterParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by hucl on 16/5/10.用于结算中心
 */
@CrossOrigin
@RestController
@RequestMapping("/balance/fishcard")
public class FishCardStatisticController {
    @Autowired
    private FishCardBalanceQueryServiceX fishCardBalanceQueryServiceX;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public JsonResultModel listFishCardsByCond(FishCardFilterParam fishCardFilterParam) {
        JsonResultModel jsonResultModel= fishCardBalanceQueryServiceX.listFishCardsByCond(fishCardFilterParam);
        return jsonResultModel;
    }
}
