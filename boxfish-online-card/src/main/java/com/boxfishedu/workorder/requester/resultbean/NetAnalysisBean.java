package com.boxfishedu.workorder.requester.resultbean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

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

    private double maxServicePing;
    private double maxInternetPing;

    private double averageServicePing;
    private double averageSInternetPing;

    private double minServicePing;
    private double minInternetPing;

    private List<java.util.Map<String, Object>> details = Lists.newArrayList();

    public NetAnalysisBean netAdapter(NetSourceBean netSourceBean, String role) {
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

        this.wrapAnalysis();

        return netAnalysisBean;
    }

    private void wrapAnalysis() {
        this.setMaxServicePing(this.max(SERVICE_PING));
        this.setMaxInternetPing(this.max(INTERNET_PING));

        this.setMinServicePing(this.min(SERVICE_PING));
        this.setMinInternetPing(this.min(INTERNET_PING));

        this.setAverageServicePing(this.average(SERVICE_PING));
        this.setAverageSInternetPing(this.average(INTERNET_PING));
    }

    private double getPing(Map<String, Object> detailMap, String pingType) {
        return Objects.isNull(detailMap.get(pingType))
                ? 0 : Double.parseDouble(detailMap.get(pingType).toString());
    }

    private double max(String pingType) {
        Object maxItem = this.getDetails().stream()
                .filter(map -> getPing(map, pingType) > 0)
                .max(Comparator.comparing(map -> getPing(map, pingType)))
                .get()
                .get(pingType);

        if (Objects.isNull(maxItem)) {
            return 0;
        }

        return Double.parseDouble(maxItem.toString());
    }

    private double min(String pingType) {
        Object maxItem = this.getDetails().stream()
                .filter(map -> getPing(map, pingType) > 0)
                .min(Comparator.comparing(map -> getPing(map, pingType)))
                .get()
                .get(pingType);

        if (Objects.isNull(maxItem)) {
            return 0;
        }

        return Double.parseDouble(maxItem.toString());
    }

    private double average(String pingType) {
        return this.getDetails().stream()
                .filter(map -> getPing(map, pingType) > 0)
                .mapToDouble(map -> getPing(map, pingType))
                .average()
                .getAsDouble();
    }

}
