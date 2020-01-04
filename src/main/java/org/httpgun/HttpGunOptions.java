package org.httpgun;

import lombok.Value;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Value
public class HttpGunOptions {
    @NotBlank(message = "`url` must not be empty")
    private String url;
    @Min(value = 1, message = "`num` must be >= 1")
    private Long num;
    @Min(value = 1, message = "`concurrency` must be >= 1")
    private Long concurrency;
    @Min(value = 1, message = "`timeout` must be >= 1")
    private Long timeout;
}
