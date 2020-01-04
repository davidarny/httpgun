package org.httpgun.caller;

public class OkHttpCallerFactory implements HttpCallerFactory {
    @Override
    public HttpCaller create(String url, Long timeout) {
        return new OkHttpCaller(url, timeout);
    }
}
