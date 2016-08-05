package com.boxfishedu.workorder.web.controller.fishcardcenter;

import com.boxfishedu.workorder.servicex.fishcardcenter.FishCardExcelServiceX;
import com.boxfishedu.workorder.web.param.FishCardFilterParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * 用于后台excel导出等功能
 * Created by jiaozijun on 16/8/4.
 */

@CrossOrigin
@RestController
@RequestMapping("/backend/excel")
public class FishCardExcelController {

    @Autowired
    private FishCardExcelServiceX fishCardExcelServiceX;
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public JsonResultModel listFishCardsByCond(FishCardFilterParam fishCardFilterParam,HttpServletResponse response, Pageable pageable) {
        fishCardExcelServiceX.exportExcel(fishCardFilterParam,response,pageable);
        return JsonResultModel.newJsonResultModel();
    }

}
