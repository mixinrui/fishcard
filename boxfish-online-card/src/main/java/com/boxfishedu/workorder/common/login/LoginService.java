package com.boxfishedu.workorder.common.login;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.WorkOrderUser;
import com.boxfishedu.workorder.service.WorkOrderUserService;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;

/**
 * 鱼卡后台登陆逻辑层
 * Created by jiaozijun on 16/6/22.
 */

@Component
public class LoginService {

    //本地异常日志记录对象
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private WorkOrderUserService workOrderUserService;

    /**
     * 从db中加载用户信息 到redis中
     * @param userName
     * @return
     */
    private String userInfoFromDb(String userName)throws  Exception{
        WorkOrderUser workOrderUser =  workOrderUserService.findByUserCodeAndFlag(userName,"1");
        if(null !=workOrderUser){
            JSONObject json =new JSONObject();
            String token = tokenUtils.getToken(userName);
            json.put("token",token);
            json.put("password",workOrderUser.getPassword());
            // 用户登陆信息 同步到redis中
            cacheManager.getCache(CacheKeyConstant.FISHCARD_BACK_ORDER_USERINFO).put(userName.trim(), json.toJSONString());
            return json.toString();

        }
        return  null;
    }

    /**
     *  登陆方法
     * @return  1 用户名不存在
     *          2 密码错误
     *          3 正确
     */
     public JsonResultModel  login(UserInfo userInfoParameter)throws  Exception{
         String  userName = userInfoParameter.getUserName();//用户名
         String  passWord = userInfoParameter.getPassWord();//密码
         logger.info("用户名:{}",userName);
         Map<String,String> resultMap = Maps.newHashMap();

         if(StringUtils.isEmpty(userName) || StringUtils.isEmpty(passWord) || StringUtils.isEmpty(userName.trim()) || StringUtils.isEmpty(passWord.trim())){
             resultMap.put("code","1");
             resultMap.put("msg","用户名和密码不能为空");
             return  JsonResultModel.newJsonResultModel(resultMap);
         }

         userName = userName.trim();
         passWord = passWord.trim();

         boolean flag = false;
         String userInfo = this.getUserInfo(userName);//用户信息
         // redis 获取用户名

         logger.info("用户信息"+userInfo);

         if(StringUtils.isEmpty(userInfo)){
             userInfo = this.userInfoFromDb(userName);

             if(StringUtils.isEmpty(userInfo)){
                 flag = false;
                 resultMap.put("code","2");
                 resultMap.put("msg","用户信息不完整或者用户名不存在,请重新输入");
             }else {
                 flag = true;
             }
         }else {
             flag = true;
         }

         JSONObject json = null;
         // 验证密码
         if(flag){
             String ps = tokenUtils.getPassword(passWord);
             json = JSON.parseObject(userInfo);
             String  redisps = json.get("password").toString();
             if(!ps .equals(redisps)){
                 flag = false;
                 resultMap.put("code","3");
                 resultMap.put("msg","密码不正确,请重新输入");
             }

         }
         // 生成token  更新redis  返回token

         if(flag){
             String realToken =DateUtil.Date2String24(new Date());

             if(null != json){
                 //更新token
                 String token = tokenUtils.getToken(userName);
                 json.put("realtoken",realToken);
                 json.put("token",token);
                 this.updateUserInfo(userName,json.toString());

                 resultMap.put("code","4");
                 resultMap.put("msg","登陆成功");
                 resultMap.put("token",token);// 生成新的token 返回给客户端
             }else {
                 resultMap.put("code","4");
                 resultMap.put("msg","服务器端异常");
             }

         }

         return JsonResultModel.newJsonResultModel(resultMap);
     }


