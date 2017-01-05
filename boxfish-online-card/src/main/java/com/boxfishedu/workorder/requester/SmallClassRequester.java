package com.boxfishedu.workorder.requester;

import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Created by hucl on 17/1/5.
 */
@Component
public class SmallClassRequester {
    @Autowired
    private UrlConf urlConf;

    @Autowired
    private RestTemplate restTemplate;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取用户难度信息
     * @param userId
     * @return 如:"LEVEL_2"
     */
    public String fetchUserDifficultyInfo(Long userId){
        String url=String.format("%s/core/difficulty/%s",urlConf.getCourse_wudaokou_recommend_service(),userId);
        logger.debug("@fetchUserDifficultyInfo获取用户课程难度信息,url[{}]",url);
        String difficulty=null;
        try {
            difficulty = restTemplate.getForObject(url, String.class);
        }
        catch (Exception ex){
            logger.error("@fetchUserDifficultyInfo获取用户课程难度信息失败,url[{}]",url,ex);
            throw new BusinessException("获取用户难度信息失败");
        }
        return difficulty;
    }

    public 


}
