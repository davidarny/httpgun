package org.httpgun;

import lombok.val;
import org.apache.commons.cli.*;

public class CommandLineController {
    private final CommandLineParser parser;
    private final Options options;
    private final HelpFormatter formatter;
    private final ConfigProvider provider;

    public CommandLineController(CommandLineParser parser, Options options, HelpFormatter formatter, ConfigProvider provider) {
        this.parser = parser;
        this.options = options;
        this.formatter = formatter;
        this.provider = provider;
    }

    HttpGunOptions parse(String[] args) throws ParseException {
        CommandLine line = parser.parse(options, args);

        val url = (String) line.getParsedOptionValue("url");
        val num = (Long) line.getParsedOptionValue("num");
        val concurrency = (Long) line.getParsedOptionValue("concurrency");

        var timeout = provider.get("default_timeout", Long.class);
        if (line.hasOption("timeout")) {
            timeout = (Long) line.getParsedOptionValue("timeout");
        }

        return new HttpGunOptions(url, num, concurrency, timeout);
    }

    void printHelp() {
        val syntax = provider.get("cmd_syntax", String.class);
        formatter.printHelp(syntax, options);
    }
}
