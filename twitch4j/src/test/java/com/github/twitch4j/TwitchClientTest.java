package com.github.twitch4j;

import com.github.philippheuer.events4j.core.EventManager;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.util.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Slf4j
@Tag("unittest")
public class TwitchClientTest {

    /**
     * Twitch Client Test
     */
    @Test
    @DisplayName("Tests the TwitchClientBuilder")
    public void buildTwitch4J() {
        // external event manager (for shared module usage - streamlabs4j)
        EventManager eventManager = new EventManager();

        // construct twitchClient
        TwitchClient twitchClient = TwitchClientBuilder.builder()
            .withEventManager(eventManager)
            .withEnableHelix(true)
            .withEnableKraken(true)
            .withEnableChat(false)
            .build();
    }

    /**
     * Debugging
     */
    @Test
    @DisplayName("Test for local execution in error diagnostics")
    @Disabled
    public void localTest() throws Exception {
        // construct twitchClient
        TwitchClient twitchClient = TwitchClientBuilder.builder()
            .withEventManager(null)
            .withEnableHelix(false)
            .withEnableKraken(false)
            .withEnableTMI(false)
            .withEnableChat(true)
            .withChatAccount(TestUtils.getCredential())
            .withEnablePubSub(false)
            .withEnableGraphQL(false)
            .withScheduledThreadPoolExecutor(new ScheduledThreadPoolExecutor(1))
            .withHelperThreadRate(10000L)
            .build();

        // join twitch4j channel
        twitchClient.getChat().joinChannel("twitch4j");

        // register all event listeners
        twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class).onEvent(ChannelMessageEvent.class, event -> {
            System.out.println("[" + event.getChannel().getName() + "]["+event.getPermissions().toString()+"] " + event.getUser().getName() + ": " + event.getMessage());
        });

        TestUtils.sleepFor(5000);
    }

}