    /**
     * 验证token 是否有效
     * @param token
     * @return
     */
    public boolean  checkToken(String token){
        boolean flag = true;
        if(StringUtils.isEmpty(token) || StringUtils.isEmpty(token.trim())){
            return false;
        }

        //更新redis中的 token
        if(flag){
            String userName = null;//登陆用户账号
            String realTokennew =null;//新的realtoken

            try{
                userName = tokenUtils.getUserName(token);
                realTokennew =DateUtil.Date2String24(new Date());
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
            String userInfo = this.getUserInfo(userName);


            try{
                if(!StringUtils.isEmpty(userInfo)){
                    JSONObject json = JSON.parseObject(userInfo);

                    // 比较token 传入token  和  redis中的token
                    if(StringUtils.isEmpty(json.get("token"))){
                        return false;
                    }

                    // token 过期
                    if (!token.equals( json.get("token") )){
                        return  false;
                    }


                    String realToken =  json.get("realtoken")==null?null:json.get("realtoken").toString();

                    if(!StringUtils.isEmpty( realToken)){
                        if(tokenUtils.isTokenCanUsed(realToken)){
                            logger.info("用户{[]}进行上述操作",userName);// 记录访问日志
                            // 更新最新的token
                            json.put("realtoken",realTokennew);
                            this.updateUserInfo(userName,json.toString());
                            logger.info("更新最新的realTokennew(时间)={}",realTokennew);
                        }else {
                            return false;
                        }
                    }else {
                        return false;
                    }

                }else {
                    return  false;
                }
            }catch (Exception e){
                logger.info("用户信息有错误,或遭受恶意攻击");
                e.printStackTrace();
                return  false;
            }
        }

        return  flag;
    }


    /**
     * 根据token退出
     * @param token
     * @return
     * @throws Exception
     */
    public JsonResultModel  logoff(String token)throws  Exception {
        Map<String,String> resultMap = Maps.newHashMap();
        if(StringUtils.isEmpty(token) || StringUtils.isEmpty(token.trim())){
            resultMap.put("code","1");
            resultMap.put("msg","请选择正确的退出方式");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        // 验证token
        String userName = tokenUtils.getUserName(token);
        if(StringUtils.isEmpty(userName)){
            resultMap.put("code","2");
            resultMap.put("msg","服务器信息错误,或者已经执行退出");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        // 通过userName获取 用户信息
        String userInfo = this.getUserInfo(userName);
        if(StringUtils.isEmpty(userInfo)){
            resultMap.put("code","3");
            resultMap.put("msg","服务器信息错误,或者已经执行退出");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        // 执行退出操作
        JSONObject json = JSON.parseObject(userInfo);
        json.put("token","");
        json.put("realtoken","");

        this.updateUserInfo(userName,json.toString());

        resultMap.put("code","4");
        resultMap.put("msg","您已经退出");

         return JsonResultModel.newJsonResultModel(resultMap);

    }

    /**
     * 根据用户登陆name  获取用户信息
     * @param userName
     * @return
     * {
     *     password:
     *     token:      用户登陆name
     *     tokenreal:  时间加密
     * }
     */
    public  String getUserInfo(String userName){
        return cacheManager.getCache(CacheKeyConstant.FISHCARD_BACK_ORDER_USERINFO).get(userName, String.class);
    }


    private void updateUserInfo(String userName,String userInfo){
        logger.info("在redis中更新用户信息");
        cacheManager.getCache(CacheKeyConstant.FISHCARD_BACK_ORDER_USERINFO).put(userName, userInfo);
    }

    public void initData() throws Exception{


        String pd = tokenUtils.getPassword("boxfishedu");
        cacheManager.getCache(CacheKeyConstant.FISHCARD_BACK_ORDER_USERINFO).put("boxfishcard001",
                 "{password:\""+pd+"\",token:\""+tokenUtils.getToken("boxfishcard001")+"\",realtoken:\""+DateUtil.Date2String24(new Date())+"\"}");
        cacheManager.getCache(CacheKeyConstant.FISHCARD_BACK_ORDER_USERINFO).put("boxfishcard002",
                "{password:\""+pd+"\",token:\""+tokenUtils.getToken("boxfishcard002")+"\",realtoken:\""+DateUtil.Date2String24(new Date())+"\"}");
        cacheManager.getCache(CacheKeyConstant.FISHCARD_BACK_ORDER_USERINFO).put("boxfishcard003",
                "{password:\""+pd+"\",token:\""+tokenUtils.getToken("boxfishcard003")+"\",realtoken:\""+DateUtil.Date2String24(new Date())+"\"}");
        cacheManager.getCache(CacheKeyConstant.FISHCARD_BACK_ORDER_USERINFO).put("boxfishcard004",
                "{password:\""+pd+"\",token:\""+tokenUtils.getToken("boxfishcard004")+"\",realtoken:\""+DateUtil.Date2String24(new Date())+"\"}");
        cacheManager.getCache(CacheKeyConstant.FISHCARD_BACK_ORDER_USERINFO).put("boxfishcard005",
                "{password:\""+pd+"\",token:\""+tokenUtils.getToken("boxfishcard005")+"\",realtoken:\""+DateUtil.Date2String24(new Date())+"\"}");
        cacheManager.getCache(CacheKeyConstant.FISHCARD_BACK_ORDER_USERINFO).put("boxfishcard006",
                "{password:\""+pd+"\",token:\""+tokenUtils.getToken("boxfishcard006")+"\",realtoken:\""+DateUtil.Date2String24(new Date())+"\"}");

    }

    /**
     * 初始化用户
     * @param userInfo
     */
    public JsonResultModel initData(UserInfo userInfo) throws Exception{
        Map<String,String> resultMap = Maps.newHashMap();

        String userName = userInfo.getUserName();
        String passWord = userInfo.getPassWord();
        if(StringUtils.isEmpty(userName) || StringUtils.isEmpty(passWord)){
            resultMap.put("code","1");
            resultMap.put("msg","用户名或者密码为空");
            return JsonResultModel.newJsonResultModel(resultMap);
        }
//        String userInfoInRedis = this.getUserInfo(userName);
//
//        if(!StringUtils.isEmpty(userInfoInRedis)){
//
//        }

        JSONObject jsonObject = new JSONObject();


        String pd = tokenUtils.getPassword(passWord.trim());

//        jsonObject.put("password",pd);
//        cacheManager.getCache(CacheKeyConstant.FISHCARD_BACK_ORDER_USERINFO).put(userName, jsonObject.toString());


        WorkOrderUser workOrderUser =  workOrderUserService.findByUserCodeAndFlag(userName,"1");
        if(null == workOrderUser){
            // 同步到db中
            workOrderUser = new WorkOrderUser();
            workOrderUser.setUserCode(userName);
            workOrderUser.setPassword(pd);
            workOrderUser.setUserName(userInfo.getRealName());
            workOrderUser.setFlag("1");
            workOrderUserService.save(workOrderUser);
        }else {
            resultMap.put("code","2");
            resultMap.put("msg","该用户已经注册过");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        resultMap.put("code","3");
        resultMap.put("userName",userName);
        resultMap.put("msg","注册成功请登陆");
        return JsonResultModel.newJsonResultModel(resultMap);
    }



    /**
     * 更改密码
     * @param userInfo
     */
    public JsonResultModel changePassword(UserInfo userInfo) throws Exception{
        Map<String,String> resultMap = Maps.newHashMap();

        String userName = userInfo.getUserName();
        String passWord = userInfo.getPassWord();
        String newPassWord = userInfo.getNewPassWord();
        if(StringUtils.isEmpty(userName) || StringUtils.isEmpty(passWord) || StringUtils.isEmpty(newPassWord)){
            resultMap.put("code","1");
            resultMap.put("msg","用户名或者密码或者新密码为空");
            return JsonResultModel.newJsonResultModel(resultMap);
        }
        String userInfoInRedis = this.getUserInfo(userName);

        if(StringUtils.isEmpty(userInfoInRedis)){
            userInfoInRedis  = this.userInfoFromDb(userName);
            if(StringUtils.isEmpty(userInfoInRedis)){
                resultMap.put("code","2");
                resultMap.put("msg","该用户未注册过");
                return JsonResultModel.newJsonResultModel(resultMap);
            }
        }

        String pd = tokenUtils.getPassword(passWord.trim());
        JSONObject jsonObject = JSON.parseObject(userInfoInRedis);

        if(!pd.equals( jsonObject.get("password").toString())){
            resultMap.put("code","4");
            resultMap.put("msg","原密码不正确");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        String newpd = tokenUtils.getPassword(newPassWord.trim());
        jsonObject.put("password",newpd);

        cacheManager.getCache(CacheKeyConstant.FISHCARD_BACK_ORDER_USERINFO).put(userName, jsonObject.toString());


        WorkOrderUser workOrderUser =  workOrderUserService.findByUserCodeAndFlag(userName,"1");
        if(null!=workOrderUser){
            workOrderUser.setPassword(newpd);
            workOrderUserService.save(workOrderUser);
        }
        resultMap.put("code","3");
        resultMap.put("msg","更改密码成功");
        return JsonResultModel.newJsonResultModel(resultMap);
    }


}
