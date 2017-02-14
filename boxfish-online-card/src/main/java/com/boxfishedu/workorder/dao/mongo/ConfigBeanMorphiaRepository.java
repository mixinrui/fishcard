package com.boxfishedu.workorder.dao.mongo;

import com.boxfishedu.workorder.entity.mongo.ConfigBean;
import com.google.common.collect.Maps;
import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by hucl on 17/2/7.
 */
@Component
public class ConfigBeanMorphiaRepository extends BaseMorphiaRepository<ConfigBean> {
    public String getPublicWarning(){
        Query<ConfigBean> query = datastore.createQuery(ConfigBean.class);
        List<ConfigBean> configBeans=query.asList();
        if(CollectionUtils.isEmpty(configBeans)){
            return null;
        }
        return configBeans.get(0).getPublicWarning();
    }

    public ConfigBean getSingleBean(){
        Query<ConfigBean> query = datastore.createQuery(ConfigBean.class);
        return query.get();
    }

    public Map<String,Object> getPublicCoverTips(){
        Map<String,Object> map= Maps.newHashMap();

        ConfigBean configBean=this.getSingleBean();

        map.put("publicWarning",configBean.getPublicWarning());
        map.put("coverTitle",configBean.getCoverTitle());
        map.put("coverDesc",configBean.getCoverDesc());

        return map;
    }
}
