package com.boxfishedu.card.comment.manage.web.filter;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.comment.manage.service.sdk.CommentCardManageSDK;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by LuoLiBing on 16/6/15.
 */
public class AuthFilter extends OncePerRequestFilter {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    public final static int order = Ordered.HIGHEST_PRECEDENCE + 1;

    @Autowired
    private CommentCardManageSDK commentCardManageSDK;

    public AuthFilter(CommentCardManageSDK commentCardManageSDK) {
        this.commentCardManageSDK = commentCardManageSDK;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getParameter("access_token");
        if((token == null) || (!commentCardManageSDK.checkToken(token))) {
            errorToken(response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void errorToken(HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        JsonResultModel jsonResultModel = JsonResultModel.newJsonResultModel();
        jsonResultModel.setReturnCode(HttpStatus.SC_UNAUTHORIZED);
        jsonResultModel.setReturnMsg("无效的token");
        try(PrintWriter out = response.getWriter()){
            out.write(convertToString(jsonResultModel));
        }
    }

    public static String convertToString(Object src) throws JsonProcessingException {
        return objectMapper.writeValueAsString(src);
    }

}
