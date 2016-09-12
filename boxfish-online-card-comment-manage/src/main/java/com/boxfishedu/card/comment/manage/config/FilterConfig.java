package com.boxfishedu.card.comment.manage.config;

import com.boxfishedu.card.comment.manage.service.sdk.CommentCardManageSDK;
import com.boxfishedu.card.comment.manage.web.filter.AuthFilter;
import com.boxfishedu.card.comment.manage.web.filter.CORSFilter;
import com.google.common.collect.Lists;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by LuoLiBing on 16/6/15.
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean corsFilter() {
        FilterRegistrationBean corsFilterBean = new FilterRegistrationBean();
        CORSFilter corsFilter = new CORSFilter();
        corsFilterBean.setFilter(corsFilter);
        corsFilterBean.setOrder(CORSFilter.order);
        corsFilterBean.setUrlPatterns(Lists.newArrayList("/*"));
        return corsFilterBean;
    }

    @Bean
    public FilterRegistrationBean authFilter(CommentCardManageSDK commentCardManageSDK) {
        FilterRegistrationBean authFilterBean = new FilterRegistrationBean();
        AuthFilter authFilter = new AuthFilter(commentCardManageSDK);
        authFilterBean.setFilter(authFilter);
        authFilterBean.setOrder(AuthFilter.order);
        authFilterBean.setUrlPatterns(Lists.newArrayList("/comment/manage/*"));
        return authFilterBean;
    }
}
