package com.boxfishedu.workorder.requester;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.online.order.entity.OrderForm;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BoxfishException;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.web.param.FetchTeacherParam;
import com.boxfishedu.workorder.web.param.TeacherChangeParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.base.TokenReturnBean;
import com.boxfishedu.workorder.web.view.fishcard.FishCardGroupsInfo;
import com.boxfishedu.workorder.web.view.teacher.PlannerAssignView;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

/**
 * Created by hucl on 16/6/17.
 */
@Component
public class TeacherStudentRequester {
    @Autowired
    private UrlConf urlConf;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    private CacheManager cacheManager;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //从师生运营组批量获取教师列表,key为coursechedule的id
    //url:http://192.168.77.233:8099/course/schedule/add/student/time
    //参数:{"userId":10001,"scheduleModelList":[{"day":1460390400000,"slotId":1,"courseType":"词汇","roleId":1,"id":1}]}
    public Map<String, TeacherView> getTeacherList(FetchTeacherParam fetchTeacherParam) {
        String url = String.format("%s/course/schedule/add/student/time", urlConf.getTeacher_service());
        JsonResultModel jsonResultModel = restTemplate.postForObject(url, fetchTeacherParam, JsonResultModel.class);
        logger.info("========获取教师=======url:{};;;;;;;;参数:{}", url, JSONObject.toJSONString(fetchTeacherParam));
        Map<String, TeacherView> teacherViewMap = new LinkedHashMap<>();
        Map<String, Object> map = ((LinkedHashMap) jsonResultModel.getData());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            TeacherView teacherView = JSONObject.parseObject(JSONObject.toJSONString(entry.getValue()), TeacherView.class);
            teacherViewMap.put(entry.getKey(), teacherView);
        }
        return teacherViewMap;
    }

    //获取答疑师
    public TeacherView getAnswerTeacher(OrderForm orderView) throws BoxfishException {
//        String url = String.format("%s/teacher/answer/assign/%s", urlConf.getTeacher_service(),orderView.getUserID());
        //目前答疑和规划改为同一个人,此处暂时该用规划的接口
        String url = String.format("%s/teacher/planner/assign/%s", urlConf.getTeacher_service(), orderView.getUserId());
        JsonResultModel jsonResultModel = restTemplate.getForObject(url, JsonResultModel.class);
        Map<String, Object> map = (Map<String, Object>) jsonResultModel.getData();
        TeacherView teacherView = new TeacherView();
        teacherView.setAnswerId(Long.valueOf(map.get("plannerId").toString()));
        teacherView.setName(map.getOrDefault("name", "").toString());
        return teacherView;
    }

    //获取规划师
    public PlannerAssignView getPlanTeacher(OrderForm orderView) throws BoxfishException {
        String url = String.format("%s/teacher/planner/assign/%s", urlConf.getTeacher_service()
                , orderView.getUserId(), orderView.getUserId());
        JsonResultModel jsonResultModel = restTemplate.getForObject(url, JsonResultModel.class);
        Map<String, Object> map = (Map<String, Object>) jsonResultModel.getData();
        PlannerAssignView plannerAssignView = new PlannerAssignView();
        plannerAssignView.setName(map.getOrDefault("name", "").toString());
        plannerAssignView.setPlannerId(Long.parseLong(map.get("plannerId").toString()));
        return plannerAssignView;
    }

    @Cacheable(value = DayTimeSlots.CACHE_KEY, key = "#roleId.toString()")
    public DayTimeSlots dayTimeSlotsTemplate(Long roleId) {
        // 1 从师生运营获取一天时间片模板列表
        String url = String.format("%s/timeslot/list/%s", urlConf.getTeacher_service(), roleId);
        logger.info("<-<-<-<-<-<-<-<-向师生运营组发送获取时间片请求,url[{}]", url);
        JsonResultModel jsonResultModel = null;
        try {
            jsonResultModel = restTemplate.getForObject(url, JsonResultModel.class);
            if (HttpStatus.OK.value() != jsonResultModel.getReturnCode()) {
                throw new BusinessException("向师生运营组获取时间片失败" + jsonResultModel.getReturnMsg());
            }
        } catch (Exception ex) {
            logger.error("向师生运营组发送获取时间片失败");
            throw new BusinessException("向师生运营组发送获取时间片失败");
        }

        List<TimeSlots> timeSlotsTemplate = jsonResultModel.getListData(TimeSlots.class);

        // 2 封装成dayTimeSlots
        DayTimeSlots dayTimeSlotsTemplate = new DayTimeSlots();
        dayTimeSlotsTemplate.setDailyScheduleTime(timeSlotsTemplate);

        return dayTimeSlotsTemplate;
    }

    /**
     * 获取单个时间片
     *
     * @param id
     * @return
     */
    @Cacheable(value = TimeSlots.CACHE_KEY, key = "#id.toString()")
    public TimeSlots getTimeSlot(Integer id) {
        String url = String.format("%s/timeslot/" + id, urlConf.getTeacher_service());
        JsonResultModel jsonResultModel = restTemplate.getForObject(url, JsonResultModel.class);
        return jsonResultModel.getData(TimeSlots.class);
    }

    /**
     * 天时间片模板
     *
     * @param teacherId
     * @return
     */
    //@Retryable(value = { RestClientException.class, ResourceAccessException.class }, maxAttempts = 3, backoff=@Backoff(delay = 10, maxDelay = 100))
    public DayTimeSlots dayTimeSlotsTemplate(Long teacherId, Date date) {
        DayTimeSlots dayTimeSlots = cacheManager.getCache(DayTimeSlots.CACHE_KEY).get(teacherId.toString(), DayTimeSlots.class);
        if (dayTimeSlots == null) {
            dayTimeSlots = dayTimeSlotsTemplate(teacherId);
            cacheManager.getCache(DayTimeSlots.CACHE_KEY).put(teacherId.toString(), dayTimeSlots);
        }
        dayTimeSlots.setDay(DateUtil.simpleDate2String(date));
        return dayTimeSlots;
    }

    //TODO:增加教师与学生的上课记录关系
    public void releaseTeacher(WorkOrder workOrder) throws BoxfishException {
        String url = String.format("%s/studentTeacher/add", urlConf.getTeacher_service());
        Map<String, Long> mapParam = new HashMap<>();
        mapParam.put("teacherId", workOrder.getTeacherId());
        mapParam.put("studentId", workOrder.getStudentId());
        logger.debug("<-<-<-<-<-<-@releaseTeacher鱼卡[{}]向师生运营发起释放资源请求,参数{}", workOrder.getId(), JacksonUtil.toJSon(mapParam));
        JsonResultModel jsonResultModel = restTemplate.postForObject(url, mapParam, JsonResultModel.class);
        if (jsonResultModel.getReturnCode() != HttpStatus.OK.value()) {
            logger.error("鱼卡[{}]通知师生运营组释放资源失败!!!!", workOrder.getId());
            return;
        }
        logger.info("@releaseTeacher鱼卡[{}]向师生运营发送释放资源成功", workOrder.getId());
    }

    //TODO:此处的url换为师生运营的url
    public void notifyChangeTeacher(WorkOrder workOrder,TeacherChangeParam teacherChangeParam) {
        StringBuilder builder = new StringBuilder(urlConf.getTeacher_service_admin());// jiaozijun 配合  haijiang  更改师生运营接口
        builder.append("/course/schedule/teacher/change");
        String url = builder.toString();
        logger.info("鱼卡[{}]向师生运营发起换教师的请求[{}]", workOrder.getId(), url);
        Map map = new HashMap<>();
        map.put("day", DateUtil.date2SimpleDate(workOrder.getStartTime()).getTime());
        map.put("timeSlotId", workOrder.getSlotId());
        map.put("teacherId", workOrder.getTeacherId());
        map.put("studentId", workOrder.getStudentId());
        map.put("changeReason",teacherChangeParam.getChangeReason());  /**  如果老师请假  takeforleave  change  **/
        logger.debug("参数{}", JacksonUtil.toJSon(map));
        JsonResultModel jsonResultModel = null;
        try {
            jsonResultModel = restTemplate.postForObject(url, map, JsonResultModel.class);
        } catch (Exception ex) {
            logger.error("向师生运营发送更换老师失败", ex);
            throw new BusinessException("向师生运营请求更换教师失败");
        }
        if (HttpStatus.OK.value() != jsonResultModel.getReturnCode()) {
            logger.error("向师生运营获取教师失败,失败原因:[{}]", jsonResultModel.getReturnMsg());
            throw new BusinessException("教师更换失败:" + jsonResultModel.getReturnMsg());
        }
    }

    public JsonResultModel getPageableTeachers(CourseSchedule courseSchedule, Pageable pageable) {
        String url = String.format(
                "%s/teacher/query/%s/%s?page=%s&size=%s",
                urlConf.getTeacher_service(),
                courseSchedule.getCourseId(),
                courseSchedule.getTimeSlotId(),
                pageable.getPageNumber(),
                pageable.getPageSize());
        return restTemplate.getForObject(url, JsonResultModel.class);
    }

    public Long getTeacherFirstDay(Long teacherId) {
//        Long day = cacheManager.getCache(MonthTimeSlots.CACHE_KEY_TEACHER_FIRST_DAY).get(teacherId.toString(), Long.class);
//        if(day != null) {
//            return day;
//        } else {
        JsonResultModel jsonResultModel = restTemplate.getForObject(createGetTeacherFirstDayURI(teacherId), JsonResultModel.class);
        Map beanMap = (Map) jsonResultModel.getData();
        return (Long) beanMap.get("day");
//        }
    }

    private URI createGetTeacherFirstDayURI(Long teacherId) {
        URI uri = UriComponentsBuilder
                .fromUriString(urlConf.getTeacher_service())
                .path("/course/schedule/teacher/first/" + teacherId)
                .build()
                .toUri();
        logger.debug("<==============@createGetTeacherFirstDayURI向师生运营发送获取教师第一个时间片请求,[{}]", uri.getPath());
        return uri;
    }

    /**
     * 向在线教育push教师消息数据
     */
    public void pushTeacherListOnlineMsg(List teachingOnlineListMsg) {
        String url = String.format("%s/teaching/callback/push", urlConf.getCourse_online_service());
        // String url="http://192.168.77.37:9090/teaching/callback/push";
        logger.debug("::::::::::::::::::::::::::::::::@[pushWrappedMsg]向在线教育推送url[{}]::::::::::::::::::::::::::::::::", url);

        logger.info("::::::::::::::::::::::::::::::::sendDate:begion::[{}]::::::::::::::::::::::::::::::::", JSON.toJSONString(teachingOnlineListMsg));
        threadPoolManager.execute(new Thread(() -> {
            restTemplate.postForObject(url, teachingOnlineListMsg, Object.class);
        }));
        logger.info("::::::::::::::::::::::::::::::::sendData:over::::::::::::::");
        //restTemplate.postForObject(url,teachingOnlineListMsg,Object.class);
        //JsonResultModel jsonResultModel = restTemplate.postForObject(url, teachingOnlineListMsg,JsonResultModel.class);
    }


    public void pushTeacherListOnlineMsg(Object message) {
        String url = String.format("%s/teaching/callback/push", urlConf.getCourse_online_service());
        // String url="http://192.168.77.37:9090/teaching/callback/push";
        logger.debug("::::::::::::::::::::::::::::::::@[pushWrappedMsg]向在线教育推送url[{}]::::::::::::::::::::::::::::::::", url);

        logger.info("::::::::::::::::::::::::::::::::sendDate:begion::[{}]::::::::::::::::::::::::::::::::", JSON.toJSONString(message));
        threadPoolManager.execute(new Thread(() -> {
            restTemplate.postForObject(url, message, Object.class);
        }));
        logger.info("::::::::::::::::::::::::::::::::sendData:over::::::::::::::");
        //restTemplate.postForObject(url,teachingOnlineListMsg,Object.class);
        //JsonResultModel jsonResultModel = restTemplate.postForObject(url, teachingOnlineListMsg,JsonResultModel.class);
    }


    /**
     * http调用师生运营 获取 教师列表
     * teacherType  teacher类型   {chineseTeacher}/{foreignTeacher}    demo:  true/false
     */
    public List pullTeacherListMsg(String teacherType) {
        String url = String.format("%s/seckillteacher/query/%s",
                urlConf.getTeacher_service(), teacherType);
//        String url=String.format("http://192.168.77.186:8099/seckillteacher/demo/query/%s",
//                teacherType);
//        String url=String.format("http://192.168.77.186:8099/seckillteacher/query/true/false",
//                teacherType);
        logger.info("::::::::::::::::::::::::::::::::@[pullTeacherListMsg]向师生运营发起获取教师列表url[{}]::::::::::::::::::::::::::::::::", url);

        JsonResultModel jsonResultModel = restTemplate.getForObject(url, JsonResultModel.class);
        List teacherList = (List) jsonResultModel.getData();
        logger.info("::::::::::::::::::::::::::::::::@[pullTeacherListMsg]向师生运营发起获取教师列表长度size[{}]  Datais[{}]::::::::::::::::::::::::::::::::",
                teacherList == null ? 0 : teacherList.size(), JSON.toJSON(teacherList));
        return teacherList;

    }

    public void notifyCancelTeacher(WorkOrder workOrder) {
        String url = String.format("%s/course/schedule/teacher/course/cancel", urlConf.getTeacher_service_admin());
        logger.info("鱼卡[{}]向师生运营发起取消教师的请求[{}]", workOrder.getId(), url);

        Map map = Maps.newHashMap();
        map.put("day", DateUtil.date2SimpleDate(workOrder.getStartTime()).getTime());
        map.put("timeSlotId", workOrder.getSlotId());
        map.put("teacherId", workOrder.getTeacherId());
        map.put("studentId", workOrder.getStudentId());

        threadPoolManager.execute(new Thread(() -> {
                    restTemplate.postForObject(url, map, JsonResultModel.class);
                })
        );

    }

    //发起教师类型请求,失败则返回默认的中教
    public int getTeacherType(Long teacherId) {
        try {
            String url = String.format("%s/teacher/%s", urlConf.getTeacher_service(), teacherId);
            logger.info("向师生运营发起获取教师信息请求,url:[{}]", url);
            JsonResultModel teacherResult = restTemplate.getForObject(url, JsonResultModel.class);
            Map<String, Object> map = (Map) teacherResult.getData();
            return Integer.parseInt(map.get("teachingType").toString());
        } catch (Exception ex) {
            logger.error("向师生运营发起教师类型请求失败", ex);
        }
        return TeachingType.ZHONGJIAO.getCode();
    }

    /**
     * 根据学生id  ,获取属于该学生所在班级的 所有老师id
     * @param studentId
     * @return
     */
    public List getTeachersBelongToStudent(Long studentId) {
        String url = String.format("%s/user/%s/teacher", urlConf.getStudent_teacher_relation(), studentId);
        logger.info(":::::::::::::::::::::::::::::::makeSendWorkOrder:@[getTeachersBelongToStudent]获取属于该学生所在班级的 所有老师url[{}]::::::::::::::::::::::::::::::::", url);

        JsonResultModel jsonResultModel = restTemplate.getForObject(url, JsonResultModel.class);
        List teacherList = (List) jsonResultModel.getData();
        logger.info("::::::::::::::::::::::::::::::::@[getTeachersBelongToStudent]向师生运营发起获取教师列表长度size[{}]  Datais[{}]::::::::::::::::::::::::::::::::",
                teacherList == null ? 0 : teacherList.size(), JSON.toJSON(teacherList));
        return teacherList;
    }

    /**
     * 获取课程过程中,如果课程类型发生变化,向师生运营发送更换教师请求
     */
    public Boolean changeTeacherForTypeChanged(WorkOrder workOrder){
       String url = new StringBuilder(urlConf.getTeacher_service()).append("/course/schedule/teacher/changeYN").toString();
        Map map = Maps.newHashMap();
        map.put("day", DateUtil.date2SimpleDate(workOrder.getStartTime()).getTime());
        map.put("timeSlotId", workOrder.getSlotId());
        map.put("teacherId", workOrder.getTeacherId());
        map.put("studentId", workOrder.getStudentId());
        map.put("courseType",workOrder.getCourseType());
        logger.debug("@changeTeacherForTypeChanged#{}向师生运营发起换教师的请求[{}],参数[{}]", workOrder.getId(), url,JacksonUtil.toJSon(map));
        JsonResultModel jsonResultModel = null;
        try {
            jsonResultModel = restTemplate.postForObject(url, map, JsonResultModel.class);
        } catch (Exception ex) {
            logger.error("@changeTeacherForTypeChanged#{}#exception向师生运营发送判断是否更换老师失败",workOrder.getId(), ex);
            throw new BusinessException("向师生运营请求更换教师失败");
        }
        if (HttpStatus.OK.value() != jsonResultModel.getReturnCode()) {
            logger.error("@changeTeacherForTypeChanged#{}#returnException向师生运营发送判断是否更换老师失败:[{}]", workOrder.getId(),jsonResultModel.getReturnMsg());
            throw new BusinessException("教师更换失败:" + jsonResultModel.getReturnMsg());
        }
        return (Boolean)jsonResultModel.getData();
    }


    /**
     * 关闭订单
     * @param orderCode
     */
    public void closeOrderByOrderCode(String orderCode){
        String url = new StringBuffer(urlConf.getOrder_service()).append("/closed/").append(orderCode).toString();
        logger.info("@closeOrderByOrderCode#{}",orderCode);
        JsonResultModel jsonResultModel = null;
        try {
            jsonResultModel = restTemplate.postForObject(url,null,JsonResultModel.class);
        }catch (Exception e){
            logger.error("@closeOrderByOrderCoded#{}#returnException 关闭订单:[{}]", orderCode,jsonResultModel==null?"null":jsonResultModel.getReturnMsg());
            throw new BusinessException("关闭订单失败:" + jsonResultModel.getReturnMsg());
        }

    }

    /**
     * token验证接口
     * @param token
     * @return
     */
    public TokenReturnBean checkTokenCommon(String token) {
        String url = urlConf.getLogin_filter_url() + "/box/fish/access/token/query/self";
        logger.info("checkTokenPrivilege - [{}]",url);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("BoxFishAccessToken", token);
        HttpEntity request = new HttpEntity(httpHeaders);
        TokenReturnBean tokenCheckObject;
        try {
            tokenCheckObject = restTemplate.postForObject(url, request, TokenReturnBean.class);
        } catch (Exception e) {
            logger.error(e.getMessage());
            tokenCheckObject = null;
        }
        return tokenCheckObject;
    }

    public TokenReturnBean checkTokenPrivilege(String token,String path) {
        if(!path.startsWith("/comment") &&   !path.startsWith("/fishcard")){
            path= "/fishcard"+path;
        }
        String url = urlConf.getLogin_filter_url() + "/box/fish/access/token/verification?systemName=" + "FishCardCenter" +"&accessToken="+token+"&requestURI="+path;
        logger.info("checkTokenPrivilege - [{}]",url);
        TokenReturnBean tokenReturnBean;
        try {
            tokenReturnBean = restTemplate.getForObject(url, TokenReturnBean.class);
        } catch (Exception e) {
            logger.error(e.getMessage());
            tokenReturnBean = null;
        }

        return tokenReturnBean;
    }


    /**
     * 查询向在线教学获取鱼卡的房间号信息
     * @param listFishCards
     */
    public FishCardGroupsInfo [] getFishcardMessage(List listFishCards) {
        String url = String.format("%s/teaching/group/member", urlConf.getCourse_online_service());
        logger.debug("::::::::::::::::::::::::::::::::@[getFishcardMessage]向在线教学请求房间号url[{}]::::::::::::::::::::::::::::::::", url);
        FishCardGroupsInfo [] fishCardGroupsInfo = null;
        logger.info("::::::::::::::::::::::::::::::::sendDate:begion::[{}]::::::::::::::::::::::::::::::::", JSON.toJSONString(listFishCards));
        try{
            fishCardGroupsInfo =  restTemplate.postForObject(url, listFishCards, FishCardGroupsInfo[].class);
        }catch (Exception e){
            logger.error(e.getMessage());
            fishCardGroupsInfo = null;
        }

        logger.info("::::::::::::::::::::::::::::::::sendData:over::::::::::::::");
        return fishCardGroupsInfo;
    }


}
