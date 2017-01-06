package com.boxfishedu.workorder.common.log;

import lombok.Data;

import java.util.Objects;

/**
 * Created by LuoLiBing on 17/1/6.
 *
 *
 * {
     //用户id
     UserId: "123",
     //应用code，或者应用包名
     ApplicationCode:"online.course",
     //业务模块code，每个业务模块的唯一标识，如果用库的名字也可以
     ModuleCode:"mall"
     //主要业务对象，如果是用表的名字，最好
     BusinessObject:"order_form"
     //主要业务主体ID
     BusinessObjectkey:"123"
     //业务主体操作
     Operation:"",
     //日志级别
     Level:"Error",
     //个性化业务数据
     DATA:{......},
     //切面数据
     REQUEST:"",
     RESPONSE:"",
     TARGET:"",
     FUNCTION:"",
     INPUT:"",
     OUTPUT:"",
     CONSUME:"",
     BETAG:"",
   }
 *
 */
@Data
public class BaseLog {

    protected Long UserId;
    protected String ApplicationCode = "online.fishcard";
    protected String ModuleCode;
    protected String BusinessObject;
    protected String BusinessObjectkey;
    protected String Operation;
    protected String Level;
    protected Object DATA;
    protected String REQUEST;
    protected String RESPONSE;
    protected String TARGET;
    protected String FUNCTION;
    protected String INPUT;
    protected String OUTPUT;
    protected String CONSUME;
    protected String BETAG;

    public BaseLog errorLevel() {
        this.Level = "Error";
        return this;
    }

    public BaseLog data(Object object) {
        this.DATA = Objects.toString(object);
        return this;
    }

    public BaseLog operation(String operation) {
        this.Operation = operation;
        return this;
    }

    public BaseLog businessObjectKey(String objectKey) {
        this.BusinessObjectkey = objectKey;
        return this;
    }

    @Override
    public String toString() {
        return "{" +
                "UserId=" + UserId +
                ", ApplicationCode='" + ApplicationCode + '\'' +
                ", ModuleCode='" + ModuleCode + '\'' +
                ", BusinessObject='" + BusinessObject + '\'' +
                ", BusinessObjectkey='" + BusinessObjectkey + '\'' +
                ", Operation='" + Operation + '\'' +
                ", Level='" + Level + '\'' +
                ", DATA=" + DATA +
                ", REQUEST='" + REQUEST + '\'' +
                ", RESPONSE='" + RESPONSE + '\'' +
                ", TARGET='" + TARGET + '\'' +
                ", FUNCTION='" + FUNCTION + '\'' +
                ", INPUT='" + INPUT + '\'' +
                ", OUTPUT='" + OUTPUT + '\'' +
                ", CONSUME='" + CONSUME + '\'' +
                ", BETAG='" + BETAG + '\'' +
                '}';
    }
}
