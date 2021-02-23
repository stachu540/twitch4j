package com.github.twitch4j.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractTwitchObject implements TwitchObject {
    private final TwitchCompanion companion;
}
