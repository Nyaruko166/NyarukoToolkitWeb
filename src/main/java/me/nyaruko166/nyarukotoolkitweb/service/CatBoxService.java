package me.nyaruko166.nyarukotoolkitweb.service;

import me.nyaruko166.nyarukotoolkitweb.config.Config;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class CatBoxService {

    private final String USER_HASH = Config.getProperty().getCatbox_hash();
    private final String CATBOX_API = "https://catbox.moe/user/api.php";

    public String uploadToCatbox(File file) {
        return ApiService.postRequest(CATBOX_API, multipartBodyBuilder(file));
    }

    private RequestBody multipartBodyBuilder(File file) {
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("reqtype", "fileupload")
                .addFormDataPart("userhash", USER_HASH)
                .addFormDataPart("fileToUpload", file.getName(),
                        RequestBody.create(file, MediaType.parse("image/*")))
                .build();
    }
}
