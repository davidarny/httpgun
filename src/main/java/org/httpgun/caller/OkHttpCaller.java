package org.httpgun.caller;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.httpgun.config.ConfigProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class OkHttpCaller implements HttpCaller {
    private final OkHttpClient client;
    private final Request request;
    private final String url;
    private final ConfigProvider config;

    public OkHttpCaller(String url, Long timeout, ConfigProvider config) {
        client = new OkHttpClient.Builder().callTimeout(timeout, TimeUnit.SECONDS).build();
        request = new Request.Builder().url(url).build();
        this.url = url;
        this.config = config;
    }

    public HttpResponse call() throws IOException {
        @Cleanup val response = client.newCall(request).execute();
        return new HttpResponse(response.isSuccessful());
    }

    @Override
    @SneakyThrows
    public long size() {
        val method = config.get("http_method", String.class);
        val version = config.get("http_version", String.class);
        return (method + " " + url + " " + version).getBytes(StandardCharsets.UTF_8).length;
    }
}
