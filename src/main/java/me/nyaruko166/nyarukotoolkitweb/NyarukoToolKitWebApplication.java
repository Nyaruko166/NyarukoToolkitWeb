package me.nyaruko166.nyarukotoolkitweb;

import me.nyaruko166.nyarukotoolkitweb.discord.Bot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NyarukoToolKitWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(NyarukoToolKitWebApplication.class, args);
//        Bot.runBot();
    }

}
