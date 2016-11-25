package com.boxfishedu.card.fixes.web.filter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by LuoLiBing on 16/11/15.
 */
@Component
public class TokenFilter extends OncePerRequestFilter {

    private final static Logger logger = LoggerFactory.getLogger(TokenFilter.class);

    @Value("${recommend.token}")
    private String token;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String t = request.getParameter("token");
        if(StringUtils.equals(token, t)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            logger.warn("403 forbidden!");
        }
    }
}
