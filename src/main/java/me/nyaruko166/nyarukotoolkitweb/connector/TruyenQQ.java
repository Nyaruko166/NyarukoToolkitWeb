package me.nyaruko166.nyarukotoolkitweb.connector;

import me.nyaruko166.nyarukotoolkitweb.model.Chapter;
import me.nyaruko166.nyarukotoolkitweb.util.NetworkHelper;
import me.nyaruko166.nyarukotoolkitweb.util.PDFHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TruyenQQ implements SourceConnector {

//    static Logger log = LogManager.getLogger(TruyenQQ.class);

    @Override
    public String getMangaTitle(String mangaUrl) {
        log.info("Getting information...");
        String html = NetworkHelper.fetchHtml(mangaUrl);
        Document document = Jsoup.parse(html);
        String title = document.select("div[class=book_other] > h1").text();
        if (title.isBlank()) {
            return null;
        }
        return title;
    }

    @Override
    public List<Chapter> getChapterList(String mangaURL) {
        List<Chapter> lstChapter = new ArrayList<>();
        log.info("Fetching chapters...");

        String html = NetworkHelper.fetchHtml(mangaURL);

        Document document = Jsoup.parse(html);
        Elements elements = document.select(".content .name-chap > a");
        for (Element element : elements) {
            String title = formatChapterName(element.text());
            if (isTruyenQQ2(mangaURL)) lstChapter.add(new Chapter(title, element.attr("href")));
            else lstChapter.add(new Chapter(title, NetworkHelper.getBaseUrl(mangaURL) + element.attr("href")));
        }
        return lstChapter;
    }

    @Override
    public void downloadManga(String title, List<Chapter> lstChapter) {

        Path mangaDownloadPath = Paths.get(mangaDir + File.separator + title);
        log.info("Fetching manga: {}", title);
        if (!mangaDownloadPath.toFile().exists()) {
            mangaDownloadPath.toFile().mkdirs();
            log.info("Created folder at: {}", mangaDownloadPath.toString());
        }

        int check = 0;


        for (Chapter chapter : lstChapter) {
            if (check == 2) break;
            //Create folder to store chapter image
            Path chapterPath = Paths.get(mangaDownloadPath.toAbsolutePath() + File.separator + chapter.getTitle());
            chapterPath.toFile().mkdir();

            downloadChapter(chapter, chapterPath);

            while (PDFHelper.isFolderEmpty(chapterPath.toString())) {
                log.error("Failed to download {}?!", chapter.getTitle());
                log.warn("Retry to download...");
                downloadChapter(chapter, chapterPath);
            }
            check++;
        }
        log.info("Downloaded manga: {}", title);
//        PDFHelper.convertAllChapterToPDF(mangaDownloadPath.toString());
    }

    @Override
    public void downloadChapter(Chapter chapter, Path chapterPath) {
        log.info("Getting {} data...", chapter.getTitle());

        int count = 0;
        String html = NetworkHelper.fetchHtml(chapter.getSrc());
        Document document = Jsoup.parse(html);

        Elements readingDetail = document.select("div[class=page-chapter] > img");

        log.info("Downloading {} images...", chapter.getTitle());
        for (Element imgDetail : readingDetail) {

            //Remove cred
            String parentId = imgDetail.parent() != null ? imgDetail.parent().attr("id") : "";
            if (parentId.equalsIgnoreCase("page_99999") || parentId.equalsIgnoreCase("page_000")) {
//                System.out.println("Skipped " + parentId);
                continue;
            }

            String imgSrc = imgDetail.attr("src");
            boolean fileFormat = true;
            String fileName;
//            log.info(imgSrc);
            String nameFromUrl = imgSrc.substring(imgSrc.lastIndexOf("/") + 1);
            if (imgSrc.contains("googleusercontent")) { //google photos cdn
                fileName = "%s.jpg".formatted(String.valueOf(count));
                count++;
            } else if (nameFromUrl.contains("-fix")) { //001-fix.jpg
                fileName = "%s%s".formatted(String.valueOf(count), imgSrc.substring(imgSrc.lastIndexOf(".")));
                count++;
            } else {
                fileName = imgSrc.substring(imgSrc.lastIndexOf("/") + 1, imgSrc.lastIndexOf("?"));
            }
            // Create path to download chapter image
            Path imgPath = Paths.get(chapterPath.toAbsolutePath() + File.separator + fileName);

            boolean downloadSuccess = false;
            int attempt = 0;

            // Retry loop may cause soft lock
            while (!downloadSuccess) {
                try {
                    NetworkHelper.downloadImageByte(imgSrc, NetworkHelper.getBaseUrl(chapter.getSrc()),
                            imgPath.toString(), fileFormat);
                    downloadSuccess = true; // Mark download as successful
                } catch (IOException e) {
                    attempt++;
                    log.error("Error downloading image {}. Attempt: {}", imgSrc, attempt);
                    log.error(e);
                }
            }
        }
        log.info("Converting {} to PDF", chapter.getTitle());
        PDFHelper.convertSingleChapterToPDF(chapterPath);
    }

    private boolean isTruyenQQ2(String url) {
        return NetworkHelper.getBaseUrl(url).equalsIgnoreCase("https://truyenqq2.com");
    }

    private String formatChapterName(String title) {
        // Replace all occurrences of "Chuong n" or "Chương n" with "Chapter n"
        return title.replaceAll("Chương|chương|Chuong", "Chapter");
    }
}
