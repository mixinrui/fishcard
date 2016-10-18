package com.boxfishedu.workorder.common.intecerptor;

import com.boxfishedu.workorder.common.util.ClassTypeJudge;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by hucl on 16/6/3.
 */
@Aspect
@Component
public class MethodInvokeInterceptor {
    //本地异常日志记录对象
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String interceptLogUrl="execution(* com.boxfishedu.workorder.web.controller.*.*.*(..))";

    /**
     * 前置通知 用于拦截Controller层记录用户的操作
     *
     * @param joinPoint 切点
     */
    @Before(interceptLogUrl)
    public void doBefore(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String className = joinPoint.getTarget().getClass().getName();
        String lastName = className.substring(className.lastIndexOf("."));
        logger.debug("@>>>>>>>>>>>>>>>>>类{}#########方法:{}#########url:{}", lastName, joinPoint.getSignature().getName(), request.getRequestURI());
        Object[] args = joinPoint.getArgs();
        if (null != args && args.length > 0) {
            for (int i = 0; i < args.length; i++) {

                if(null!=args[i] && args[i].getClass().getName().toLowerCase().contains("response")){
                    /**
                     * 防止  传入参数HttpServletResponse  logger.info response  flush两次
                     *
                     * java.lang.IllegalStateException :getWriter() has already been called for this response
                     */
                }else if(null==args[i]||ClassTypeJudge.isBaseDataType(args[i].getClass()) ){
                    logger.debug(">>>参数[{}]: {}", i, args[i]);
                }
                else {
                    logger.debug(">>>参数[{}]: {}", i, JacksonUtil.toJSon(args[i]));
                }
            }
        }
    }

    @AfterReturning(pointcut = interceptLogUrl,returning = "returnValue")
    public void log(JoinPoint joinPoint, Object returnValue) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String className = joinPoint.getTarget().getClass().getName();
        String lastName = className.substring(className.lastIndexOf("."));
        if (!ClassTypeJudge.isBaseDataType(returnValue.getClass())) {
            logger.debug("@<<<<<<<<<<<<<<<<<类{}#########方法:{}#########url:{}##########返回值:{}", lastName, joinPoint.getSignature().getName(), request.getRequestURI(), JacksonUtil.toJSon(returnValue));
        } else {
            logger.debug("@<<<<<<<<<<<<<<<<<类{}#########方法:{}#########url:{}##########返回值:{}", lastName, joinPoint.getSignature().getName(), request.getRequestURI(), returnValue);
        }
    }
}
