package org.httpgun.caller;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OkHttpCaller implements HttpCaller {
    private final OkHttpClient client;
    private final Request request;

    public OkHttpCaller(String url, Long timeout) {
        client = new OkHttpClient.Builder().callTimeout(timeout, TimeUnit.SECONDS).build();
        request = new Request.Builder().url(String.format("https://%s", url)).build();
    }

    public Response call() throws IOException {
        return client.newCall(request).execute();
    }
}
