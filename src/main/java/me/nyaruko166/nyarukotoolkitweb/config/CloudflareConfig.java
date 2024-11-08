package me.nyaruko166.nyarukotoolkitweb.config;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CloudflareConfig {

    private String token;

    private String zone_id;

    private String account_id;

    private String ip;

}
