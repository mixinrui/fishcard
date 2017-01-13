package com.boxfishedu.workorder.common.login;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
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
import java.util.Set;

/**
 * 退单filter验证
 * {
 * token:返回调用端 用作访问凭证
 * realtoken: 每次操作的时间
 * }
 * Created by jiaozijun on 16/6/23.
 */
@SuppressWarnings("ALL")
@WebFilter(urlPatterns = {
        "/fishcard/backend/backorder/*",
        "/fishcard/backend/makeup/*",
        "/fishcard/backend/fishcard/*",

        "/backend/backorder/*",
        "/backend/makeup/*",
        "/backend/fishcard/*",
        "/service/backend/*"

        })
public class BackOrderFilter extends OncePerRequestFilter {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LoginService loginService;

    @Value("${parameter.allow_test}")
    private Boolean allowTest;

    @Value("spring.profiles.active")
    private String active;

    @Override
    @Profile({"local_hucl","product","local","development","development_new","demo","pretest"})
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    logger.info("doFilterInternal");
        if(allowTest){
            if(request.getParameter("test")!=null&&request.getParameter("test").equals("true")) {
                logger.info("允许测试api的接口,不需要验证accesstoken");
                filterChain.doFilter(request, response);
                return;
            }
        }
        String url = request.getRequestURI();

        logger.info("后台登陆url={}"+url);
        String token = request.getParameter("token");
        String URI  = request.getRequestURI();
        // 获取token
        if(StringUtils.isEmpty(token) || StringUtils.isEmpty(token.trim())){
            errorTokenHandle(response, String.format("token为空,token:[%s]", token),HttpStatus.SC_UNAUTHORIZED);
            return;
        } else {
            if(!loginService.checkToken(token)){
                errorTokenHandle(response, String.format("token无效,token:[%s]", token),HttpStatus.SC_UNAUTHORIZED);
                return;
            }else if(URI!=null && URI.endsWith("item") &&!loginService.checkURI(token,URI)){
                logger.info("URI无效,token:[{}]",token);
                errorTokenHandle(response,String.format("您没有权限访问"),HttpStatus.SC_BAD_REQUEST);
                return;
            }
        }



        logger.debug("请求接口 = [{}],token = [{}]", request.getRequestURI(), token);
        Map<String, Object> params = Maps.newHashMap();
        AuthorRequestWrapper authorRequestWrapper = new AuthorRequestWrapper(request, params);
        filterChain.doFilter(authorRequestWrapper, response);
    }

    public void errorTokenHandle(HttpServletResponse response, String message,Integer code) throws IOException {
        response.setStatus(HttpStatus.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        /**
         * 跨域问题
         */
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers","Content-Type");
        JsonResultModel jsonResultModel = JsonResultModel.newJsonResultModel(null);
        jsonResultModel.setReturnCode(code);
        jsonResultModel.setReturnMsg(message);
        String json = new Gson().toJson(jsonResultModel);
        try(PrintWriter out = response.getWriter()){
            out.write(json);
            //out.flush();
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

