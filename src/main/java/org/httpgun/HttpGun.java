package org.httpgun;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.cli.*;
import org.httpgun.attacker.HttpAttacker;
import org.httpgun.caller.OkHttpCallerFactory;
import org.httpgun.config.ConfigProvider;
import org.httpgun.config.PropertiesFileConfigProvider;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HttpGun {
    public static final int EXIT_SUCCESS = 0;
    public static final double MILLISECOND = 1000.0;

    public static void main(String[] args) {
        val validator = createValidator();
        val config = new PropertiesFileConfigProvider();
        val controller = createController(config);
        val factory = new OkHttpCallerFactory();

        try {
            val options = controller.parse(args);

            val violations = validator.validate(options);
            if (violations.iterator().hasNext()) {
                val violation = violations.iterator().next();
                throw new IllegalArgumentException(violation.getMessage());
            }

            val url = options.getUrl();
            val num = options.getNum();
            val concurrency = options.getConcurrency();
            val total = Stopwatch.createStarted();

            log.info("\n\n==================== ATTACK {} ====================\n", url);

            val attacker = new HttpAttacker(options, config, factory);
            val stats = attacker.attack();

            total.stop();

            val fails = stats.getFails();
            val sum = stats.getTimersSum();
            val average = stats.getTimersAverage();
            val bytes = stats.getTotalBytesCount();

            log.info("\n\n==================== REPORT ====================\n");

            log.info("Concurrency Level: {}", concurrency);
            log.info("Total time: {}s", StringUtils.friendlyDouble(total.elapsed(TimeUnit.MILLISECONDS) / MILLISECOND));
            log.info("Total requests: {}", num);
            log.info("Total fails: {}", fails);
            log.info("Total RPS: {}", StringUtils.friendlyDouble((double) sum / (double) concurrency / MILLISECOND));
            log.info("Total bytes transmitted: {}b", bytes);
            if (average.isPresent()) {
                log.info("Average response time: {}ms", StringUtils.friendlyDouble(average.getAsDouble()));
            }
            log.info("50%: {}ms", stats.getPercentileBy(50));
            log.info("80%: {}ms", stats.getPercentileBy(80));
            log.info("90%: {}ms", stats.getPercentileBy(90));
            log.info("95%: {}ms", stats.getPercentileBy(95));
            log.info("99%: {}ms", stats.getPercentileBy(99));
            log.info("100%: {}ms", stats.getPercentileBy(100));

            System.exit(EXIT_SUCCESS);
        } catch (ParseException e) {
            controller.printHelp();
            log.error(config.get("exception_message_template", String.class), e.getMessage());
        } catch (InterruptedException e) {
            log.error(config.get("exception_message_template", String.class), e.getMessage());
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

    private static Validator createValidator() {
        val factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }
}
