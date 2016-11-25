package com.boxfishedu.card.fixes.entity.mongo;

import com.boxfishedu.card.fixes.entity.jpa.WorkOrder;
import com.boxfishedu.card.fixes.entity.jpa.WorkOrderJpaRepository;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

@Component
public class ScheduleCourseInfoMorphiaRepository extends BaseMorphiaRepository<ScheduleCourseInfo> {

    @Value("${recommend.url.courseInfo}")
    private String url;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    private LongAdder longAdder = new LongAdder();

    public void updateCourseInfos() {

        Query<ScheduleCourseInfo> query = datastore.createQuery(ScheduleCourseInfo.class);
        List<ScheduleCourseInfo> list = query.asList();
        System.out.println(list.size());
        list.parallelStream().forEach(this::updateCourseInfo);
    }


    public void updateCourseInfosByDateRange(Date from, Date to) {
        List<WorkOrder> workOrders = workOrderJpaRepository.findByStartTimeRange(from, to);
        workOrders.parallelStream().forEach(workOrder -> {
            ScheduleCourseInfo courseInfo = findByWorkOrderId(workOrder.getId());
            if(courseInfo != null) {
                updateCourseInfo(courseInfo);
            }
        });
    }

    public void updateCourseEnglishNames() {
        Query<ScheduleCourseInfo> query = datastore.createQuery(ScheduleCourseInfo.class);
        List<ScheduleCourseInfo> list = query.asList();
        System.out.println(list.size());
        list.parallelStream().forEach(this::updateCourseEnglishName);
    }

    public void updateCourseInfo(ScheduleCourseInfo sci) {
        System.out.println("before update= [{" + sci + "}]");
        if(StringUtils.isNotBlank(sci.getCourseId())) {
            try {
                Map courseMap = new RestTemplate().getForObject(
                        String.format(url, sci.getCourseId()), Map.class);
                if(!Objects.isNull(courseMap) && !Objects.isNull(courseMap.get("englishName"))) {
                    sci.setEnglishName((String) courseMap.get("englishName"));
                } else {
                    sci.setEnglishName("");
                }
                sci.setName((String) courseMap.get("courseName"));
                sci.setCourseType((String) courseMap.get("type"));
                sci.setDifficulty((String) courseMap.get("difficulty"));
                sci.setLastModified((long) courseMap.get("lastModified"));
                sci.setPublicDate(courseMap.get("publicDate").toString());
                datastore.save(sci);
                System.out.println("after  update= [{" + sci + "}]");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void updateCourseEnglishName(ScheduleCourseInfo sci) {
        System.out.println("before update= [{" + sci + "}]");
        if(StringUtils.isNotBlank(sci.getCourseId())) {
            try {
                Map courseMap = new RestTemplate().getForObject(
                        String.format(url, sci.getCourseId()), Map.class);
                if(!Objects.isNull(courseMap) && !Objects.isNull(courseMap.get("englishName"))) {
                    sci.setEnglishName((String) courseMap.get("englishName"));
                } else {
                    sci.setEnglishName("");
                }
                datastore.save(sci);
                System.out.println("after  update= [{" + sci + "}]");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void updateCourseDifficultys() {
        Query<ScheduleCourseInfo> query = datastore.createQuery(ScheduleCourseInfo.class);
        List<ScheduleCourseInfo> list = query.asList();
        System.out.println("updateCount = " + list.size());
        list.parallelStream().forEach(this::updateCourseDifficulty);
        System.out.println("modifyCount = " + longAdder.sumThenReset());
    }

    public ScheduleCourseInfo findByWorkOrderId(Long workOrderId) {
        Query<ScheduleCourseInfo> query = datastore.createQuery(ScheduleCourseInfo.class);
        query.criteria("workOrderId").equal(workOrderId);
        return query.get();
    }

    public List<ScheduleCourseInfo> findByCourseId(String courseId) {
        Query<ScheduleCourseInfo> query = datastore.createQuery(ScheduleCourseInfo.class);
        query.criteria("courseId").equal(courseId);
        return query.asList();
    }

    private void updateCourseDifficulty(ScheduleCourseInfo sci) {
        String difficulty = sci.getDifficulty();
        if(StringUtils.isNotBlank(difficulty)) {
            if(difficulty.trim().length() == 1) {
                System.out.println("before update= [{" + sci + "}]");
                sci.setDifficulty("LEVEL_" + difficulty);
                datastore.save(sci);
                longAdder.increment();
                System.out.println("after  update= [{" + sci + "}]");
            }
        }
    }
}
