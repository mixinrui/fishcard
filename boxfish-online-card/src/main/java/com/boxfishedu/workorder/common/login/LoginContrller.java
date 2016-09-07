package com.boxfishedu.workorder.common.login;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 登陆contrller
 * Created by jiaozijun on 16/6/22.
 */

@CrossOrigin
@RestController
@RequestMapping("/backend/login")
public class LoginContrller {

    //本地异常日志记录对象
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LoginService loginService;


    /**
     * 登陆
     * @param userInfo
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/toLogin", method = RequestMethod.POST)
    public JsonResultModel toLogin(UserInfo userInfo) throws Exception{
        return loginService.login(userInfo);
    }

    /**
     * 退出登陆
     * @param token
     * @return
     * @throws Exception
     */

    @RequestMapping(value = "/tologoff", method = RequestMethod.GET)
    public JsonResultModel tologoff(String token) throws Exception{
        return loginService.logoff(token);
     }

    /** 初始化用户 **/
    @RequestMapping(value = "/inituser", method = RequestMethod.POST)
    public JsonResultModel initUser(@RequestBody  UserInfo userInfo) throws Exception{
        return  loginService.initData(userInfo);
    }

    /** 更改密码 **/
    @RequestMapping(value = "/changepassword", method = RequestMethod.POST)
    public JsonResultModel changePassword(UserInfo userInfo) throws Exception{
        return  loginService.changePassword(userInfo);
    }


    @RequestMapping(value = "/initdata", method = RequestMethod.GET)
    public JsonResultModel initdata() throws Exception{
        loginService.initData();
        return JsonResultModel.newJsonResultModel(null);
    }


    /**
     * 外教点评验证token有效性
     * @param token
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/checktoken/{token}/out", method = RequestMethod.GET)
    public JsonResultModel checktoken(@PathVariable("token") String token) throws Exception{
        if(!loginService.checkToken(token)){
            return JsonResultModel.newJsonResultModel("error");
        }
        logger.info("checktoken:tokenis[{}]",token);
        return JsonResultModel.newJsonResultModel("ok");
    }



}
