package org.httpgun.caller;

import org.httpgun.config.ConfigProvider;

public interface HttpCallerFactory {
    HttpCaller create(String url, Long timeout, ConfigProvider config);
}
