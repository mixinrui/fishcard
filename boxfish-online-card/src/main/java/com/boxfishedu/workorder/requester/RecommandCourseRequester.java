package com.boxfishedu.workorder.requester;

import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hucl on 16/6/17.
 */
@Component
public class RecommandCourseRequester {
    @Autowired
    private UrlConf urlConf;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ThreadPoolManager threadPoolManager;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    public RecommandCourseView getRecommandCourse(WorkOrder workOrder, Integer index) {
        String url = String.format("%s/online/%s/%s", urlConf.getCourse_recommended_service(), workOrder.getStudentId(), index);
        logger.debug("@<-<-<-<-<-<-向推荐课发起获取推荐课的请求,url:[{}]", url);
        RecommandCourseView recommandCourseView = null;
        try {
            recommandCourseView = restTemplate.postForObject(url, null, RecommandCourseView.class);
            if (null == recommandCourseView) {
                throw new BusinessException();
            }
            logger.info("@->->->->->->->获取推荐课成功,返回值:{}", JacksonUtil.toJSon(recommandCourseView));
        } catch (Exception ex) {
            logger.error("!!!!!!!!!!!!!!向推荐课发起请求失败[{}]", ex.getMessage(), ex);
            throw new BusinessException("获取推荐课程失败:" + ex.getMessage());
        }
        return recommandCourseView;
    }

    /**
     * 取消课程信息
     */
    public void cancelOldRecommandCourse(WorkOrder workOrder) {
        String url = String.format("%s/cancel/%s/%s", urlConf.getCourse_recommended_service(), workOrder.getStudentId(), workOrder.getCourseId());
        logger.debug("@<-<-<-<-<-<-向推荐课发起取消课程的请求,url:[{}]", url);
        try {
            Object object = restTemplate.postForObject(url, null, Object.class);
        } catch (Exception ex) {
            logger.error("!!!!!!!!!!!!!!向推荐课发起取消请求失败[{}]", ex.getMessage(), ex);
        }
    }

    public String getThumbNailPath(RecommandCourseView courseView) {
        return String.format("%s%s", urlConf.getThumbnail_server(), courseView.getCover());
    }


    public RecommandCourseView getRecommandCourse(WorkOrder workOrder) {
        return getRecommandCourse(workOrder, workOrder.getSeqNum());
    }

    public RecommandCourseView getRecommandCourse___________mock(WorkOrder workOrder, Integer index) {
        RecommandCourseView recommandCourseView = mockCourses().get(index.intValue() + "");
        logger.info("假课程[{}]", JacksonUtil.toJSon(recommandCourseView));
        return recommandCourseView;
    }

