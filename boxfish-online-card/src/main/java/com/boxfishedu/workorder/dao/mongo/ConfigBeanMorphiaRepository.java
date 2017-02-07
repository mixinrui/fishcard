package com.boxfishedu.workorder.dao.mongo;

import com.boxfishedu.workorder.entity.mongo.ConfigBean;
import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

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
}
