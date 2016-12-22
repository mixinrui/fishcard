package com.boxfishedu.workorder.service;

import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.mall.domain.order.OrderForm;
import com.boxfishedu.mall.domain.product.ComboDurations;
import com.boxfishedu.mall.domain.product.ProductCombo;
import com.boxfishedu.mall.domain.product.ProductComboDetail;
import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import com.boxfishedu.mall.enums.ProductType;
import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.bean.*;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BoxfishException;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mongo.ScheduleCourseInfo;
import com.boxfishedu.workorder.entity.mongo.TrialCourse;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.base.BaseService;
import com.boxfishedu.workorder.service.commentcard.SyncCommentCard2SystemService;
import com.boxfishedu.workorder.servicex.commentcard.CommentTeacherAppServiceX;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.course.CourseView;
import com.boxfishedu.workorder.web.view.course.ResponseCourseView;
import com.boxfishedu.workorder.web.view.order.OrderDetailView;
import com.boxfishedu.workorder.web.view.order.ProductSKUView;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by hucl on 16/3/31.
 */
@SuppressWarnings("ALL")
@Component
public class ServeService extends BaseService<Service, ServiceJpaRepository, Long> {
    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;
    @Autowired
    private ServiceJpaRepository serviceJpaRepository;
    @Autowired
    private UrlConf urlConf;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private RabbitMqSender rabbitMqSender;

    @Autowired
    private RedisService redisService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private ScheduleCourseInfoService scheduleCourseInfoService;

    @Autowired
    private CommentTeacherAppServiceX commentTeacherAppServiceX;

    @Autowired
    SyncCommentCard2SystemService syncCommentCard2SystemService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    static ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)       // 属性为空（“”）或者为 NULL 都不序列化
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    public boolean isServiceExists(Service service) {
        return (null != service);
    }

    public boolean isServiceValid(Service service) {
        Date endDate = service.getEndTime();
        if (endDate.before(new Date())) {
            return false;
        }
        return true;
    }

    //服务数量是否够用
    public boolean isServiceOutNumber(Service service) {
        return (service.getAmount() > 0);
    }

    public boolean isServiceValidX(Service service) throws BoxfishException {
        if (null == service) {
            return false;
        }
        //暂时不考虑过期情况
//        Date endDate = service.getEndTime();
//        if (endDate.before(new Date())) {
//            return false;
//        }
        return true;
    }

    public ResponseCourseView getCoursesByStudentId(Long studentId) {
        ResponseCourseView responseCourseView = null;
        StringBuilder buffer = new StringBuilder(urlConf.getCourse_recommended_service()).
                append("/core/course/calculator/").append(studentId).append("/1");
        try {
            responseCourseView = restTemplate.getForObject(buffer.toString(), ResponseCourseView.class);
            if (null == responseCourseView) {
                throw new BoxfishException();
            }
        } catch (Exception ex) {
            throw new BusinessException("学生id为:[" + studentId + "]的学生访问课程推荐失败");
        }
        return responseCourseView;
    }

    //获取五门课程给规划师更换
    public List<CourseView> getCoursesForUpdate(Long studentId, int num) {
        ResponseCourseView responseCourseView = null;
        StringBuilder buffer = new StringBuilder(urlConf.getCourse_recommended_service()).
                append("/core/course/calculator/").append(studentId).append("/").append(num);
        return getRecommandedCourseViews(studentId, buffer);
    }

    private List<CourseView> getRecommandedCourseViews(Long studentId, StringBuilder buffer) {
        ResponseCourseView responseCourseView;
        try {
            responseCourseView = restTemplate.getForObject(buffer.toString(), ResponseCourseView.class);
            if (null==responseCourseView||(null == responseCourseView.getData())) {
                throw new BusinessException("获取课程信息失败:" + buffer.toString());
            }
        } catch (Exception ex) {
            throw new BusinessException("学生id为:[" + studentId + "]的学生访问课程推荐失败");
        }
        return responseCourseView.getData();
    }

    //url:http://192.168.66.176:8083/course/recommend/{user_id}/{需要获取的课程数量}
    public CourseView getCourseByService(Service service) throws BoxfishException {
        CourseView courseView = null;
        ResponseCourseView responseCourseView = getCoursesByStudentId(service.getStudentId());
        courseView = responseCourseView.getData().get(0);
        return courseView;
    }

    public List<CourseView> getCourseViewsByService(Service service) throws BoxfishException {
        ResponseCourseView responseCourseView = getCoursesByStudentId(service.getStudentId());
        return responseCourseView.getData();
    }

    public List<CourseView> getCoursesByServiceAndWorkorders(Service service, List<WorkOrder> workOrders) throws BoxfishException {
        ResponseCourseView responseCourseView = getCoursesByStudentId(service.getStudentId());
        return responseCourseView.getData();
    }

    public List<Service> findByOrderId(Long orderId) {
        return jpa.findByOrderId(orderId);
    }

    public Service findTop1ByOrderIdAndSkuId(Long orderId, Long skuId) {
        return jpa.findTop1ByOrderIdAndSkuId(orderId, skuId);
    }

