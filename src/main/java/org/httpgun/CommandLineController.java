package org.httpgun;

import lombok.val;
import org.apache.commons.cli.*;

public class CommandLineController {
    public static final long DEFAULT_TIMEOUT = 30;

    private final CommandLineParser parser;
    private final Options options;
    private final HelpFormatter formatter;

    public CommandLineController(CommandLineParser parser, Options options, HelpFormatter formatter) {
        this.parser = parser;
        this.options = options;
        this.formatter = formatter;
    }

    HttpGunOptions parse(String[] args) throws ParseException {
        CommandLine line = parser.parse(options, args);

        val url = (String) line.getParsedOptionValue("url");
        val num = (Long) line.getParsedOptionValue("num");
        val concurrency = (Long) line.getParsedOptionValue("concurrency");

        var timeout = DEFAULT_TIMEOUT;
        if (line.hasOption("timeout")) {
            timeout = (Long) line.getParsedOptionValue("timeout");
        }

        return new HttpGunOptions(url, num, concurrency, timeout);
    }

    void printHelp() {
        formatter.printHelp("httpgun", options);
    }
}
