package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ansel on 2017/3/16.
 */
@Repository
public class SmallClassJdbc {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String,String>> getPublicAndSmallGroupId(){
        Object[] queryParam = new Object[2];
        LocalDateTime now = LocalDateTime.now();
        queryParam[0] = DateUtil.localDate2Date(now.minusHours(24));
        queryParam[1] = DateUtil.localDate2Date(now.minusHours(0));
        return jdbcTemplate.query("select group_id from small_class where end_time between ? and ?", queryParam, (resultSet, i) -> {
            Map<String,String> groupId = new HashMap();
            groupId.put("groupId",resultSet.getString("group_id"));
            return groupId;
        });
    }
}
