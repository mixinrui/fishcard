package com.boxfishedu.workorder.web.param.callbacklog;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by hucl on 17/2/16.
 */
@Data
public class CallBackHeartBeatParam {
    private String CallbackCommand;
    private String From_Account;
    private String GroupId;
    private List<CallBackMsgBody> MsgBody;
    private String Type;

    @Data
    public static class CallBackMsgBody{
        private CallBackMsgContent MsgContent;
        private String MsgType;
    }

    @Data
    public static class CallBackMsgContent{
        private Map<String,Object> Data;
        private String Desc;
        private String Ext;
    }

}