//    public Service findTop1ByOrderIdAndComboType(Long orderId, String comboType) {
//        return jpa.findTop1ByOrderIdAndComboType(orderId, comboType);
//    }

    public List<Service> findByOrderIdAndProductType(Long orderId, Integer productType) {
        return jpa.findByOrderIdAndProductType(orderId, productType);
    }

    public Service findByIdForUpdate(Long id) {
        return jpa.findByIdForUpdate(id);
    }

    public JsonResultModel getAmountofSurplus(List<Long> ids) {
        return null;
    }

    public List<Service> findAllServicesByUser(Long studentId, Long skuIdPlanner, Long skuIdAnswer) {
        return jpa.findAllServicesByUser(studentId, skuIdPlanner, skuIdAnswer);
    }

    public List<Service> findServicesByUserAndCond(Long studentId, String orderCode, Long skuIdPlanner, Long skuIdAnswer) {
        return jpa.findServicesByUserAndCond(studentId, orderCode, skuIdPlanner, skuIdAnswer);
    }

    //通知订单中心修改订单状态
    public void notifyOrderUpdateStatus(WorkOrder workOrder, Integer status) {
        List<Service> services=this.findByOrderId(workOrder.getService().getOrderId());
        int leftAmount=services.stream().collect(Collectors.summingInt(Service::getAmount));
        if(leftAmount>0){
            logger.debug("@notifyOrderUpdateStatus#{}服务次数大于0,不处理,鱼卡[{}]",workOrder.getId(),workOrder.getId(),workOrder.getId());
            return;
        }
        logger.info("@notifyOrderUpdateStatus#{}向订单中心发起订单完成消息,鱼卡[{}],订单[{}]",workOrder.getId(),workOrder.getId(),workOrder.getOrderId());
        Map param = Maps.newHashMap();
        param.put("id", workOrder.getOrderId());
        param.put("status", status);
        rabbitMqSender.send(param, QueueTypeEnum.NOTIFY_ORDER);
    }

    public void notifyOrderUpdateStatus(Long orderId, Integer status) {
        Map param = Maps.newHashMap();
        param.put("id",orderId.toString());
        param.put("status",status.toString());
        rabbitMqSender.send(param, QueueTypeEnum.NOTIFY_ORDER);
    }

    @Transactional
    public void order2ServiceAndWorkOrder(OrderForm orderView){
        try {
            StopWatch stopWatch=new StopWatch();
            stopWatch.start();
            logger.info("@order2ServiceAndWorkOrder*****订单:[{}]转换服务,答疑单,鱼卡开始***** 订单信息[{}]", orderView.getId(), JacksonUtil.toJSon(orderView));
//            订单转服务
            order2Service(orderView);
//            规划服务已不再有
//            order2PlanWorkOrder(orderView);
//            答疑服务为内置,不再需要
//            order2AnswerWorkOrder(orderView);
            //尝试粗粒度通知等待结果

            stopWatch.stop();
            logger.info("*****订单:[{}]转换服务,答疑单,鱼卡成功*****;耗时:[{}]", orderView.getId(),stopWatch.getTotalTimeSeconds());
        } catch (Exception ex) {
            logger.error("订单:[{}]转换为服务失败", orderView.getId(), ex);
//            throw new BusinessException("订单转换为服务失败");
        }
    }

    @Transactional
    //此处需要使用select for update对service的更新记录加锁
    public void decreaseService(WorkOrder workOrder,CourseSchedule courseSchedule,Integer status) throws BoxfishException {
        //从service表中减去已经上课的数量
        Service service = workOrder.getService();
        logger.info("@decreaseService:开始对服务次数进行减操作,操作的鱼卡号[{}],服务号[{}],服务初始数量[{}],还剩数量[{}],触发操作的状态为[{}],状态描述是[{}]",
                workOrder.getId(),service.getId(),service.getOriginalAmount(),service.getAmount(),status,FishCardStatusEnum.getDesc(status));
        if (null == service) {
            logger.error("@decreaseService:鱼卡[{}]无对应的服务",workOrder.getId());
            throw new BusinessException("订单无对应的服务");
        }
        //使用select for update为记录加锁
        service=findByIdForUpdate(service.getId());
        //此处是否是减去1?
        if(service.getAmount()>0) {
            if (null == workOrder.getMakeUpSeq() || 0 == workOrder.getMakeUpSeq()) {
                service.setAmount(service.getAmount() - 1);
            }
        }
        else{
            logger.error("@decreaseService,服务出现不够减情况,需要排查问题service[{}],鱼卡[{}]",service.getId(),workOrder.getId());
        }
        service.setUpdateTime(new Date());
        save(service);

        //修改WorkOrder的状态
        workOrder.setUpdateTime(new Date());
        workOrder.setIsCourseOver((short)1);
        if(status!=FishCardStatusEnum.STUDENT_ABSENT.getCode()) {
            workOrder.setActualEndTime(new Date());
        }
        workOrder.setStatus(status);

        courseSchedule.setUpdateTime(new Date());
        courseSchedule.setStatus(workOrder.getStatus());
        workOrderService.save(workOrder);
        courseScheduleService.save(courseSchedule);
    }

    /**
     * 订单转换为服务
     * @param orderView
     * @throws Exception
     */
    private void order2Service(OrderForm orderView) throws Exception {
        //订单中的商品列表
        String orderRemark = orderView.getOrderRemark();
        ProductCombo productCombo = objectMapper.readValue(orderRemark, ProductCombo.class);
        //服务信息容器
        Map<String, Service> serviceHashMap = new HashMap<>();
        List<Service> services = new ArrayList<>();

        // 混合型套餐特殊处理,合并为一个service
        boolean isOverAll = Objects.equals(productCombo.getComboType(), ComboTypeToRoleId.OVERALL);

        productCombo.getComboDetails().forEach( productComboDetail -> {
            // 改成用comboCode作为分组字段
            String key = productComboDetail.getComboCode();
            if(StringUtils.isEmpty(key)) {
                if(isOverAll) {
                    key = productCombo.getComboType().name();
                } else {
                    key = productComboDetail.getTutorType().name();
                }

            }
            Service service = serviceHashMap.get(key);
            if(service != null) {
                //增加有效期,数量
                setServiceExistedSpecs(service, productComboDetail, orderView);
            } else {
                service = getServiceByOrderView(orderView, productComboDetail, productCombo, isOverAll);
                services.add(service);
                serviceHashMap.put(key, service);
            }
        });

        // service的开始日期,结束日期设置
        addValidTimeForServices(services);
        save(services);

        for (Service service:services){
            logger.info("订单[{}],保存服务[{}]成功",orderView.getId(),service.getId());
            if (Objects.equals(service.getProductType(),1002)){
                logger.info("@order2Service2 购买点评次数,通知跟单系统");
                syncCommentCard2SystemService.syncCommentCard2System(service.getId(),CommentCardStatus.UNASKED.getCode(),null);
            }
        }
        logger.info("@order2Service 购买点评次数,设置首页点评次数");
        commentTeacherAppServiceX.findHomeComment(orderView.getUserId());

    }

    //根据订单生成服务列表
