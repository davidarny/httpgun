package org.httpgun;

import com.google.common.base.Stopwatch;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.OptionalDouble;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;

public class HttpGun {
    public static final long DEFAULT_TIMEOUT = 30;
    public static final int EXIT_SUCCESS = 0;
    public static final double MILLISECOND = 1000.0;

    public static final String EXCEPTION = "Unexpected exception: {}";

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public static void main(String[] args) {
        val parser = new DefaultParser();

        val options = new Options();
        options.addOption(
            Option
                .builder()
                .longOpt("url")
                .desc("Attack URL")
                .hasArg()
                .type(String.class)
                .required()
                .build()
        );
        options.addOption(
            Option
                .builder()
                .longOpt("num")
                .desc("Total amount of requests")
                .hasArg()
                .type(Number.class)
                .required()
                .build()
        );
        options.addOption(
            Option
                .builder()
                .longOpt("concurrency")
                .desc("Total amount of threads")
                .hasArg()
                .type(Number.class)
                .required()
                .build()
        );
        options.addOption(
            Option
                .builder()
                .longOpt("timeout")
                .desc("Request timeout (defaults to 30s)")
                .hasArg()
                .type(Number.class)
                .build()
        );

        try {
            CommandLine line = parser.parse(options, args);

            val url = (String) line.getParsedOptionValue("url");
            val num = (Long) line.getParsedOptionValue("num");
            val concurrency = (Long) line.getParsedOptionValue("concurrency");

            var timeout = DEFAULT_TIMEOUT;
            if (line.hasOption("timeout")) {
                timeout = (Long) line.getParsedOptionValue("timeout");
            }

            val client = new OkHttpClient.Builder().callTimeout(timeout, TimeUnit.SECONDS).build();
            val request = new Request.Builder().url(String.format("https://%s", url)).build();
            val pool = Executors.newFixedThreadPool(concurrency.intValue());

            val index = new AtomicInteger();
            val successes = new AtomicInteger();
            val fails = new AtomicInteger();
            val timers = Collections.synchronizedList(new ArrayList<Long>());
            val total = Stopwatch.createStarted();

            logger.info("\n\n==================== ATTACK {} ====================\n", url);

            LongStream.range(0, num).forEach(i -> pool.execute(() -> {
                val watch = Stopwatch.createStarted();

                try (val response = client.newCall(request).execute()) {
                    watch.stop();
                    long elapsed = watch.elapsed(TimeUnit.MILLISECONDS);
                    timers.add(elapsed);

                    if (!response.isSuccessful()) {
                        logger.info("Fail #{} in {}ms", index.incrementAndGet(), elapsed);
                        fails.incrementAndGet();
                        return;
                    }

                    logger.info("Success #{} in {}ms", index.incrementAndGet(), elapsed);
                    successes.incrementAndGet();
                } catch (IOException e) {
                    watch.stop();
                    fails.incrementAndGet();
                    logger.error(EXCEPTION, e.getMessage());
                }
            }));

            pool.shutdown();
            pool.awaitTermination(num / concurrency * timeout, TimeUnit.SECONDS);

            logger.info("\n\n==================== REPORT ====================\n");

            logger.info("Concurrency Level: {}", concurrency);
            total.stop();
            logger.info("Total time: {}s", StringUtils.friendlyDouble(total.elapsed(TimeUnit.MILLISECONDS) / MILLISECOND));
            logger.info("Total requests: {}", num);
            logger.info("Total fails: {}", fails.get());
            long sum = timers.stream().mapToLong(Long::longValue).sum();
            logger.info("Total RPS: {}s", StringUtils.friendlyDouble(sum / concurrency / MILLISECOND));
            OptionalDouble average = timers.stream().mapToLong(Long::longValue).average();
            if (average.isPresent()) {
                logger.info("Average response time: {}ms", StringUtils.friendlyDouble(average.getAsDouble()));
            }

            System.exit(EXIT_SUCCESS);
        } catch (ParseException e) {
            val formatter = new HelpFormatter();
            formatter.printHelp("httpgun", options, true);
            logger.error(EXCEPTION, e.getMessage());
        } catch (InterruptedException e) {
            logger.error(EXCEPTION, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
