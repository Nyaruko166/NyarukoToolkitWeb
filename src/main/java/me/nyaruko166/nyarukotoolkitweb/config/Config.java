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

    static Logger log = LogManager.getLogger(Config.class);
    static Gson gson = new Gson();
    private static final File configFile = new File("./libs/config.json");

    private static Config instance;
    private static AppConfig appConfig;

    private Config() {
        if (!configFile.exists()) {
            try {
                FileUtils.writeStringToFile(configFile, gson.toJson(appConfig.configTemplate()), "UTF-8");
            } catch (IOException e) {
                log.error(e);
            }
        }
        loadConfig();
    }

    private void loadConfig() {
        try {
            appConfig = gson.fromJson(new FileReader(configFile), AppConfig.class);
            if (appConfig.getDiscord_token().isBlank()) {
                log.error("Discord token is blank");
                log.error("Please, put your Discord token in ./libs/config.json");
                System.exit(1);
            }
        } catch (FileNotFoundException e) {
            log.error("Config file not found", e);
        }
    }

    public static void updateConfig() {
        try {
            FileUtils.writeStringToFile(configFile, gson.toJson(appConfig), "UTF-8");
            log.info("Configuration updated successfully.");
        } catch (IOException e) {
            log.error("Failed to update config file", e);
        }
    }

//    public static Config getInstance() {
//        if (instance == null) {
//            instance = new Config();
//        }
//        return instance;
//    }

    public static AppConfig getProperty() {
        if (instance == null) {
            instance = new Config();
        }
        return appConfig;
    }
}
