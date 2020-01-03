package org.httpgun.attacker;

import lombok.val;
import org.httpgun.HttpGunOptions;
import org.httpgun.caller.HttpCaller;
import org.httpgun.caller.HttpCallerFactory;
import org.httpgun.caller.HttpResponse;
import org.httpgun.config.ConfigProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

class HttpAttackerTest {
    @Test
    @DisplayName("should success for 10 times")
    void testAttackSuccess() throws InterruptedException, IOException {
        val url = "foo.bar";
        val num = 10L;
        val concurrency = 5L;
        val timeout = 1000L;
        val options = new HttpGunOptions(url, num, concurrency, timeout);


        val config = Mockito.mock(ConfigProvider.class);
        val factory = Mockito.mock(HttpCallerFactory.class);
        val caller = Mockito.mock(HttpCaller.class);

        Mockito.when(caller.call()).thenReturn(new HttpResponse(true));
        Mockito.when(factory.create(url, timeout)).thenReturn(caller);

        val attacker = new HttpAttacker(options, config, factory);
        val stats = attacker.attack();

        Assertions.assertEquals(stats.getSuccesses(), num);
        Assertions.assertEquals(stats.getFails(), 0L);
    }
}
