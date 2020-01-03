package org.httpgun.caller;

import java.io.IOException;

public interface HttpCaller {
    HttpResponse call() throws IOException;

    long size();
}
