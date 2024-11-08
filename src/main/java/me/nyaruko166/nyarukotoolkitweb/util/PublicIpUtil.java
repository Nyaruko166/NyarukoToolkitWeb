package me.nyaruko166.nyarukotoolkitweb.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.nyaruko166.nyarukotoolkitweb.config.CloudflareConfig;
import me.nyaruko166.nyarukotoolkitweb.dto.DnsRecordDto;
import me.nyaruko166.nyarukotoolkitweb.service.ApiService;
import okhttp3.Headers;
import okhttp3.RequestBody;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@EnableScheduling
@Component
public class PublicIpUtil {

    Logger log = LogManager.getLogger(PublicIpUtil.class);
    Gson gson = new Gson();

    private final String CLOUDFLARE_CRED = "./libs/cloudflare_cred.json";
    private CloudflareConfig cloudflareConfig = getCloudflareRequest();

    private CloudflareConfig getCloudflareRequest() {
        try {
            return gson.fromJson(new FileReader(CLOUDFLARE_CRED), CloudflareConfig.class);
        } catch (FileNotFoundException e) {
            log.error("Cloudflare Credential file not found.");
            log.error(e);
        }
        return null;
    }

    @Scheduled(fixedRate = 15 * 60 * 1000) // 15 minutes in milliseconds
    private void performTask() {
        String currentIp = getPublicIp();
        AtomicBoolean check = new AtomicBoolean(true);
        if (!currentIp.equalsIgnoreCase(cloudflareConfig.getIp())) {
            List<DnsRecordDto> lstDnsRecord = getAllDnsRecord();
            lstDnsRecord.forEach(dnsRecordDto -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", dnsRecordDto.getName());
                jsonObject.addProperty("proxied", true);
                jsonObject.addProperty("ttl", 1);
                jsonObject.addProperty("content", currentIp);
                jsonObject.addProperty("type", "A");
                if (updateIp(jsonObject.toString(), dnsRecordDto.getId()) == null) check.set(false);
            });
            try {
                if (check.get()) {
                    cloudflareConfig.setIp(currentIp);
                    FileUtils.writeStringToFile(new File(CLOUDFLARE_CRED), gson.toJson(cloudflareConfig), "UTF-8");
                }
            } catch (IOException e) {
                log.error(e);
                return;
            }
            log.info("Update ip successfully.");
        }
    }

    private String updateIp(String jsonBody, String dnsRecordId) {
        log.info("Updating ip on DNS record id: {}...", dnsRecordId);
        List<String> lstHeaders = new ArrayList<>();
        //Zone id | Dns record id
        String url = "https://api.cloudflare.com/client/v4/zones/%s/dns_records/%s".formatted(cloudflareConfig.getZone_id(), dnsRecordId);
        lstHeaders.add("Content-Type: application/json");
        lstHeaders.add("Authorization: Bearer %s".formatted(cloudflareConfig.getToken()));
        RequestBody requestBody = ApiService.requestBodyBuilder(jsonBody);
        Headers headers = ApiService.headersBuilder(lstHeaders);
        return ApiService.putRequest(url, requestBody, headers);
    }

    public static void main(String[] args) {
        PublicIpUtil publicIpUtil = new PublicIpUtil();
        publicIpUtil.performTask();
//        System.out.println(publicIpUtil.getPublicIp());
//        List<DnsRecordDto> lstRecord = publicIpUtil.getAllDnsRecord();
//        lstRecord.forEach(System.out::println);
    }

    private List<DnsRecordDto> getAllDnsRecord() {
        List<DnsRecordDto> lstDnsRecordDtos = new ArrayList<>();
        String url = "https://api.cloudflare.com/client/v4/zones/%s/dns_records".formatted(cloudflareConfig.getZone_id());
        List<String> lstHeaders = new ArrayList<>();
        lstHeaders.add("Content-Type: application/json");
        lstHeaders.add("Authorization: Bearer %s".formatted(cloudflareConfig.getToken()));
        String response = ApiService.getRequest(url, ApiService.headersBuilder(lstHeaders));

        JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
        JsonArray jsonArray = jsonObject.getAsJsonArray("result");
        for (JsonElement jsonElement : jsonArray) {
            JsonObject resultObject = jsonElement.getAsJsonObject();
            lstDnsRecordDtos.add(new DnsRecordDto(resultObject.get("id").getAsString(), resultObject.get("name").getAsString()));
        }
        return lstDnsRecordDtos;
    }

    private String getPublicIp() {
        return ApiService.getRequest("https://api.ipify.org", null);
    }

}
