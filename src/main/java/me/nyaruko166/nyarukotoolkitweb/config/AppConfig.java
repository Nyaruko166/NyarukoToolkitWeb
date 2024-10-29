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

}
