package me.nyaruko166.nyarukotoolkitweb.controller;

import me.nyaruko166.nyarukotoolkitweb.discord.Bot;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/discord")
public class DiscordController {

    @PostMapping("/send-clip")
    public ResponseEntity<?> sendClip(@RequestBody Map<String, String> mapBody) {
        Bot.sendClip(mapBody.get("videoId"));
        return ResponseEntity.ok("Uploaded clip to discord channel");
    }

}
