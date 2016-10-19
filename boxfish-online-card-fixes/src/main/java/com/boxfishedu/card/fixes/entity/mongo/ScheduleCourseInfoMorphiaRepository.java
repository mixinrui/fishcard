package com.boxfishedu.card.fixes.entity.mongo;

import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class ScheduleCourseInfoMorphiaRepository extends BaseMorphiaRepository<ScheduleCourseInfo> {

    public void updateCourseInfos() {
        String url = "http://base.boxfish.cn/course/info/%s";
        Query<ScheduleCourseInfo> query = datastore.createQuery(ScheduleCourseInfo.class);
        query.and(query.criteria("workOrderId").greaterThan(38206));
        List<ScheduleCourseInfo> list = query.asList();

        System.out.println(list.size());

        for(int i = 0, size = list.size(); i < size; i++) {
            ScheduleCourseInfo sci = list.get(i);
            System.out.println("before update= [{" + sci + "}]");
            if(StringUtils.isNotEmpty(sci.getCourseId())) {
                try {
                    Map courseMap = new RestTemplate().getForObject(
                            String.format(url, list.get(i).getCourseId()), Map.class);
                    sci.setEnglishName(sci.getEnglishName());
                    datastore.save(sci);
                    System.out.println("after  update= [{" + sci + "}]");
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }
        }
    }
}
