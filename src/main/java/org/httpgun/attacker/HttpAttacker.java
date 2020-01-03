package org.httpgun.attacker;

import com.google.common.base.Stopwatch;
import lombok.val;
import org.httpgun.HttpGunOptions;
import org.httpgun.caller.HttpCaller;
import org.httpgun.caller.HttpCallerFactory;
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

    private final ExecutorService pool;
    private final long termination;
    private final LongStream range;

    private final ConfigProvider config;
    private final HttpCaller caller;

    private final HttpAttackerStats stats = new HttpAttackerStats();

    public HttpAttacker(HttpGunOptions options, ConfigProvider config, HttpCallerFactory factory) {
        val timeout = options.getTimeout();
        val concurrency = options.getConcurrency();
        val num = options.getNum();
        val url = options.getUrl();

        caller = factory.create(url, timeout);
        pool = Executors.newFixedThreadPool(concurrency.intValue());
        range = LongStream.range(0, num);
        termination = num / concurrency * timeout;
        this.config = config;
    }

    public HttpAttackerStats attack() throws InterruptedException {
        val index = new AtomicInteger();

        range.forEach(i -> pool.execute(() -> {
            val watch = Stopwatch.createStarted();
            try {
                val response = caller.call();

                watch.stop();

                long elapsed = watch.elapsed(TimeUnit.MILLISECONDS);

                if (!response.isSuccessful()) {
                    logger.info("Fail #{} in {}ms", index.incrementAndGet(), elapsed);
                    stats.incrementFails();
                    return;
                }

                stats.addTimer(elapsed);

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
