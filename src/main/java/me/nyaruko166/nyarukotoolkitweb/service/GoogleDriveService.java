package me.nyaruko166.nyarukotoolkitweb.service;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import me.nyaruko166.nyarukotoolkitweb.config.Config;
import me.nyaruko166.nyarukotoolkitweb.handler.FileUploadProgressListener;
import me.nyaruko166.nyarukotoolkitweb.util.ByteUnitFormatter;
import me.nyaruko166.nyarukotoolkitweb.util.MimeTypeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService {

    //Todo: auto check and create req folder
    @Autowired
    private ByteUnitFormatter byteUnitFormatter;

    Logger log = LogManager.getLogger(GoogleDriveService.class);

    private final Drive driveService = getService();
    private final String SERVICE_ACCOUNT_KEY_PATH = "./libs/cred.json";
    private final String ROOT_FOLDER_ID = Config.getProperty().getRootFolderId();

    private Drive getService() {
        // Create the credentials object
        GoogleCredentials credentials = null;
        try {
            // Load the service account key JSON file
            FileInputStream serviceAccountStream = new FileInputStream(SERVICE_ACCOUNT_KEY_PATH);
            credentials = GoogleCredentials
                    .fromStream(serviceAccountStream)
                    .createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));
        } catch (IOException e) {
            log.error("cred.json for service account not found. Put it in ./libs", e);
            System.exit(1);
        }

        // Build the Drive service object
        assert credentials != null;
        return new Drive.Builder(
                new NetHttpTransport(),
                new GsonFactory(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("Google-Drive-Resumable-Uploader")
                .build();
    }

    public List<File> getAllFolder() {
        String query = "mimeType = '%s' and trashed = false".formatted(MimeTypeUtil.GOOGLE_APPS_FOLDER);
        FileList result = null;
        try {
            result = driveService.files().list()
                    .setSpaces("drive")
                    .setQ(query)
                    .setFields("files(id, name, size)")
                    .execute();
        } catch (IOException e) {
            log.error(e);
        }

        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            log.info("No folder found.");
            return Collections.emptyList();
        }
        return files;
    }

    public String createFolder(String folderName) {
        File fileMetadata = new File();
        fileMetadata.setParents(Collections.singletonList(ROOT_FOLDER_ID));
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType(MimeTypeUtil.GOOGLE_APPS_FOLDER);

        try {
            File folder = driveService.files().create(fileMetadata).execute();
            log.info("Folder created: {} | Id: {}", folder.getName(), folder.getId());
            return folder.getId();
        } catch (IOException e) {
            log.error("Error when creating new folder.");
            log.error(e);
            return null;
        }
    }

    public static void main(String[] args) {
        GoogleDriveService service = new GoogleDriveService();
//        service.createFolder("sech");
//        service.uploadFile(new java.io.File("D:\\Downloads\\Riel_Side_Shit\\Videos\\Asu no Yozora Shoukaihan (Start End)\\Asu no Yozora Shoukaihan ／ Startend cover_burn-in.mp4"), MimeTypeUtil.VIDEO_MP4, service.ROOT_FOLDER_ID);
//        service.viewFiles();
//        service.moveToTrash("1EEfEL1lj7T9UZVkCr8wvPB-N1WmarA1q");
//        service.moveToTrash("1cwOZX2p_JZCxlGNm9kh7NDJ7PEB0G4c7");
//        List<File> lstFolder = service.getAllFolder();
//        lstFolder.forEach(System.out::println);
//        List<File> lstFile = service.getAllFile();
//        lstFile.forEach(System.out::println);
        List<File> trashedFiles = service.getAllTrash();
        trashedFiles.forEach(System.out::println);
    }

    // -----------------------------------------------------------------------------------------------------------------
    public List<File> getAllTrash() {
        String query = "trashed = true";
        FileList result = null;
        try {
            result = driveService.files().list()
                    .setQ(query) // Query to list all files
                    .setSpaces("drive")
                    .setFields("files(id, name, size)")
                    .execute();
        } catch (IOException e) {
            log.error(e);
        }
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            log.info("No files found.");
            return Collections.emptyList();
        }
        return files;
    }

    public List<File> getAllFile() {
        // List all files uploaded by the service account
        String query = "mimeType != 'application/vnd.google-apps.folder' and trashed = false";
        FileList result = null;
        try {
            result = driveService.files().list()
                    .setQ(query) // Query to list all files
                    .setSpaces("drive")
                    .setFields("files(id, name, size)")
                    .execute();
        } catch (IOException e) {
            log.error(e);
        }

        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            log.info("No files found.");
            return Collections.emptyList();
        }
        return files;
    }

    public String uploadFile(java.io.File file, String mimeType, String folderId) {
        try {
            // Create file metadata
            File fileMetadata = new File();
            fileMetadata.setName(file.getName());
            fileMetadata.setParents(Collections.singletonList(folderId)); // Set the parent folder

            // Specify the file content and media type
            InputStreamContent mediaContent = new InputStreamContent(
                    mimeType, new FileInputStream(file));

            // Set file length for large uploads
            mediaContent.setLength(file.length());

            // Create the file in Drive and initiate a resumable upload
            Drive.Files.Create request = driveService.files().create(fileMetadata, mediaContent);
            request.getMediaHttpUploader().setDirectUploadEnabled(false); // Enable resumable upload
            request.getMediaHttpUploader().setChunkSize(MediaHttpUploader.DEFAULT_CHUNK_SIZE);

            // Set a progress listener
            request.getMediaHttpUploader().setProgressListener(new FileUploadProgressListener());

            // Execute the upload
            File uploadedFile = request.execute();

            log.info("File uploaded with ID: {}", uploadedFile.getId());
            log.info("Download link: https://drive.usercontent.google.com/download?id={}", uploadedFile.getId());
            return uploadedFile.getId();
        } catch (IOException e) {
            log.error("Error when uploading file.");
            log.error(e);
            return null;
        }
    }

    public String updateFile(String filedId, File file) {
        try {
            File returnFile = driveService.files().update(filedId, file).execute();
            return returnFile.getId();
        } catch (IOException e) {
            log.error("Error when updating file.");
            log.error(e);
            return null;
        }
    }

    public String moveToTrash(String fileId) {
        try {
            File file = driveService.files().update(fileId, new File().setTrashed(true)).execute();
            return file.getId();
        } catch (IOException e) {
            log.error("Error when trashing file.");
            log.error(e);
            return null;
        }
    }

    public void emptyTrash() {
        try {
            driveService.files().emptyTrash();
        } catch (IOException e) {
            log.error("Error when emptying trash.");
            log.error(e);
        }
    }

    public void deleteFile(String fileId) {
        // Delete the file with the given fileId
        try {
            driveService.files().delete(fileId).execute();
            log.info("File with ID {} has been deleted.", fileId);
        } catch (IOException e) {
            log.error("Error occurred: {}", String.valueOf(e));
        }
    }

}
