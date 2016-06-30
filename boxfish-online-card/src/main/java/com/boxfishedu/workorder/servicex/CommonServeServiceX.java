package com.boxfishedu.workorder.servicex;

import com.boxfishedu.workorder.common.exception.UnauthorizedException;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.service.ServeService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by hucl on 16/3/31.
 */
@org.springframework.stereotype.Service
public class CommonServeServiceX {
    @Autowired
    private ServeService serveService;

    private org.slf4j.Logger logger= LoggerFactory.getLogger(this.getClass());
    /**
     * 免费体验的天数
     */
    @Value("${parameter.allow_test}")
    private Boolean allowTest;

    public JsonResultModel getAmountofSurplus(List<Long> ids){
        return serveService.getAmountofSurplus(ids);
    }

    public void checkToken(Long appSendedId, Long userId) {
        if(allowTest){
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            if(request.getParameter("test")!=null&&request.getParameter("test").equals("true")) {
                HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                logger.debug("允许测试api的接口,不需要验证accesstoken");
                return;
            }
        }
        if(appSendedId != null && !appSendedId.equals(userId)){
            throw new UnauthorizedException("请用正确的账号登录操作");
        }
    }
}
