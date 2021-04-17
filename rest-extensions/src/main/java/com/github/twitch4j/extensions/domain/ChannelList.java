package com.github.twitch4j.extensions.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Data
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ChannelList extends ExtensionsPagination {

    private List<Channel> channels;

}
