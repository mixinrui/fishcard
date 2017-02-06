package com.boxfishedu.workorder.dao.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by ansel on 2017/2/6.
 */
@Repository
public class CommentCardOrderJdbc {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<Map<String,Object>> closeCommentCardOrder(){
        return jdbcTemplate.queryForList("select distinct(order_id) as id from service" +
                " where product_type = 1002 and amount = 0" +
                " and order_id in (" +
                " select distinct(order_id) from comment_card" +
                " where order_id not in (" +
                " select distinct(order_id) from comment_card where status < 400))");
    }
}
