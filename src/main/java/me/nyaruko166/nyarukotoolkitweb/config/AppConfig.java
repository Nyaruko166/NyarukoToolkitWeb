package me.nyaruko166.nyarukotoolkitweb.config;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AppConfig {

    private String catbox_hash;

    private String rootFolderId;

    private String mangaFolderId;

    private String pdfDoneFolderId;

    private String discord_token;

    private String guild_id;

    private String channel_id;

    public static AppConfig configTemplate() {
        return AppConfig.builder().catbox_hash(" ").discord_token(" ").guild_id(" ").channel_id(" ")
                .rootFolderId(" ").mangaFolderId(" ").pdfDoneFolderId(" ").build();
    }
}
