package org.httpgun.caller;

import org.httpgun.config.ConfigProvider;

public class OkHttpCallerFactory implements HttpCallerFactory {
    @Override
    public HttpCaller create(String url, Long timeout, ConfigProvider config) {
        return new OkHttpCaller(url, timeout, config);
    }
}
