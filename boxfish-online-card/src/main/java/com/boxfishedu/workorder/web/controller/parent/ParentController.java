package com.boxfishedu.workorder.web.controller.parent;

import com.boxfishedu.workorder.entity.mongo.AccountCardInfo;
import com.boxfishedu.workorder.servicex.home.HomePageServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.TimePickerServiceX;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import java.util.Locale;

/**
 * Created by hucl on 16/11/30.
 */
@CrossOrigin
@RestController
@RequestMapping("/service/parent")
@SuppressWarnings("ALL")
public class ParentController {
    @Autowired
    private HomePageServiceX homePageServiceX;

    @Autowired
    private TimePickerServiceX timePickerServiceX;

    @RequestMapping(value = "/card_infos", method = RequestMethod.GET)
    public JsonResultModel userCardInfo(String order_type, Long studentId,Long parentId) {
        return homePageServiceX.getHomePage(order_type, studentId);
    }

    @RequestMapping(value = "/schedule/page", method = RequestMethod.GET)
    public Object courseSchedulePage( Long studentId, Long parentId,
                                     @PageableDefault(value = 15) Pageable pageable,
                                     Locale locale) {
        return timePickerServiceX.getCourseSchedulePage(studentId, pageable, locale);
    }
}
