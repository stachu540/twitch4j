package com.github.twitch4j.helix.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * Extension Analytics
 */
@Data
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtensionAnalytics {

    /** ID of the extension whose analytics data is being provided. */
    @NonNull
    private String extensionId;

    /** URL to the downloadable CSV file containing analytics data. Valid for 5 minutes. */
    @JsonProperty("URL")
    private String URL;

    /** Type of report. */
    private String type;

    /** Report contains data of this time range. */
    private AnaylticsDateRange dateRange;
}