//    private void order2Service1(OrderForm orderView) throws Exception {
//        //订单中的商品列表 TODO
//        List<OrderDetailView> orderProducts = null;
//                //List<OrderDetailView> orderProducts = orderView.getOrderDetails();
//        Iterator<OrderDetailView> productsIterator = orderProducts.iterator();
//        List<Service> services = new ArrayList<>();
//        //服务信息容器
//        Map<Long, Service> serviceHashMap = new HashMap<>();
//
//        //遍历订单中的商品列表
//        while (productsIterator.hasNext()) {
//            //获取其中一个商品
//            OrderDetailView orderDetailView = productsIterator.next();
//            ProductSKUView productSKUView = skuDesc2ProductHasSKUViews(orderDetailView);
//            //如果存在相同类型的产品,则叠加
//            if (null != serviceHashMap.get(productSKUView.getId())) {
//                Service service = serviceHashMap.get(productSKUView.getId());
//                //增加有效期,数量
//                setServiceExistedSpecs(service, orderDetailView, productSKUView);
//            } else {
//                Service service = getServiceByOrderView(orderView, orderDetailView, productSKUView);
//                services.add(service);
//                serviceHashMap.put(productSKUView.getId(), service);
//            }
//        }
//        // service的开始日期,结束日期设置
//        addValidTimeForServices(services);
//        save(services);
//        for (Service service:services){
//            logger.info("订单[{}],保存服务[{}]成功",orderView.getId(),service.getId());
//        }
//    }

    private ProductSKUView skuDesc2ProductHasSKUViews(OrderDetailView orderDetailView) throws Exception {
        return JSONObject.parseObject(orderDetailView.getProductInfo(), ProductSKUView.class);
    }

    private void addValidTimeForServices(List<Service> services) {
        LocalDate now = LocalDate.now();
        for (Service service : services) {
            service.setStartTime(DateUtil.convertToDate(now));
            // 从购买当天0点算起, 到过期的第一天0点为止
            service.setEndTime(DateUtil.convertToDate(now.plusDays(service.getValidityDay() + 1)));
            logger.info("订单[{}]生成服务类型[{}]成功]", service.getOrderId(), service.getSkuName());
        }
    }


    private void setServiceExistedSpecs(Service service, ProductComboDetail productComboDetail, OrderForm orderForm) {
        service.setOriginalAmount(service.getOriginalAmount() + productComboDetail.getSkuAmount());
        service.setAmount(service.getOriginalAmount());
        // TODO 长期有效service.getValidityDay() + serviceSKU.getValidDay()
        if(Objects.isNull(orderForm)) {
            service.setValidityDay(365 * 2);
        } else {
            service.setValidityDay(orderForm.getValidDays());
        }
    }

    private Service getServiceByOrderView(OrderForm orderView, ProductComboDetail productComboDetail,
                                          ProductCombo productCombo, boolean isOverAll) throws BoxfishException {
        Service service = new Service();
        service.setStudentId(orderView.getUserId());
        service.setOrderId(orderView.getId());
        service.setOriginalAmount(productComboDetail.getSkuAmount());
        service.setAmount(service.getOriginalAmount());
        service.setSkuId(productCombo.getId());
        // 设置用户会员类型
        service.setUserType(UserTypeEnum.getUserTypeByComboCode(productCombo.getComboCode()).type());
        // 课程类型
        if(isOverAll) {
            service.setTutorType(TutorType.MIXED.name());
        } else {
            service.setTutorType(productComboDetail.getTutorType().name());
        }
        // 课程类型 兼容老版本
        service.setTeachingType(productCombo.getComboType().getValue());
        // 产品类型
        service.setProductType(productComboDetail.getProductCode());
        service.setComboType(productCombo.getComboType().name());
        service.setCreateTime(new Date());
        service.setOrderCode(orderView.getOrderCode());
        service.setCoursesSelected(0);
        service.setValidityDay(365);
        service.setOrderChannel(orderView.getOrderChannel().name());

        productCombo.getComboCode();

        List<ComboDurations> durations = productCombo.getComboDurations();
        if(CollectionUtils.isNotEmpty(durations)) {
            for(int i = 0, size = durations.size(); i < size;  i++) {
                ComboDurations duration = durations.get(i);
                if(Objects.equals(duration.getComboCode(), productComboDetail.getComboCode())) {
                    service.setComboCycle(duration.getDurationWeeks());
                    service.setCountInMonth(duration.getPerWeekCourseCount());
                    break;
                }
            }
        } else {
            service.setComboCycle(productCombo.getComboCycle());
        }
        return service;
    }

    public Service findTop1ByOrderId(Long orderId){
        return jpa.findTop1ByOrderId(orderId);
    }

    @Transactional
    public void saveWorkorderAndCourse(WorkOrder workOrder){
        workOrderService.save(workOrder);
        CourseSchedule courseSchedule=new CourseSchedule();
        courseSchedule.setStatus(workOrder.getStatus());
        courseSchedule.setStudentId(workOrder.getStudentId());
        courseSchedule.setCourseId(workOrder.getCourseId());
        courseSchedule.setCourseName(workOrder.getCourseName());
        courseSchedule.setCourseType(workOrder.getCourseType());
        courseSchedule.setCreateTime(workOrder.getCreateTime());
        courseSchedule.setTimeSlotId(workOrder.getSlotId());
        courseSchedule.setClassDate(workOrder.getStartTime());
        courseSchedule.setStartTime(workOrder.getStartTime());
        courseSchedule.setWorkorderId(workOrder.getId());
        courseSchedule.setTeacherId(workOrder.getTeacherId());
        courseScheduleService.save(courseSchedule);
//        ScheduleCourseInfo courseInfo=scheduleCourseInfoService.queryByCourseIdAndScheduleType(courseSchedule.getCourseId(), ScheduleTypeEnum.TRIAL.getDesc());
        TrialCourse trialCourse=scheduleCourseInfoService.queryByCourseIdAndScheduleType(courseSchedule.getCourseId(), ScheduleTypeEnum.TRIAL.getDesc());
        ScheduleCourseInfo scheduleCourseInfo=new ScheduleCourseInfo();
        scheduleCourseInfo.setCourseType(trialCourse.getCourseType());
        scheduleCourseInfo.setThumbnail(trialCourse.getThumbnail());
        scheduleCourseInfo.setName(trialCourse.getName());
        scheduleCourseInfo.setDifficulty(trialCourse.getDifficulty());
        scheduleCourseInfo.setCourseId(trialCourse.getCourseId());
        scheduleCourseInfo.setLastModified(trialCourse.getLastModified());
        scheduleCourseInfo.setWorkOrderId(workOrder.getId());
        scheduleCourseInfo.setScheduleId(courseSchedule.getId());
        scheduleCourseInfoService.save(scheduleCourseInfo);
    }

    public Map<String, Integer> getForeignCommentServiceCount(long studentId) {
        List<Service> services = serviceJpaRepository.getForeignCommentServiceCount(
                studentId, ProductType.COMMENT.value());
        Integer originalAmount = services.stream().reduce(
                0,
                (total, service) -> total + service.getOriginalAmount(),
                Integer::sum
        );
        Integer amount = services.stream().reduce(
                0,
                (total, service) -> total + service.getAmount(),
                Integer::sum
        );
        Map<String, Integer> countMap = Maps.newHashMap();
        countMap.put("originalAmount", originalAmount);
        countMap.put("amount", amount);
        return countMap;
    }

    public boolean haveAvailableForeignCommentService(long studentId) {
        return serviceJpaRepository.getAvailableForeignCommentServiceCount(
                studentId, ProductType.COMMENT.value()) > 0;
    }

    @Transactional
    public Optional<Service> findFirstAvailableForeignCommentService(long studentId) {
        Page<Service> servicePage = serviceJpaRepository.getFirstAvailableForeignCommentService(
                studentId, ProductType.COMMENT.value(), new PageRequest(0, 1));
        return CollectionUtils.isEmpty(servicePage.getContent()) ?
                Optional.empty() : Optional.of(servicePage.getContent().get(0));
    }

