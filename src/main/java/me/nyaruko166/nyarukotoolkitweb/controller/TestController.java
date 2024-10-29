package me.nyaruko166.nyarukotoolkitweb.controller;

import me.nyaruko166.nyarukotoolkitweb.discord.Bot;
import me.nyaruko166.nyarukotoolkitweb.model.Manga;
import me.nyaruko166.nyarukotoolkitweb.repository.MangaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    private MangaRepository mangaRepository;

    @GetMapping("/test")
    public String test() {
        List<Manga> lstManga = mangaRepository.findAll();
        for (Manga manga : lstManga) {
            System.out.println(manga.toString());
        }
        return "test";
    }
}
