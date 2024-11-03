package me.nyaruko166.nyarukotoolkitweb.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AppConfig {

    private String catbox_hash;

    private String discord_token;

    private String guild_id;

    private String channel_id;

    public static AppConfig configTemplate() {
        return AppConfig.builder().catbox_hash(" ").discord_token(" ").guild_id(" ").channel_id(" ").build();
    }
}