//    @Transactional
//    public void saveWorkorderAndCourse(WorkOrder workOrder,CourseSchedule courseSchedule,TrialLectureModifyParam trialLectureModifyParam){
//        workOrderService.save(workOrder);
//        courseSchedule.setStatus(workOrder.getStatus());
//        courseSchedule.setStudentId(workOrder.getStudentId());
//        courseSchedule.setCourseId(workOrder.getCourseId());
//        courseSchedule.setCourseName(workOrder.getCourseName());
//        courseSchedule.setCourseType(workOrder.getCourseType());
//        courseSchedule.setCreateTime(workOrder.getCreateTime());
//        courseSchedule.setTimeSlotId(workOrder.getSlotId());
//        courseSchedule.setClassDate(workOrder.getStartTime());
//        courseSchedule.setWorkorderId(workOrder.getId());
//        courseSchedule.setTeacherId(workOrder.getTeacherId());
//        courseScheduleService.save(courseSchedule);
//        scheduleCourseInfoService.updateTrialScheduleInfo(trialLectureModifyParam,workOrder,courseSchedule);
//    }

    @Transactional
    public void deleteWorkOrderAndSchedule(WorkOrder workOrder,CourseSchedule courseSchedule){
        workOrderService.delete(workOrder);
        courseScheduleService.delete(courseSchedule);
    }


    /**************** 兼容历史版本代码******************/
    /**
     * 每周选几次课
     * @param service
     * @return
     */
    public int getNumPerWeek(Service service) {
        // 兼容之前的逻辑
        if (service.getComboCycle() == -1) {
//            return service.getAmount();
            return 2;
        } else {
            // 每周选课次数
            int numPerWeek = service.getAmount() / service.getComboCycle();
            return numPerWeek == 0 ? 1 : numPerWeek;
        }
    }

    /**
     * 几周上完
     * @param service
     * @return
     */
    public int getLoopOfWeek(Service service) {
        // 兼容之前的业务逻辑
        if (service.getComboCycle() == -1) {
//            return 1;
            return 4;
        } else {
            int comboCycle = service.getComboCycle();
            return (getNumPerWeek(service) -1 + service.getAmount()) / getNumPerWeek(service);
//            return service.getAmount() % comboCycle == 0
//                    ? comboCycle : service.getAmount() / getNumPerWeek(service);
        }
    }


    /*********兼容老版本*************/
    public Service findTop1ByOrderIdAndComboType(Long orderId, String comboType) {
        return jpa.findTop1ByOrderIdAndComboType(orderId, comboType);
    }

    /**
     * 获取首次鱼卡时间
     * @param workOrder
     * @return
     */
    @Transactional
    public Date getFirstTimeOfService(WorkOrder workOrder){
        //获取首次鱼卡开始时间
        WorkOrder fistWorkOrder = null;

        Date beginDate = workOrder.getService().getFirstTime();

        if(null==beginDate){
            //获取该订单所有鱼卡信息
            List<WorkOrder> workOrders = validateAndGetWorkOrders(workOrder.getOrderId());
            fistWorkOrder = getFirstOrder(workOrders);
            beginDate = fistWorkOrder.getStartTime();
            logger.info("getFirstTimeOfService====>fishcardId[{}]====>开始时间[{}]",fistWorkOrder.getId(),   fistWorkOrder.getStartTime());
            //更新service的首次鱼卡开始时间
            Service  service =this.findByIdForUpdate(fistWorkOrder.getService().getId());
            service.setFirstTime(beginDate);
            this.save(service);
        }
        logger.info("getFirstTimeOfService====>fishcardId[{}]====>开始时间[{}]",workOrder.getId(),   workOrder.getStartTime());
        return beginDate;
    }

    /**
     * 返回首次时间
     * @param workOrders
     * @return
     */
    private WorkOrder getFirstOrder(List<WorkOrder> workOrders){
        workOrders.sort(new Comparator<WorkOrder>() {
            @Override
            public int compare(WorkOrder o1, WorkOrder o2) {
                if(o1.getStartTime().after(o2.getStartTime())){
                    return 0;
                }
                return -1;
            }
        });

        return workOrders.get(0);
    }

    /**
     * 根据鱼卡获取该订单所有鱼卡
     * @param workOrderId
     * @return
     */
    private List<WorkOrder> validateAndGetWorkOrders(Long workOrderId){
        List<WorkOrder> list =  null;
        try {
            list = workOrderService.getAllWorkOrdersByOrderId(workOrderId);
            if (org.springframework.util.CollectionUtils.isEmpty(list)) {
                throw new BusinessException("数据不合法");
            }
        } catch (Exception ex) {
            throw new BusinessException("传入鱼卡的id在服务端无对应服务");
        }
        return list;
    }

    private List<Service> getServiceSelectedStatus(Long studentId,Integer coursesSelected){
        return jpa.getServiceSelectedStatus(studentId,coursesSelected);
    }

    public List<Service> getUnselectedService(Long studentId, List<ComboTypeEnum> comboTypeEnums, TutorTypeEnum tutorTypeEnum,Integer selectedFlag){
        return jpa.findByStudentIdAndComboTypeInAndTutorTypeAndCoursesSelectedAndProductType(studentId, workOrderService.enums2StringAray(comboTypeEnums),tutorTypeEnum.toString(),selectedFlag,ProductType.TEACHING.value());
    }

    public List<Service> getUnselectedService(Long studentId,ComboTypeEnum comboTypeEnum,Integer selectedFlag){
        return jpa.findByStudentIdAndComboTypeAndCoursesSelectedAndProductType(studentId, comboTypeEnum.toString() ,selectedFlag,ProductType.TEACHING.value());
    }

    public List<Service> getUnselectedService(Long studentId,List<ComboTypeEnum> comboTypeEnums,Integer selectedFlag){
        return jpa.findByStudentIdAndComboTypeInAndCoursesSelectedAndProductType(studentId, workOrderService.enums2StringAray(comboTypeEnums) ,selectedFlag,ProductType.TEACHING.value());
    }
}
