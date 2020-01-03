package org.httpgun;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.httpgun.config.ConfigProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CommandLineControllerTest {
    @SneakyThrows
    @Test
    @DisplayName("should return HttpHunOptions with url = google.com, num = 1000, concurrency = 5, timeout = 30")
    void testParseWithAllArgs() {
        val parser = Mockito.mock(CommandLineParser.class);
        val options = Mockito.mock(Options.class);
        val formatter = Mockito.mock(HelpFormatter.class);
        val config = Mockito.mock(ConfigProvider.class);

        val url = "google.com";
        val num = 1000L;
        val concurrency = 5L;
        val timeout = 30L;

        String[] args = {
            "--url", url,
            "--num", String.valueOf(num),
            "--concurrency", String.valueOf(concurrency),
            "--timeout", String.valueOf(timeout)
        };

        val line = Mockito.mock(CommandLine.class);

        Mockito.when(line.getParsedOptionValue("url")).thenReturn(url);
        Mockito.when(line.getParsedOptionValue("num")).thenReturn(num);
        Mockito.when(line.getParsedOptionValue("concurrency")).thenReturn(concurrency);
        Mockito.when(line.hasOption("timeout")).thenReturn(true);
        Mockito.when(line.getParsedOptionValue("timeout")).thenReturn(timeout);

        Mockito.when(parser.parse(options, args)).thenReturn(line);
        Mockito.when(config.get("default_timeout", Long.class)).thenReturn(timeout);

        val controller = new CommandLineController(parser, options, formatter, config);
        val result = controller.parse(args);

        Assertions.assertEquals(url, result.getUrl());
        Assertions.assertEquals(num, result.getNum());
        Assertions.assertEquals(concurrency, result.getConcurrency());
        Assertions.assertEquals(timeout, result.getTimeout());
    }

    @SneakyThrows
    @Test
    @DisplayName("should return HttpHunOptions with url = google.com, num = 1000, concurrency = 5, timeout = default_timeout")
    void testParseWithRequiredArgs() {
        val parser = Mockito.mock(CommandLineParser.class);
        val options = Mockito.mock(Options.class);
        val formatter = Mockito.mock(HelpFormatter.class);
        val config = Mockito.mock(ConfigProvider.class);

        val url = "google.com";
        val num = 1000L;
        val concurrency = 5L;
        val timeout = 1000L;

        String[] args = {
            "--url", url,
            "--num", String.valueOf(num),
            "--concurrency", String.valueOf(concurrency)
        };

        val line = Mockito.mock(CommandLine.class);

        Mockito.when(line.getParsedOptionValue("url")).thenReturn(url);
        Mockito.when(line.getParsedOptionValue("num")).thenReturn(num);
        Mockito.when(line.getParsedOptionValue("concurrency")).thenReturn(concurrency);
        Mockito.when(line.hasOption("timeout")).thenReturn(false);

        Mockito.when(parser.parse(options, args)).thenReturn(line);
        Mockito.when(config.get("default_timeout", Long.class)).thenReturn(timeout);

        val controller = new CommandLineController(parser, options, formatter, config);
        val result = controller.parse(args);

        Assertions.assertEquals(url, result.getUrl());
        Assertions.assertEquals(num, result.getNum());
        Assertions.assertEquals(concurrency, result.getConcurrency());
        Assertions.assertEquals(timeout, result.getTimeout());
    }

    @SneakyThrows
    @Test
    @DisplayName("should verify help is printing")
    void testPrintHelp() {
        val parser = Mockito.mock(CommandLineParser.class);
        val options = Mockito.mock(Options.class);
        val formatter = Mockito.mock(HelpFormatter.class);
        val config = Mockito.mock(ConfigProvider.class);

        val syntax = "httpgun";

        Mockito.when(config.get("cmd_syntax", String.class)).thenReturn(syntax);
        Mockito.doNothing().when(formatter).printHelp(syntax, options);

        val controller = new CommandLineController(parser, options, formatter, config);
        controller.printHelp();

        Mockito.verify(formatter).printHelp(syntax, options);
    }
}
