package org.httpgun;

import lombok.val;
import org.apache.commons.cli.*;
import org.httpgun.config.ConfigProvider;

public class CommandLineController {
    private final CommandLineParser parser;
    private final Options options;
    private final HelpFormatter formatter;
    private final ConfigProvider config;

    public CommandLineController(CommandLineParser parser, Options options, HelpFormatter formatter, ConfigProvider config) {
        this.parser = parser;
        this.options = options;
        this.formatter = formatter;
        this.config = config;
    }

    HttpGunOptions parse(String[] args) throws ParseException {
        CommandLine line = parser.parse(options, args);

        val url = (String) line.getParsedOptionValue("url");
        val num = (Long) line.getParsedOptionValue("num");
        val concurrency = (Long) line.getParsedOptionValue("concurrency");

        var timeout = config.get("default_timeout", Long.class);
        if (line.hasOption("timeout")) {
            timeout = (Long) line.getParsedOptionValue("timeout");
        }

        return new HttpGunOptions(url, num, concurrency, timeout);
    }

    void printHelp() {
        val syntax = config.get("cmd_syntax", String.class);
        formatter.printHelp(syntax, options);
    }
}
