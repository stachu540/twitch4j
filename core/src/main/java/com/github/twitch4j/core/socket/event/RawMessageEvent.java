package com.github.twitch4j.core.socket.event;

import com.github.twitch4j.core.AbstractSocketClient;
import lombok.Getter;

import java.io.IOException;

@Getter
public final class RawMessageEvent<C extends AbstractSocketClient<C>> extends AbstractSocketEvent<C> {
    private final String messageRaw;

    public RawMessageEvent(C client, String messageRaw) {
        super(client);
        this.messageRaw = messageRaw;
    }

    /**
     * Map received JSON message to requested type
     *
     * @param type type of message
     * @param <T>  received message type
     * @return message parsed to requested type
     * @throws IOException           could not parse message
     * @throws IllegalStateException the message is not a JSON
     */
    public final <T> T mapMessageAs(Class<T> type) throws IOException {
        if (messageRaw.matches("^(\\{(.+)}|\\[(.+)])$")) {
            return getClient().getCompanion().getHttpClient().getMapper().readValue(messageRaw, type);
        } else {
            throw new IllegalStateException("Cannot process message when it is not a \"JSON\"!");
        }
    }
}
