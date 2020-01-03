package org.httpgun;

import com.google.common.base.Stopwatch;
import lombok.val;
import org.apache.commons.cli.*;
import org.httpgun.attacker.HttpAttacker;
import org.httpgun.caller.OkHttpCallerFactory;
import org.httpgun.config.ConfigProvider;
import org.httpgun.config.PropertiesFileConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

public class HttpGun {
    public static final int EXIT_SUCCESS = 0;
    public static final double MILLISECOND = 1000.0;

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public static void main(String[] args) {
        val config = new PropertiesFileConfigProvider();
        val controller = createController(config);
        val factory = new OkHttpCallerFactory();

        try {
            val options = controller.parse(args);

            val url = options.getUrl();
            val num = options.getNum();
            val concurrency = options.getConcurrency();
            val total = Stopwatch.createStarted();

            logger.info("\n\n==================== ATTACK {} ====================\n", url);

            val attacker = new HttpAttacker(options, config, factory);
            val stats = attacker.attack();

            total.stop();

            val fails = stats.getFails();
            val sum = stats.getTimersSum();
            val average = stats.getTimersAverage();

            logger.info("\n\n==================== REPORT ====================\n");

            logger.info("Concurrency Level: {}", concurrency);
            logger.info("Total time: {}s", StringUtils.friendlyDouble(total.elapsed(TimeUnit.MILLISECONDS) / MILLISECOND));
            logger.info("Total requests: {}", num);
            logger.info("Total fails: {}", fails);
            logger.info("Total RPS: {}s", StringUtils.friendlyDouble(sum / concurrency / MILLISECOND));
            if (average.isPresent()) {
                logger.info("Average response time: {}ms", StringUtils.friendlyDouble(average.getAsDouble()));
            }
            logger.info("50%: {}ms", stats.getPercentileBy(50));
            logger.info("80%: {}ms", stats.getPercentileBy(80));
            logger.info("90%: {}ms", stats.getPercentileBy(90));
            logger.info("95%: {}ms", stats.getPercentileBy(95));
            logger.info("99%: {}ms", stats.getPercentileBy(99));
            logger.info("100%: {}ms", stats.getPercentileBy(100));

            System.exit(EXIT_SUCCESS);
        } catch (ParseException e) {
            controller.printHelp();
            logger.error(config.get("exception_message_template", String.class), e.getMessage());
        } catch (InterruptedException e) {
            logger.error(config.get("exception_message_template", String.class), e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private static CommandLineController createController(ConfigProvider config) {
        val options = createOptions();
        val parser = new DefaultParser();
        val formatter = new HelpFormatter();

        return new CommandLineController(parser, options, formatter, config);
    }

    private static Options createOptions() {
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

        return options;
    }
}