    public Map<String, RecommandCourseView> mockCourses() {
        RecommandCourseView recommandCourseView1 = new RecommandCourseView();
        recommandCourseView1.setCourseId("L3NoYXJlL3N2bi9GdW5jdGlvbiDluIzmnJvlkozmhL_mnJsvMTM3LuWmguS9leivoumXruWvueaWueaDs-imgeS7gOS5iO-8ny54bHN4");
        recommandCourseView1.setCourseName("如何询问对方想要什么？");
        recommandCourseView1.setCover("8ee3399912731a86abd1fab10e01e952");
        recommandCourseView1.setCourseType("READING");
        recommandCourseView1.setDifficulty(1);

        RecommandCourseView recommandCourseView2 = new RecommandCourseView();
        recommandCourseView2.setCourseId("L3NoYXJlL3N2bi9GdW5jdGlvbiDnm7jkvLzlkozlt67liKsvNTA1LuWmguS9leihqOi-vuKAnOWSjC4uLuS4gOagt-KAne-8ny54bHN4");
        recommandCourseView2.setCourseName("如何表达“和...一样”？");
        recommandCourseView2.setCover("e1f4503e21e32117611603d72621f105");
        recommandCourseView2.setCourseType("FUNCTION");
        recommandCourseView2.setDifficulty(1);

        RecommandCourseView recommandCourseView3 = new RecommandCourseView();
        recommandCourseView3.setCourseId("L3NoYXJlL3N2bi9GdW5jdGlvbiDmhI_mhL_lkozmiZPnrpcvMTQzLuaAjuagt-ivoumXruKAnOS9oOaEv-aEj-WKoOWFpeWQl-KAne-8ny54bHN4");
        recommandCourseView3.setCourseName("怎样询问“你愿意加入吗”？");
        recommandCourseView3.setCover("b5fa860d55f83b85a20736bf54b9d43e");
        recommandCourseView3.setCourseType("PHONICS");
        recommandCourseView3.setDifficulty(1);

        RecommandCourseView recommandCourseView4 = new RecommandCourseView();
        recommandCourseView4.setCourseId("L3NoYXJlL3N2bi9GdW5jdGlvbiDor63oqIDkuqTpmYXlm7Dpmr4vMzIyLuWmguS9leihqOi-vuWBmuafkOS6i-acieWbsOmavu-8ny54bHN4");
        recommandCourseView4.setCourseName("如何表达做某事有困难?");
        recommandCourseView4.setCover("9b174a8a28bc42e511359fbac957c861");
        recommandCourseView4.setCourseType("EXAMINATION");
        recommandCourseView4.setDifficulty(1);

        RecommandCourseView recommandCourseView5 = new RecommandCourseView();
        recommandCourseView5.setCourseId("L3NoYXJlL3N2bi9GdW5jdGlvbiDnuqbkvJovNDMwLuWmguS9leihqOi-vui_n-WIsO-8ny54bHN4");
        recommandCourseView5.setCourseName("如何表达迟到？");
        recommandCourseView5.setCover("4f4b0d2f518641b5b651bf6ad08ea7bf");
        recommandCourseView5.setCourseType("FUNCTION");
        recommandCourseView5.setDifficulty(1);

        RecommandCourseView recommandCourseView6 = new RecommandCourseView();
        recommandCourseView6.setCourseId("L3NoYXJlL3N2bi9GdW5jdGlvbiDorablkYrlkoznpoHmraIvNDExLuWmguS9leihqOekuuWBnOatouWBmuafkOS6i--8ny54bHN4");
        recommandCourseView6.setCourseName("如何表示停止做某事？");
        recommandCourseView6.setCover("84200f7132fb436ead08b8d6e5e039f4");
        recommandCourseView6.setCourseType("PHONICS");
        recommandCourseView6.setDifficulty(1);

        RecommandCourseView recommandCourseView7 = new RecommandCourseView();
        recommandCourseView7.setCourseId("L3NoYXJlL3N2bi9Ub3BpY1_kuKrkurrmg4XlhrUvMDA1LuW3puaSh-WtkOeahOS6uuacieS7gOS5iOS8mOWKv--8ny54bHN4");
        recommandCourseView7.setCourseName("左撇子的人有什么优势？");
        recommandCourseView7.setCover("cb8103ede2816ab65b41036ad17d56c6");
        recommandCourseView7.setCourseType("PHONICS");
        recommandCourseView7.setDifficulty(1);

        RecommandCourseView recommandCourseView8 = new RecommandCourseView();
        recommandCourseView8.setCourseId("L3NoYXJlL3N2bi9BZHZhbmNlZCAxLzAwNi5EbyBBbWVyaWNhbnMgYW5kIENoaW5lc2Ugc2hvcCB0aGUgc2FtZSB3YXk_Lnhsc3g");
        recommandCourseView8.setCourseName("Do Americans and Chinese shop the same way?");
        recommandCourseView8.setCover("5811c6cd8016028e205f7116be299f36");
        recommandCourseView8.setCourseType("TALK");
        recommandCourseView8.setDifficulty(1);

        Map<String, RecommandCourseView> map = new HashMap<>();
        map.put("1", recommandCourseView1);
        map.put("2", recommandCourseView2);
        map.put("3", recommandCourseView3);
        map.put("4", recommandCourseView4);
        map.put("5", recommandCourseView5);
        map.put("6", recommandCourseView6);
        map.put("7", recommandCourseView7);
        map.put("8", recommandCourseView8);
        return map;
    }

    //课程完成后,通知推荐课程服务
    public void notifyCompleteCourse(WorkOrder workOrder) {
        String url = String.format("%s/counter/user_id/%s/lesson_id/%s", urlConf.getCourse_recommended_service(),
                workOrder.getStudentId(), workOrder.getCourseId());
        logger.info("上课结束,通知推荐课url::::[{}]", url);
        threadPoolManager.execute(new Thread(() -> {
            try {
                restTemplate.postForObject(url, null, Object.class);
            } catch (Exception ex) {
                logger.error("上课结束通知推荐课服务失败", ex);
            }

        }));
    }

}
