package com.boxfishedu.workorder.web.controller.parent;

import com.boxfishedu.workorder.servicex.home.HomePageServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.TimePickerServiceX;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

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
