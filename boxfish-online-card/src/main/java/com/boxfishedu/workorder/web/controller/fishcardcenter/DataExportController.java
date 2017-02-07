package com.boxfishedu.workorder.web.controller.fishcardcenter;

import com.boxfishedu.workorder.servicex.fishcardcenter.FishCardExcelServiceX;
import com.boxfishedu.workorder.web.param.ExcelPageAble;
import com.boxfishedu.workorder.web.param.FishCardFilterParam;
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
 * 用于后台excel导出等功能
 * Created by hucl on 16/7/5.
 */
@CrossOrigin
@RestController
@RequestMapping("/backend/fishcard/excel")
public class DataExportController {

    @Autowired
    private FishCardExcelServiceX fishCardExcelServiceX;
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public JsonResultModel listFishCardsByCond(FishCardFilterParam fishCardFilterParam, HttpServletResponse response,ExcelPageAble excelPageAble) {

//        ExcelPageAble    Pageable pageable
        Pageable pageable = new PageRequest(excelPageAble.getPage(),excelPageAble.getSize());
        fishCardExcelServiceX.exportExcel(fishCardFilterParam,response,pageable);
        return JsonResultModel.newJsonResultModel();
    }

    @RequestMapping(value = "/export/buke", method = RequestMethod.GET)
    public JsonResultModel listFishBuke(HttpServletResponse response) {
        fishCardExcelServiceX.exportExcelbuke(response);
        return JsonResultModel.newJsonResultModel();
    }
}

