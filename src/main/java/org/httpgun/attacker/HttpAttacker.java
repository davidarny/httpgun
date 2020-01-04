package org.httpgun.attacker;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.httpgun.utils.ErrorUtils;
import org.httpgun.HttpGunOptions;
import org.httpgun.caller.HttpCaller;
import org.httpgun.caller.HttpCallerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;

@Slf4j
public class HttpAttacker {
    private final ExecutorService pool;
    private final long termination;
    private final LongStream range;

    private final HttpCaller caller;

    private final HttpAttackerStats stats = new HttpAttackerStats();

    public HttpAttacker(HttpGunOptions options, HttpCallerFactory factory) {
        val timeout = options.getTimeout();
        val concurrency = options.getConcurrency();
        val num = options.getNum();
        val url = options.getUrl();

        caller = factory.create(url, timeout);
        pool = Executors.newFixedThreadPool(concurrency.intValue());
        range = LongStream.range(0, num);
        termination = num / concurrency * timeout;
    }

    public HttpAttackerStats attack() throws InterruptedException {
        val index = new AtomicInteger();

        range.forEach(i -> pool.execute(() -> {
            val watch = Stopwatch.createStarted();
            try {
                val response = caller.call();

                stats.addBytes(caller.size());
                watch.stop();
                long elapsed = watch.elapsed(TimeUnit.MILLISECONDS);

                if (!response.isSuccessful()) {
                    log.info("Fail #{} in {}ms", index.incrementAndGet(), elapsed);
                    stats.incrementFails();
                    return;
                }

                stats.addTimer(elapsed);

                log.info("Success #{} in {}ms", index.incrementAndGet(), elapsed);
                stats.incrementSuccesses();
            } catch (IOException e) {
                watch.stop();
                stats.incrementFails();
                ErrorUtils.log(e);
            }
        }));

        pool.shutdown();
        pool.awaitTermination(termination, TimeUnit.MILLISECONDS);

        return stats;
    }
}
