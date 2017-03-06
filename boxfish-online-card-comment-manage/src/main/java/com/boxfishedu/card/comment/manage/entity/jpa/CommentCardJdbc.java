package com.boxfishedu.card.comment.manage.entity.jpa;

import com.boxfishedu.card.comment.manage.entity.mysql.CommentCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by ansel on 2017/3/2.
 */
@Repository
public class CommentCardJdbc {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    JdbcTemplate jdbcTemplate;

    public List getCommentCardByOptions(String strOptions){
        logger.info("###getCommentCardByOptions### use jdbc query comment_cards ... ");
        return jdbcTemplate.queryForList(strOptions);
    }
}
