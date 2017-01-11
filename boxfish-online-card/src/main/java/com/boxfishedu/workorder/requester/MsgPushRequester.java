package com.boxfishedu.workorder.requester;

import com.boxfishedu.workorder.common.bean.MessagePushTypeEnum;
import com.boxfishedu.workorder.common.bean.TeachingOnlineGroupMsg;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.mongo.InstantCardLogMorphiaRepository;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Created by hucl on 16/12/22.
 */
@Component
public class MsgPushRequester {
    @Autowired
    private UrlConf urlConf;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    private InstantCardLogMorphiaRepository instantCardLogMorphiaRepository;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void notifyInstantGroupClassMsg(InstantClassCard instantClassCard, List<Long> teacherIds) {
        //打开混合的情况
        String url = String.format("%s/notification/push?type=%s",
                                   urlConf.getMsg_push_url(),"INSTANCE_CLASS");

//        String url = String.format("%s/notification/push",
//                                   urlConf.getMsg_push_url());

        logger.info("@notifyInstantGroupClassMsg,向教师发起立即上课推送,url[{}]", url);
//        instantCardLogMorphiaRepository.saveInstantLog(instantClassCard, teacherIds, "向教师发起推送");
        TeachingOnlineGroupMsg teachingOnlineGroupMsg = new TeachingOnlineGroupMsg();
        teachingOnlineGroupMsg.setPush_title("Many students are waiting for class. Now matching...");

        TeachingOnlineGroupMsg.TeachingOnlineMsgAttach teachingOnlineMsgAttach = new TeachingOnlineGroupMsg.TeachingOnlineMsgAttach();
        teachingOnlineMsgAttach.setType(MessagePushTypeEnum.SEND_INSTANT_CLASS_TYPE.toString());
        teachingOnlineMsgAttach.setCardId(instantClassCard.getId());
        teachingOnlineMsgAttach.setDay(DateUtil.simpleDate2String(instantClassCard.getClassDate()));
        teachingOnlineMsgAttach.setSlotId(instantClassCard.getSlotId());
        teachingOnlineMsgAttach.setCount(teacherIds.size());
        teachingOnlineMsgAttach.setStudentId(instantClassCard.getStudentId());
        teachingOnlineGroupMsg.setData(teachingOnlineMsgAttach);

        teacherIds.forEach(teacherId -> teachingOnlineGroupMsg.getUser_id().add(teacherId));

        logger.debug(">>>>>>@notifyInstantClassMsg, IIIIIIIIIIIIIII 向教师发起推送实时上课请求,courseInfo[{}],教师信息[{}]"
                , JacksonUtil.toJSon(teachingOnlineGroupMsg), teacherIds);
        threadPoolManager.execute(new Thread(() -> {
            restTemplate.postForObject(url, teachingOnlineGroupMsg, Object.class);
        }));
    }
}
