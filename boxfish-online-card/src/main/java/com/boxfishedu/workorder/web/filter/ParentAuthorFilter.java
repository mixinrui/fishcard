package com.boxfishedu.workorder.web.filter;

import com.boxfishedu.workorder.common.exception.UnauthorizedException;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.apache.commons.collections.MapUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 家长帮拦截器
 */
@SuppressWarnings("ALL")
@WebFilter(urlPatterns ={
        "/service/parent/*"})
public class ParentAuthorFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${parameter.allow_test}")
    private Boolean allowTest;

    @Autowired
    ParentRelationGetter parentRelationGetter;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if(allowTest){
            if(request.getParameter("test")!=null&&request.getParameter("test").equals("true")) {
                logger.info("允许测试api的接口,不需要验证accesstoken");
                filterChain.doFilter(request, response);
                return;
            }
        }
        //没有access_token的请求直接返回
        String accessToken = request.getParameter("access_token");
        String studentIdStr = request.getParameter("student_id");
        logger.debug("用户的url:{}",request.getRequestURI());
        if (StringUtils.isEmpty(accessToken)) {
            logger.debug("用户没有带accesstoken");
            errorHandle(response, "", HttpStatus.SC_UNAUTHORIZED);
            return;
        }

        if(StringUtils.isEmpty(studentIdStr)){
            errorHandle(response, "学生id不合法", HttpStatus.SC_UNAUTHORIZED);
            return;
        }

        //将access_token转换为userId,如果返回null则表示用户不存在,直接返回
        Map relationMap= MapUtils.EMPTY_MAP;
        try {
            relationMap = parentRelationGetter.getRelation(Long.parseLong(studentIdStr),accessToken);
        } catch (UnauthorizedException e) {
            errorHandle(response, "", HttpStatus.SC_UNAUTHORIZED);
            return;
        } catch (Exception e) {
            errorHandle(response, "", HttpStatus.SC_BAD_REQUEST);
            return;
        }

        if (MapUtils.isEmpty(relationMap)) {
            errorHandle(response, "", HttpStatus.SC_UNAUTHORIZED);
            return;
        }

        //家长id
        Long suorceId=MapUtils.getLong(relationMap,"source_id");
        //学生id
        Long targetId=MapUtils.getLong(relationMap,"target_id");

        logger.debug("请求接口 = [{}],access_token = [{}],parentId = [{}],studentId = [{}]"
                , request.getRequestURI(), accessToken, suorceId,targetId);

        Map<String, Object> params = Maps.newHashMap();

        params.put("parentId", suorceId);
        params.put("studentId", targetId);

        AuthorRequestWrapper authorRequestWrapper = new AuthorRequestWrapper(request, params);
        filterChain.doFilter(authorRequestWrapper, response);
    }

    public void errorHandle(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        JsonResultModel jsonResultModel = JsonResultModel.newJsonResultModel(null);
        jsonResultModel.setReturnCode(status);
        jsonResultModel.setReturnMsg(message);
        String json = new Gson().toJson(jsonResultModel);
        try(PrintWriter out = response.getWriter()){
            out.write(json);
        }
    }

    private class AuthorRequestWrapper extends HttpServletRequestWrapper {
        private Map<String, String[]> params = Maps.newHashMap();

        AuthorRequestWrapper(HttpServletRequest request) {
            super(request);
            this.params.putAll(request.getParameterMap());
            this.modifyParameterValues();
        }

        //重载一个构造方法
        AuthorRequestWrapper(HttpServletRequest request, Map<String, Object> extendParams) {
            this(request);
            addAllParameters(extendParams);//这里将扩展参数写入参数表
        }

        void modifyParameterValues() {//将parameter的值去除空格后重写回去
            Set<String> set = params.keySet();
            for (String key : set) {
                String[] values = params.get(key);
                values[0] = values[0].trim();
                params.put(key, values);
            }
        }

        @Override
        public String getParameter(String name) {//重写getParameter，代表参数从当前类中的map获取
            String[] values = params.get(name);
            if (values == null || values.length == 0) {
                return null;
            }
            return values[0];
        }

        public String[] getParameterValues(String name) {
            return params.get(name);
        }

        private void addAllParameters(Map<String, Object> otherParams) {
            for (Map.Entry<String, Object> entry : otherParams.entrySet()) {
                addParameter(entry.getKey(), entry.getValue());
            }
        }

        private void addParameter(String name, Object value) {//增加参数
            if (value != null) {
                if (value instanceof String[]) {
                    params.put(name, (String[]) value);
                } else if (value instanceof String) {
                    params.put(name, new String[]{value.toString()});
                } else {
                    params.put(name, new String[]{String.valueOf(value)});
                }
            }
        }
    }
}
