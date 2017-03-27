package com.boxfishedu.workorder.web.controller.smallclass;

import com.boxfishedu.workorder.servicex.smallclass.SmallClassExcelServiceX;
import com.boxfishedu.workorder.web.param.ExcelPageAble;
import com.boxfishedu.workorder.web.param.FishCardFilterParam;
import com.boxfishedu.workorder.web.param.fishcardcenetr.PublicFilterParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by jiaozijun on 17/3/27.
 */
@CrossOrigin
@RestController
@RequestMapping("/backend/fishcard/excel")
public class DataExportSmallClassController {


    @Autowired
    private SmallClassExcelServiceX smallClassExcelServiceX;

    @RequestMapping(value = "/small/export", method = RequestMethod.GET)
    public JsonResultModel listFishCardsByCond(PublicFilterParam publicFilterParam, HttpServletResponse response, ExcelPageAble excelPageAble) {
        Pageable pageable = new PageRequest(excelPageAble.getPage(),excelPageAble.getSize());
        smallClassExcelServiceX.exportExcel(publicFilterParam,response,pageable);
        return JsonResultModel.newJsonResultModel();
    }
}
