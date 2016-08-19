package com.boxfishedu.workorder.common.mongo;

import com.boxfishedu.workorder.entity.mongo.CommentCardLog;
import com.boxfishedu.workorder.entity.mongo.ScheduleCourseInfo;
import com.boxfishedu.workorder.entity.mongo.TrialCourse;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.mongodb.*;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shichao on 15/11/30.
 */
@SuppressWarnings("ALL")
@Configuration
@EnableConfigurationProperties(MongoProperties.class)
public class MongoConfiguration {

    @Autowired
    private MongoProperties mongoProperties;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Bean
    MongoClient mongoClient() {
        try {
            String mongodbAddress = mongoProperties.getServerAddress();
            String[] addresses = mongodbAddress.split(",");
            ArrayList<ServerAddress> servers = new ArrayList<ServerAddress>();
            for (String address : addresses) {
                String[] addPort = address.split(":");
                String add = addPort[0];
                int port = addPort.length > 1 ? Integer.parseInt(addPort[1]) : 27017;
                servers.add(new ServerAddress(add, port));
            }
            MongoClientOptions.Builder builder = MongoClientOptions.builder();
            builder.socketKeepAlive(true)
                    .socketTimeout(60000)
                    .connectTimeout(30000)
                    .readPreference(ReadPreference.secondary())
                    .connectionsPerHost(60);
            //mongodb的连接方式 使用用户名和密码,非本地连接
            List<MongoCredential> credentials = new ArrayList<MongoCredential>();
            MongoClientOptions options = MongoClientOptions.builder()
                    .socketTimeout(6000)
                    .connectTimeout(3000)
                    .readPreference(ReadPreference.secondary())
                    .connectionsPerHost(60).build();
            credentials.add(MongoCredential.createScramSha1Credential(mongoProperties.getUsername(),
                    mongoProperties.getDbName(), mongoProperties.getPassword().toCharArray()));
            MongoClient mongoClient = new MongoClient(servers,credentials,options);
            return mongoClient;
        } catch (Exception e) {
            logger.error("mongodb初始化失败");
            throw new ExceptionInInitializerError(e);
        }
    }

    @Bean
    Morphia morphia() {
        return new Morphia();
    }

    @Bean
    Datastore datastore() {
        Morphia morphia = morphia();
        morphia.map(CommentCardLog.class);
        morphia.map(WorkOrderLog.class);
        morphia.map(ScheduleCourseInfo.class);
        morphia.map(TrialCourse.class);
        Datastore datastore = morphia.createDatastore(mongoClient(),mongoProperties.getDbName());
        datastore.ensureIndexes();
        return  datastore;
    }
}

