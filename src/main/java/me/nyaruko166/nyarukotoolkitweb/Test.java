package me.nyaruko166.nyarukotoolkitweb;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import me.nyaruko166.nyarukotoolkitweb.handler.FileUploadProgressListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class Test {

    private static final String SERVICE_ACCOUNT_KEY_PATH = getPathToGoodleCredentials();
    private static final String FOLDER_ID = "1UkZq5NaJ3D4YRscX9tE7Jj2Hnb_CnOYc";
    private static final String SUB_FOLDER_ID = "1mdvYJ-ltM4oV6LnOtAUmCZXPwG6v0Wbd";

    private static String getPathToGoodleCredentials() {
        String currentDirectory = System.getProperty("user.dir");
        Path filePath = Paths.get(currentDirectory, "cred.json");
        return filePath.toString();
    }

    private static Drive getService() throws IOException {
        // Load the service account key JSON file
        FileInputStream serviceAccountStream = new FileInputStream(SERVICE_ACCOUNT_KEY_PATH);

        // Create the credentials object
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(serviceAccountStream)
                .createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));

        // Build the Drive service object
        return new Drive.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                new GsonFactory(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("Google-Drive-API-Resumable-Upload")
                .build();
    }

    public static void viewFiles() throws IOException {
        Drive driveService = getService();
        // List all files uploaded by the service account
        String query = "mimeType != 'application/vnd.google-apps.folder'"; // Adjust query as needed
        FileList result = driveService.files().list()
                .setQ(query) // Query to list all files (you can adjust this)
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name)")
                .setPageSize(10) // Adjust page size as needed
                .execute();

        List<com.google.api.services.drive.model.File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (com.google.api.services.drive.model.File file : files) {
                System.out.printf("Name: %s, File ID: %s, Size: %d\n", file.getName(), file.getId(), file.getSize());
            }
        }
    }

    public static void uploadFile(File zipFile, String mimeType, String folderId) throws IOException {
        Drive service = getService();

        // Create file metadata
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(zipFile.getName());
        fileMetadata.setParents(Collections.singletonList(folderId)); // Set the parent folder

        // Specify the file content and media type
        InputStreamContent mediaContent = new InputStreamContent(
                mimeType, new FileInputStream(zipFile));

        // Set file length for large uploads
        mediaContent.setLength(zipFile.length());

        // Create the file in Drive and initiate a resumable upload
        Drive.Files.Create request = service.files().create(fileMetadata, mediaContent);
        request.getMediaHttpUploader().setDirectUploadEnabled(false); // Enable resumable upload
        request.getMediaHttpUploader().setChunkSize(MediaHttpUploader.DEFAULT_CHUNK_SIZE);

        // Set a progress listener
        request.getMediaHttpUploader().setProgressListener(new FileUploadProgressListener());

        // Execute the upload
        com.google.api.services.drive.model.File uploadedFile = request.execute();

        System.out.println("File uploaded with ID: " + uploadedFile.getId());
        System.out.println("Download link: https://drive.usercontent.google.com/download?id=" + uploadedFile.getId());
    }

    public static void deleteFile(String fileId) throws IOException {
        // Delete the file with the given fileId
        Drive driveService = getService();
        try {
            driveService.files().delete(fileId).execute();
            System.out.println("File with ID " + fileId + " has been deleted.");
        } catch (IOException e) {
            System.err.println("Error occurred: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
//            deleteFile("1JbsPvnsPErj2m2r15dCnqJEjiRsYzos4");
//            File filePath = new File(Paths.get("C:\\Users\\ADMIN\\AppData\\Roaming\\r2modmanPlus-local\\GTFO\\profiles\\Default\\Default.zip").toString());
//            String mimeType = "application/zip";  // For ZIP files
//
//            uploadFile(filePath, mimeType, SUB_FOLDER_ID);
            viewFiles();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
