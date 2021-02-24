/*
 * MIT License
 *
 * Copyright (c) 2021 Philipp Heuer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.twitch4j.test.event;

import com.github.twitch4j.core.event.EventDisposable;
import com.github.twitch4j.core.event.EventManager;
import com.github.twitch4j.core.internal.event.ReactorEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Slf4j
@Tag("unittest")
public class ReactorEventHandlerTest {

  private static final EventManager eventManager = new EventManager();
  private static int eventsProcessed = 0;

  @BeforeAll
  public static void setUp() {
    eventManager.registerHandler(new ReactorEventHandler());
    eventManager.setDefaultEventHandler(ReactorEventHandler.class);
  }

  @AfterAll
  public static void tearDown() throws Exception {
    eventManager.close();
  }

  @Test
  @DisplayName("Handle with Object")
  public void testEventHandlerWithEvent() throws Exception {

    // Register Listener
    EventDisposable disposable = eventManager.onEvent(TestEvent.class, event -> {
      log.info("Received event [{}] that was fired at {}.", event.getId(), event.getFiredAt());
      eventsProcessed = +1;
    });
    Assertions.assertEquals(1, eventManager.getActiveSubscriptions().size());

    // dispatch
    eventManager.handle(new TestEvent());
    Thread.sleep(1000);

    // dispose handler and dispatch 1 more event
    disposable.dispose();
    eventManager.handle(new TestEvent());
    Thread.sleep(1000);

    // Verify
    Assertions.assertEquals(0, eventManager.getActiveSubscriptions().size());
    Assertions.assertEquals(1, eventsProcessed, "one event should have been handled");
  }
}
