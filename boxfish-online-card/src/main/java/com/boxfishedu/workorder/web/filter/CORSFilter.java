package com.boxfishedu.workorder.web.filter;

import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by LuoLiBing on 16/9/2.
 */
@WebFilter(urlPatterns ={
        // 带contextPath的拦截
        "/fishcard/service/backend/*"})
public class CORSFilter extends OncePerRequestFilter {

    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final int order = Ordered.HIGHEST_PRECEDENCE;

    static void handle(final HttpServletRequest request, final HttpServletResponse response) {
        response.addHeader(ACCESS_CONTROL_ALLOW_METHODS, "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
        response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.addHeader(ACCESS_CONTROL_ALLOW_HEADERS, "x-be-product,Content-Type");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        handle(request, response);
        filterChain.doFilter(request, response);
    }
}

