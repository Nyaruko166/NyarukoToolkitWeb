package me.nyaruko166.nyarukotoolkitweb.service;

import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ApiService {

    static Logger log = LogManager.getLogger(ApiService.class);

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
//            .followRedirects(true)
            .build();

    public static String getRequest(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                // Response from the API
                return response.body().string();
            } else {
                log.error("Failed to fetch data: {}", response.message());
            }
        } catch (IOException e) {
            log.error(e);
        }

        return null;
    }

    public static String postRequest(String url, RequestBody requestBody) {

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().string();
                } else {
                    log.error("Failed to post data: {}", response.toString());
                }
            }
        } catch (IOException e) {
            log.error(e);
        }

        return null;
    }

    public static String urlParamBuilder(String baseUrl, Map<String, String> params) {

        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl).newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }

        return urlBuilder.build().toString();
    }

    public static RequestBody requestBodyBuilder(String jsonBody) {

        return RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));

    }

}