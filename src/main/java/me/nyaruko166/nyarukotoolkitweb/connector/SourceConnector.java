package me.nyaruko166.nyarukotoolkitweb.connector;

import com.google.gson.Gson;
import me.nyaruko166.nyarukotoolkitweb.model.Chapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public interface SourceConnector {

    Logger log = LogManager.getLogger(SourceConnector.class);
    Gson gson = new Gson();

    Path mangaDir = Paths.get("./temp");

    String getMangaTitle(String mangaUrl);

    List<Chapter> getChapterList(String mangaURL);

    void downloadManga(String title, List<Chapter> lstChapter);

    void downloadChapter(Chapter chapter, Path chapterPath);

}
