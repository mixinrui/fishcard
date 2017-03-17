package com.boxfishedu.workorder.web.controller.monitor;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ansel on 2017/3/16.
 */
@RestController
@RequestMapping(value = "/class/monitor")
public class ClassMonitorController {


    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public Object page(String classType, Pageable pageable){
        Map map = new HashMap();

        return null;
    }
}
