package com.boxfishedu.workorder.requester;

import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.online.order.entity.OrderForm;
import com.boxfishedu.online.order.entity.TeacherForm;
import com.boxfishedu.workorder.common.bean.TeachingOnlineListMsg;
import com.boxfishedu.workorder.common.bean.TeachingOnlineMsg;
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
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.teacher.PlannerAssignView;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
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

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    //从师生运营组批量获取教师列表,key为coursechedule的id
    //url:http://192.168.77.233:8099/course/schedule/add/student/time
    //参数:{"userId":10001,"scheduleModelList":[{"day":1460390400000,"slotId":1,"courseType":"词汇","roleId":1,"id":1}]}
    public Map<String,TeacherView> getTeacherList(FetchTeacherParam fetchTeacherParam){
        String url = String.format("%s/course/schedule/add/student/time", urlConf.getTeacher_service());
        JsonResultModel jsonResultModel=restTemplate.postForObject(url,fetchTeacherParam,JsonResultModel.class);
        logger.info("========获取教师=======url:{};;;;;;;;参数:{}",url, JSONObject.toJSONString(fetchTeacherParam));
        Map<String,TeacherView> teacherViewMap=new LinkedHashMap<>();
        Map<String,Object> map=((LinkedHashMap)jsonResultModel.getData());
        for(Map.Entry<String,Object> entry:map.entrySet()){
            TeacherView teacherView = JSONObject.parseObject(JSONObject.toJSONString(entry.getValue()), TeacherView.class);
            teacherViewMap.put(entry.getKey(),teacherView);
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

    @Cacheable(value = DayTimeSlots.CACHE_KEY, key = "#teacherId.toString()")
    public DayTimeSlots dayTimeSlotsTemplate(Long teacherId) {
        // 1 从师生运营获取一天时间片模板列表
        String url = String.format("%s/timeslot/list/%s", urlConf.getTeacher_service(), teacherId);
        logger.info("<-<-<-<-<-<-<-<-向师生运营组发送获取时间片请求,url[{}]",url);
        JsonResultModel jsonResultModel = null;
        try {
            jsonResultModel = restTemplate.getForObject(url, JsonResultModel.class);
            if(HttpStatus.OK.value()!=jsonResultModel.getReturnCode()){
                throw  new BusinessException("向师生运营组获取时间片失败"+jsonResultModel.getReturnMsg());
            }
        }
        catch (Exception ex){
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
     * @param teacherId
     * @return
     */
    //@Retryable(value = { RestClientException.class, ResourceAccessException.class }, maxAttempts = 3, backoff=@Backoff(delay = 10, maxDelay = 100))
    public DayTimeSlots dayTimeSlotsTemplate(Long teacherId, Date date) {
        DayTimeSlots dayTimeSlots = cacheManager.getCache(DayTimeSlots.CACHE_KEY).get(teacherId.toString(), DayTimeSlots.class);
        if(dayTimeSlots == null) {
            dayTimeSlots = dayTimeSlotsTemplate(teacherId);
            cacheManager.getCache(DayTimeSlots.CACHE_KEY).put(teacherId.toString(), dayTimeSlots);
        }
        dayTimeSlots.setDay(DateUtil.simpleDate2String(date));
        return dayTimeSlots;
    }

    //TODO:增加教师与学生的上课记录关系
    public void releaseTeacher(WorkOrder workOrder) throws BoxfishException {
        String url = String.format("%s/studentTeacher/add", urlConf.getTeacher_service());
        Map<String,Long> mapParam=new HashMap<>();
        mapParam.put("teacherId",workOrder.getTeacherId());
        mapParam.put("studentId",workOrder.getStudentId());
        logger.debug("<-<-<-<-<-<-@releaseTeacher鱼卡[{}]向师生运营发起释放资源请求,参数{}",workOrder.getId(), JacksonUtil.toJSon(mapParam));
        JsonResultModel jsonResultModel=restTemplate.postForObject(url,mapParam,JsonResultModel.class);
        if(jsonResultModel.getReturnCode()!= HttpStatus.OK.value()){
            logger.error("鱼卡[{}]通知师生运营组释放资源失败!!!!",workOrder.getId());
            return;
        }
        logger.info("@releaseTeacher鱼卡[{}]向师生运营发送释放资源成功",workOrder.getId());
    }

    //TODO:此处的url换为师生运营的url
    public void notifyChangeTeacher(WorkOrder workOrder) {
        StringBuilder builder=new StringBuilder(urlConf.getTeacher_service());
        builder.append("/course/schedule/teacher/change");
        String url=builder.toString();
        logger.info("鱼卡[{}]向师生运营发起换教师的请求[{}]",workOrder.getId(),url);
        Map map=new HashMap<>();
        map.put("day", DateUtil.date2SimpleDate(workOrder.getStartTime()).getTime());
        map.put("timeSlotId",workOrder.getSlotId());
        map.put("teacherId",workOrder.getTeacherId());
        map.put("studentId",workOrder.getStudentId());
        logger.debug("参数{}", JacksonUtil.toJSon(map));
        JsonResultModel jsonResultModel=null;
        try {
            jsonResultModel=restTemplate.postForObject(url,map,JsonResultModel.class);
        }
        catch (Exception ex){
            logger.error("向师生运营发送更换老师失败",ex);
            throw new BusinessException("向师生运营请求更换教师失败");
        }
        if(HttpStatus.OK.value()!=jsonResultModel.getReturnCode()){
            logger.error("向师生运营获取教师失败,失败原因:[{}]",jsonResultModel.getReturnMsg());
            throw new BusinessException("教师更换失败:"+jsonResultModel.getReturnMsg());
        }
    }

    public JsonResultModel getPageableTeachers(CourseSchedule courseSchedule,Pageable pageable){
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
        URI uri= UriComponentsBuilder
                .fromUriString(urlConf.getTeacher_service())
                .path("/course/schedule/teacher/first/" + teacherId)
                .build()
                .toUri();
        logger.debug("<==============@createGetTeacherFirstDayURI向师生运营发送获取教师第一个时间片请求,[{}]",uri.getPath());
        return uri;
    }



    /**
     *  向在线教育push教师消息数据
     */
    public void pushTeacherListOnlineMsg(TeachingOnlineListMsg teachingOnlineListMsg){
        String url=String.format("%s/teaching/callback/push?user_id=%s&push_title=%s",
                urlConf.getCourse_online_service());
        logger.debug("<<<<<<<<<<<<<@[pushWrappedMsg]向在线教育发起获取教师列表url[{}]",url);
        threadPoolManager.execute(new Thread(()->{restTemplate.postForObject(url,teachingOnlineListMsg,Object.class);}));
    }



    /**
     *  http调用师生运营 获取 教师列表
     *  teacherType  teacher类型   {ct:1,wt:1}   需要中教 和  外教    0  表示不需要
     */
    public List<TeacherForm> pullTeacherListMsg(String teacherType){
        String url=String.format("%s/teaching/callback/push?teacherType=%s",
                urlConf.getCourse_online_service(),teacherType);
        logger.debug("<<<<<<<<<<<<<@[pullTeacherListMsg]向师生运营发起获取教师列表url[{}]",url);

        JsonResultModel jsonResultModel = restTemplate.getForObject(url, JsonResultModel.class);
//        Map beanMap = (Map) jsonResultModel.getData();
//        return (Long) beanMap.get("day");

        return  null;

    }
}
