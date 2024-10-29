package me.nyaruko166.nyarukotoolkitweb.config;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Config {

    Logger log = LogManager.getLogger(Config.class);

    private static Config instance;

    private AppConfig appConfig;

    private Config() {
        Gson gson = new Gson();
        File configFile = new File("./libs/config.json");
        if (!configFile.exists()) {
            try {
                FileUtils.writeStringToFile(configFile, gson.toJson(new AppConfig("")), "UTF-8");
            } catch (IOException e) {
                log.error(e);
            }
        }
        try {
            appConfig = gson.fromJson(new FileReader(configFile), AppConfig.class);
            if (appConfig.getDiscord_token().isBlank()) {
                log.error("Discord token is blank");
                log.error("Please, put your Discord token in ./libs/config.json");
                System.exit(1);
            }
        } catch (FileNotFoundException e) {
            log.error(e);
        }
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public AppConfig getProperty() {
        return appConfig;
    }

}
