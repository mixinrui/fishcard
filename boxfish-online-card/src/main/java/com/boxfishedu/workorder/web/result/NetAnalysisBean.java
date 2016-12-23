package com.boxfishedu.workorder.web.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by hucl on 16/12/22.
 */
@Data
public class NetAnalysisBean {

    @JsonIgnore
    private final String studentRole = "student";

    @JsonIgnore
    private final String teacherRole = "teacher";

    private final static String MAX = "MAX";

    private final static String MIN = "MIN";

    private final static String AVERAGE = "AVERAGE";

    private final static String SERVICE_PING = "service_ping";

    private final static String INTERNET_PING = "internet_ping";


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

    public static Double max(NetAnalysisBean netAnalysisBean, String pingType) {
        return new Double(0);
//        netAnalysisBean.getDetails().stream()
//                .sorted((o1, o2) -> Double.parseDouble(o1.get(pingType).toString()) > Double.parseDouble(o2.get(pingType).toString())).collect(Collectors.toList());
    }
}
