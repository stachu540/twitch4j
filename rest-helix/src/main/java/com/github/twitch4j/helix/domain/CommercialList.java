package com.github.twitch4j.helix.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;

/**
 * Commercials attempted
 */
@Data
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
public class CommercialList {

    @NonNull
    @JsonProperty("data")
    private List<Commercial> commercials;

}
