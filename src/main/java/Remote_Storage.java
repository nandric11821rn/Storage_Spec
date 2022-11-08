import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Remote_Storage extends Storage_Spec{

    /**
     * Application name.
     */
    private static Drive service;
    private Map<String, String> folders;
    private static final String APPLICATION_NAME = "My project";

    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the JSON factory.
     */
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the scopes required by this quickstart.
     * <p>
     * If modifying these scopes, delete your previously saved credentials at
     * ~/.credentials/calendar-java-quickstart
     */
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in = Remote_Storage.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES).setAccessType("offline").build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     *
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public static Drive getDriveService() throws IOException {
        Credential credential = authorize();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public Remote_Storage() throws IOException {
        service = getDriveService();
        this.absolutePath = null;
        this.size = Long.MAX_VALUE;
        this.prohibitedExt = new ArrayList<>();
        this.directories = new ArrayList<>();

    }
    @Override
    public boolean createStorage() throws IOException {
        return false;
    }

    @Override
    public boolean createStorage(String path) throws IOException {
        return createStorage(path, -1, new ArrayList<>());
    }

    @Override
    public boolean createStorage(String path, long size) throws IOException {
        return createStorage(path, size, new ArrayList<>());
    }

    @Override
    public boolean createStorage(String path, List<String> extensions) throws IOException {
        return createStorage(path, -1, extensions);
    }

    @Override
    public boolean createStorage(String path, long size, List<String> extensions) throws IOException {
        setAbsolutePath(path);
        setSize(size);
        setProhibitedExt(extensions);

        File fileMetadata = new File();
        fileMetadata.setName(path);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        try {
            File file = service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            System.out.println("Folder ID: " + file.getId());
            folders = new HashMap<>();
            folders.put("", file.getId());
            return true;
        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            System.err.println("Unable to create folder: " + e.getDetails());
            return false;
        }
    }

    @Override
    protected void updateConfig() throws IOException {

    }

    @Override
    public boolean createDirectory(String path) throws IOException {
        return createDirectory(path, Long.MAX_VALUE);
    }

    @Override
    public boolean createDirectory(String path, long fileNum) throws IOException {
        if (folders.containsKey(path)) {
            return false;
        }
        File fileMetadata = new File();
        fileMetadata.setName(getName(path));
        fileMetadata.setParents(Collections.singletonList(getFolderid(path)));
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        try {
            File file = service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            folders.put(path, file.getId());
            directories.add(new Directory(path, fileNum, new ArrayList<>()));
            return true;
        } catch (GoogleJsonResponseException e) {

            return false;
        }
    }

    @Override
    public boolean createDirectory(List<Directory> directories) throws IOException {
        for (Directory d : directories) {
            System.out.println(folders);
            if (folders.containsKey(d.getPath())) {
                return false;
            }
            File fileMetadata = new File();
            fileMetadata.setName(d.getName());
            fileMetadata.setParents(Collections.singletonList(getFolderid(d.getPath())));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            try {
                File file = service.files().create(fileMetadata)
                        .setFields("id")
                        .execute();
                folders.put(d.getPath(), file.getId());
                this.directories.add(d);
            } catch (GoogleJsonResponseException e) {

                return false;
            }
        }
        return true;
    }

    private String getFolderid(String path) {
        char[] cPath = path.toCharArray();
        int i = cPath.length - 1;
        while (cPath[i] != '\\') {
            i--;
        }

        return folders.get(path.substring(0, i));
    }

    private String getName(String path) {
        char[] cPath = path.toCharArray();
        int i = cPath.length - 1;
        while (cPath[i] != '\\') {
            i--;
        }

        return path.substring(i + 1);
    }
    private Directory getDirectory(String path) {
        for (Directory d : directories) {

            if (folders.get(d.getPath()).equals(getFolderid(path))) {
                return d;
            }
        }
        return null;
    }
    @Override
    public boolean createFile(String path) throws IOException {
        if (!folders.containsValue(getFolderid(path))) {
            return false;
        }
        Directory d = getDirectory(path);
        if (d == null) {
            return false;
        }
        if (d.getFileNumberLimit() <= d.getFiles().size()) {
            return false;
        }
        File fileMetadata = new File();
        fileMetadata.setName(getName(path));
        fileMetadata.setParents(Collections.singletonList(getFolderid(path)));
        try {
            File file = service.files().create(fileMetadata)
                    .setFields("id, parents")
                    .execute();
            System.out.println("File ID: " + file.getId());
            d.getFiles().add(getName(path));
            System.out.println(directories);
            return true;
        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            System.err.println("Unable to upload file: " + e.getDetails());
            return false;
        }
    }

    @Override
    public boolean createFile(String path, List<String> names) throws IOException {

        return false;
    }

    @Override
    public boolean delete(String path) throws IOException {
        return false;
    }

    @Override
    public boolean renameTo(String path, String newName) throws IOException {
        return false;
    }
}
