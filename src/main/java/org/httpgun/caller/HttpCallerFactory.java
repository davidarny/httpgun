package org.httpgun.caller;

public interface HttpCallerFactory {
    HttpCaller create(String url, Long timeout);
}
