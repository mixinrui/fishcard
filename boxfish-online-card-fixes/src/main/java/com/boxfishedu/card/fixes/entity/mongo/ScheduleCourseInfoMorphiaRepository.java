package com.boxfishedu.card.fixes.entity.mongo;

import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class ScheduleCourseInfoMorphiaRepository extends BaseMorphiaRepository<ScheduleCourseInfo> {

    private final static String url = "http://base.boxfish.cn/course/info/%s";

    public void updateCourseInfos() {

        Query<ScheduleCourseInfo> query = datastore.createQuery(ScheduleCourseInfo.class);
        List<ScheduleCourseInfo> list = query.asList();
        System.out.println(list.size());
        list.parallelStream().forEach(this::updateCourseInfo);
    }

    public void updateCourseInfo(Long id) {
        Query<ScheduleCourseInfo> query = datastore.createQuery(ScheduleCourseInfo.class);
        query.criteria("workOrderId").equal(id);
        ScheduleCourseInfo courseInfo = query.get();
        updateCourseInfo(courseInfo);
    }

    private void updateCourseInfo(ScheduleCourseInfo sci) {
        System.out.println("before update= [{" + sci + "}]");
        if(StringUtils.isNotEmpty(sci.getCourseId())) {
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
//                    e.printStackTrace();
            }
        }
    }
}
