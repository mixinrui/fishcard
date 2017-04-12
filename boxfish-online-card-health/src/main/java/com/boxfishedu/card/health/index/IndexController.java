package com.boxfishedu.card.health.index;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by hucl on 16/4/11.
 */
@CrossOrigin
@RestController
@RequestMapping("/")
public class IndexController {

    @RequestMapping(value = "index", method = RequestMethod.GET)
    public ModelAndView index() {
        System.out.println("hello");
        return new ModelAndView("user_detail");
    }

}
