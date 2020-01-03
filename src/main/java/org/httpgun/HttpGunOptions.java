package org.httpgun;

import lombok.Value;

@Value
public class HttpGunOptions {
    private String url;
    private Long num;
    private Long concurrency;
    private Long timeout;
}
