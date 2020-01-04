package org.httpgun.caller;

import lombok.Cleanup;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class OkHttpCaller implements HttpCaller {
    private final OkHttpClient client;
    private final Request request;
    private long size = 0;

    public OkHttpCaller(String url, Long timeout) {
        client = new OkHttpClient.Builder().callTimeout(timeout, TimeUnit.SECONDS).build();
        request = new Request.Builder().url(url).build();
    }

    public HttpResponse call() throws IOException {
        @Cleanup val response = client.newCall(request).execute();
        size += response.headers().byteCount();
        size += response.protocol().name().getBytes(StandardCharsets.UTF_8).length;
        size += String.valueOf(response.code()).getBytes(StandardCharsets.UTF_8).length;
        return new HttpResponse(response.isSuccessful());
    }

    @Override
    public long size() {
        return size;
    }
}
