package me.nyaruko166.nyarukotoolkitweb.config;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AppConfig {

    private String discord_token;

    private String guild_id;

    private String channel_id;

    public AppConfig configTemplate() {
        return AppConfig.builder().discord_token("").guild_id("").channel_id("").build();
    }
}
