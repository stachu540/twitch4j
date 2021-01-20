package com.github.twitch4j.extensions.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Data
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
public class ExtensionSecret {

    private Instant active;

    private String content;

    private Instant expires;

}
