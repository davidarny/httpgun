package org.httpgun.caller;

import okhttp3.Response;

import java.io.IOException;

public interface HttpCaller {
    Response call() throws IOException;
}
