package com.boxfishedu.workorder.common.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by hucl on 16/3/25.
 */
public class HttpUtil {
    public static String httpGet(String url) throws Exception {
        //创建HttpClientBuilder
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        CloseableHttpClient httpClient = httpClientBuilder.build();
        HttpGet httpget = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpget);
        org.apache.http.HttpEntity entity = response.getEntity();
        //start 读取整个页面内容
        InputStream is = entity.getContent();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }
        String backContent = buffer.toString();
        return backContent;
    }

    public static String httpPost(String requestContent, String url) throws Exception {
        //创建HttpClientBuilder
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        CloseableHttpClient httpClient = httpClientBuilder.build();
        StringEntity entity = new StringEntity(requestContent, "utf-8");//解决中文乱码问题
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(entity);
        HttpResponse response = httpClient.execute(httpPost);
        //start 读取整个页面内容
        InputStream is = response.getEntity().getContent();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }
        String backContent = buffer.toString();
        return backContent;
    }
}
