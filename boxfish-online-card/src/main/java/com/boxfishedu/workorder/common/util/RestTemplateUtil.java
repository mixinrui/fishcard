/*
* Copyright (c) 2015 boxfish.cn. All Rights Reserved.
*/
package com.boxfishedu.workorder.common.util;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Created with Intellij IDEA
 * Author: boxfish
 * Date: 16/3/22
 * Time: 09:28
 */
public class RestTemplateUtil {
    private static RestTemplate restTemplate = null;
    private static ClientHttpRequestFactory requestFactory = null;
    private static HttpClient client = null;

    static {

        client = HttpClients.createDefault();
        requestFactory = new SimpleClientHttpRequestFactory();
        restTemplate = new RestTemplate(requestFactory);

    }

    public static RestTemplate getTemplate() {
        return restTemplate;
    }

}
