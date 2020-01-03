package org.httpgun.attacker;

import com.google.common.base.Stopwatch;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.httpgun.HttpGunOptions;
import org.httpgun.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;

public class HttpAttacker {
    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private final OkHttpClient client;
    private final Request request;
    private final ExecutorService pool;
    private final long termination;
    private final LongStream range;

    private final ConfigProvider config;

    private final HttpAttackerStats stats = new HttpAttackerStats();

    public HttpAttacker(HttpGunOptions options, ConfigProvider config) {
        val timeout = options.getTimeout();
        val concurrency = options.getConcurrency();
        val num = options.getNum();

        client = new OkHttpClient.Builder().callTimeout(timeout, TimeUnit.SECONDS).build();
        request = new Request.Builder().url(String.format("https://%s", options.getUrl())).build();
        pool = Executors.newFixedThreadPool(concurrency.intValue());
        range = LongStream.range(0, num);
        termination = num / concurrency * timeout;
        this.config = config;
    }

    public HttpAttackerStats attack() throws InterruptedException {
        val index = new AtomicInteger();

        range.forEach(i -> pool.execute(() -> {
            val watch = Stopwatch.createStarted();

            try (val response = client.newCall(request).execute()) {
                watch.stop();
                long elapsed = watch.elapsed(TimeUnit.MILLISECONDS);
                stats.addTimer(elapsed);

                if (!response.isSuccessful()) {
                    logger.info("Fail #{} in {}ms", index.incrementAndGet(), elapsed);
                    stats.incrementFails();
                    return;
                }

                logger.info("Success #{} in {}ms", index.incrementAndGet(), elapsed);
                stats.incrementSuccesses();
            } catch (IOException e) {
                watch.stop();
                stats.incrementFails();
                logger.error(config.get("exception_message_template", String.class), e.getMessage());
            }
        }));

        pool.shutdown();
        pool.awaitTermination(termination, TimeUnit.SECONDS);

        return stats;
    }
}
