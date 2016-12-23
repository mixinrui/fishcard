package com.boxfishedu.workorder.web.result;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.Date;
import java.util.*;

/**
 * Created by hucl on 16/12/22.
 */
@Data
public class NetAnalysisBean {

    private final String studentRole = "student";

    private final String teacherRole = "teacher";

    //"student,teacher"
    private String role;

    private String maxServicePing;
    private String maxInternetPing;

    private String averageServicePing;
    private String averageSInternetPing;

    private String minServicePing;
    private String minInternetPing;

    private List<java.util.Map<String, Object>> details = Lists.newArrayList();

    public static NetAnalysisBean netAdapter(NetSourceBean netSourceBean, String role) {
        NetAnalysisBean netAnalysisBean = new NetAnalysisBean();
        netAnalysisBean.setRole(role);
        if (Objects.isNull(netSourceBean) || CollectionUtils.isEmpty(netSourceBean.getContent())) {
            return netAnalysisBean;
        }
        netSourceBean.getContent().forEach(contentBean -> {
            Map map = Maps.newHashMap();
            map.putAll(contentBean.getProperties());
            map.put("appTime", new Date(contentBean.getAppTime()));
            map.put("sysTime", new Date(contentBean.getSysTime()));
            netAnalysisBean.getDetails().add(map);
        });
        return netAnalysisBean;
    }
}
