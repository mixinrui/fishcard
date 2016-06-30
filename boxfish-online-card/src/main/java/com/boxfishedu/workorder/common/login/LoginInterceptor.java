package com.boxfishedu.workorder.common.login;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jiaozijun on 16/6/22.
 */
//@Aspect
//@Component
public class LoginInterceptor {
    //本地异常日志记录对象
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LoginService loginService;

    private final String interceptLogUrl="execution(* com.boxfishedu.workorder.web.controller.fishcardcenter.*.*(..))";

    /**
     * 前置通知 用于拦截鱼卡后台Controller登陆控制
     *
     * @param joinPoint 切点
     */
    @Before(interceptLogUrl)
    public void doBefore(JoinPoint joinPoint){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String url = request.getRequestURI();

        boolean flag = false;
        for(String u :FilterUrls.urlLists){
            if(url.indexOf(u)>=0){ // 需要进行登陆验证
                flag= true;
                break;
            }
        }

        if(!flag){
            logger.info("退出访问");
            throw new NoPermissionException();
        }
        logger.info("后台登陆url={}"+url);
        String token = request.getParameter("token");
        // 获取token
        if(StringUtils.isEmpty(token) || StringUtils.isEmpty(token.trim())){
            throw new NoPermissionException();
        } else {
            if(!loginService.checkToken(token)){
                throw new NoPermissionException();
            }
        }
    }



}
