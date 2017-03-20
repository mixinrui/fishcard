package com.boxfishedu.workorder.web.param.callbacklog;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hucl on 17/2/16.
 */
@Data
public class CallBackHeartBeatParam {
    @JsonProperty("CallbackCommand")
    private String CallbackCommand;

    @JsonProperty("From_Account")
    private String From_Account;

    @JsonProperty("GroupId")
    private String GroupId;

    @JsonProperty("MsgBody")
    private List<CallBackMsgBody> MsgBody;

    @JsonProperty("Type")
    private String Type;

    @Data
    public static class CallBackMsgBody {
        @JsonProperty("MsgContent")
        private CallBackMsgContent MsgContent;

        @JsonProperty("MsgType")
        private String MsgType;
    }

    @Data
    public static class CallBackMsgContent {
        @JsonProperty("Data")
        private String Data;

        @JsonProperty("Desc")
        private String Desc;

        @JsonProperty("Ext")
        private String Ext;

        @JsonProperty("FileSize")
        private Object FileSize;
    }

}



