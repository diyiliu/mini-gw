package com.tiza.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Description: HttpclientUtil
 * Author: DIYILIU
 * Update: 2016-04-22 13:40
 */
public class HttpclientUtil {

    public static String askFor(String url, String method, Map params) throws IOException {

        StringBuilder strb = new StringBuilder(url);
        strb.append("?");
        for (Iterator iterator = params.keySet().iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            Object value = params.get(key);
            strb.append(key).append("=").append(value).append("&");
        }
        String uri = strb.substring(0, strb.length() - 1);

        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpGet httpget = new HttpGet(uri);
        ResponseHandler<String> responseHandler = (final HttpResponse response) -> {

            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };

        return httpclient.execute(httpget, responseHandler);
    }
}
