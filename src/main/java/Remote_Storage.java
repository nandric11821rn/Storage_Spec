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
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Remote_Storage extends Storage_Spec {

    /**
     * Application name.
     */
    private static Drive service;
    private Map<String, String> folders;
    private Map<String, String> files;
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
        this.config = new java.io.File("config.json");
        boolean v = this.config.createNewFile();
        this.files = new HashMap<>();
    }

    @Override
    public boolean createStorage() throws IOException {
        return false;
    }

    @Override
    public boolean createStorage(String path) throws IOException {
        return createStorage(path, size, new ArrayList<>());
    }

    @Override
    public boolean createStorage(String path, long size) throws IOException {
        return createStorage(path, size, new ArrayList<>());
    }

    @Override
    public boolean createStorage(String path, List<String> extensions) throws IOException {
        return createStorage(path, size, extensions);
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
            folders = new HashMap<>();
            folders.put("", file.getId());

            updateConfig();

            return true;
        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            System.err.println("Unable to create folder: " + e.getDetails());
            return false;
        }
    }

    @Override
    protected void updateConfig() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(this.config, this);


        try {
            if (files.containsKey(this.config.getName())) {
                File oldFile = service.files().get(files.get(this.config.getName())).execute();
                File newFile = new File();
                newFile.setName(oldFile.getName());
                newFile.setParents(oldFile.getParents());
                FileContent newContent = new FileContent("text/json", this.config);
                File f = service.files().update(files.get(this.config.getName()), newFile, newContent).execute();
                return;
            }
            File config = new File();
            config.setName("config.json");
            config.setParents(Collections.singletonList(folders.get("")));


            FileContent mediaContent = new FileContent("text/json", this.config);
            File file = service.files().create(config, mediaContent)
                    .setFields("id, parents").execute();
            files.put(this.config.getName(), file.getId());

        } catch (GoogleJsonResponseException e) {
            System.err.println("Unable to delete config file: " + e.getDetails());
        }
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
            updateConfig();
            return true;
        } catch (GoogleJsonResponseException e) {

            return false;
        }
    }

    @Override
    public boolean createDirectory(List<Directory> directories) throws IOException {
        for (Directory d : directories) {
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
                updateConfig();
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

        Date date = new Date(System.currentTimeMillis());
        DateTime dateTime = new DateTime(date);
        System.out.println(dateTime);

        //fileMetadata.setCreatedTime(dateTime);
        //fileMetadata.setModifiedTime(dateTime);
        fileMetadata.setParents(Collections.singletonList(getFolderid(path)));
        try {
            File file = service.files().create(fileMetadata)
                    .setFields("id, parents")
                    .execute();
            d.getFiles().add(getName(path));
            files.put(path, file.getId());


            updateConfig();
            return true;
        } catch (GoogleJsonResponseException e) {

            System.err.println("Unable to upload file: " + e.getDetails());
            return false;
        }
    }

    @Override
    public boolean createFile(String path, List<String> names) throws IOException {

        return false;
    }

    private boolean isFile(String path) {
        return files.containsKey(path);
    }

    @Override
    public boolean delete(String path) throws IOException {
        try {
            if (isFile(path)) {
                service.files().delete(files.get(path)).execute();
                Directory d = getDirectory(path);
                d.getFiles().remove(getName(path));
                files.remove(path);
            } else {
                Directory directory = null;
                for (Directory d : directories) {
                    if (d.getPath().equals(path)) {
                        directory = d;
                        break;
                    }
                }
                service.files().delete(folders.get(path)).execute();
                for (String file : directory.getFiles()) {
                    files.remove(file);
                }
                directories.remove(directory);

            }
            return true;
        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
        }

        return false;
    }

    private String getPathWithoutName(String path) {
        char[] cPath = path.toCharArray();
        int i = cPath.length - 1;
        while (cPath[i] != '\\') {
            i--;
        }

        return path.substring(0, i);
    }

    @Override
    public boolean renameTo(String path, String newName) throws IOException {

        try {
            if (isFile(path)) {
                //rename-uje u directoriumu
                String fileName = getName(path);
                Directory d = getDirectory(path);
                d.getFiles().remove(fileName);
                d.getFiles().add(newName);
                String fileId = files.get(path);

                //rename-uje u files-u
                files.remove(path);
                files.put(getPathWithoutName(path) + "\\" + newName, fileId);

                //rename-uje na drive-u
                File newFile = new File();
                newFile.setName(newName);
                service.files().update(fileId, newFile).execute();

            } else {
                //preimenovanje directorijuma
                String dirName = getName(path);
                for (Directory d : directories) {
                    if (d.getName().equals(dirName)) {
                        d.setName(newName);
                        d.setPath(getPathWithoutName(path) + "\\" + newName);
                    }
                }

                //preimenovanje na drive-u
                String folderID = folders.get(path);
                File newFile = new File();
                newFile.setName(newName);
                service.files().update(folderID, newFile).execute();

                //preimenovanje u folders
                folders.remove(path);
                folders.put(getPathWithoutName(path) + "\\" + newName, folderID);

            }
            return true;
        } catch (GoogleJsonResponseException e) {

            System.err.println("Unable to rename file: " + e.getDetails());
            return false;
        }

    }

    @Override
    public boolean moveFile(String filePath, String goalDirectory) throws IOException {
        try {
            File oldFile;

            if (isFile(filePath)) {
                oldFile = service.files().get(files.get(filePath)).execute();
            } else {
                oldFile = service.files().get(folders.get(filePath)).execute();
            }

            File newFile = new File();
            newFile.setName(oldFile.getName());
            newFile.setParents(Collections.singletonList(folders.get(goalDirectory)));
            service.files().create(newFile)
                    .setFields("id, parents")
                    .execute();
            ;
            service.files().delete(files.get(filePath)).execute();

            //service.files().update(oldFile.getId(), newFile).execute();

            return true;
        } catch (GoogleJsonResponseException e) {

            System.err.println("Unable to move file: " + e.getDetails());
            return false;
        }
    }
    public void getAllFile(String path) {
        try {
            File f = service.files().get(files.get(path)).setFields("name, createdTime, modifiedTime").execute();
            System.out.println(f.getName() + " : " + f.getCreatedTime());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public boolean download(String filepath, String goalAbsolutePath) throws GoogleJsonResponseException {
        File file = new File();
        return false;
    }

    private FileInfo getFileInfo(File file, Directory directory) {
        String fileName = file.getName();
        String filePath = directory.getPath() + "\\" + file;
        Date date = new Date(System.currentTimeMillis());
        DateTime fileCreationDate = file.getCreatedTime();
        DateTime fileModifyDate = file.getModifiedTime();

        return  new FileInfo(fileName,filePath,null, date, date, file.getSize());
    }

    @Override
    public List<FileInfo> searchDirectory(String path) throws IOException {
        String folderID = folders.get(path);
        String directoryName = getName(path);

        Directory directory = null;
        for (Directory d : directories) {
            if (d.getName().equals(directoryName)) {
                directory = d;
                break;
            }
        }
        List<FileInfo> infoList = new ArrayList<>();
        for (String file : directory.getFiles()) {
            File file1 = service.files().get(files.get(directory.getPath() + "\\" + file)).setFields("name, createdTime, modifiedTime, size").execute();

            infoList.add(getFileInfo(file1, directory));
        }
        System.out.println(infoList);
        return infoList;
    }

    @Override
    public List<FileInfo> searchSubdirectories(String path) throws IOException {
        List<Directory> direcotryList = new ArrayList<>();
        for (String directoryKey : folders.keySet()) {
            if (directoryKey.equals(path))
                continue;
            if (directoryKey.contains(path)) {
                for (Directory d : directories) {
                    if (d.getPath().equals(directoryKey))
                        direcotryList.add(d);
                }
            }
        }

        return null;
    }

    @Override
    public List<FileInfo> searchAll(String path) throws IOException {
        List<Directory> direcotryList = new ArrayList<>();
        for (String directoryKey : folders.keySet()) {
            if (directoryKey.contains(path)) {
                for (Directory d : directories) {
                    if (d.getPath().equals(directoryKey))
                        direcotryList.add(d);
                }
            }
        }
        return null;
    }

    @Override
    public List<FileInfo> searchByExtension(String extension) throws IOException {
        List<FileInfo> infoList = searchAll(absolutePath);

        List<FileInfo> resultInfoList = new ArrayList<>();
        for (FileInfo info : infoList) {
            if (info.getName().endsWith(extension))
                resultInfoList.add(info);
        }

        return resultInfoList;
    }

    @Override
    public List<FileInfo> searchBySubstring(String substring) throws IOException {
        List<FileInfo> infoList = searchAll(absolutePath);

        List<FileInfo> resultInfoList = new ArrayList<>();
        for (FileInfo info : infoList) {
            if (info.getName().contains(substring))
                resultInfoList.add(info);
        }

        return resultInfoList;
    }

    @Override
    public boolean isInDirectory(String path, String name) throws IOException {
        Directory directory = new Directory();

        for (Directory d : directories) {
            if (d.getPath().equals(path)) {
                directory = d;
                break;
            }
        }

        return directory.getFiles().contains(name);
    }

    @Override
    public boolean isInDirectory(String path, List<String> names) throws IOException {
        Directory directory = new Directory();

        for (Directory d : directories) {
            if (d.getPath().equals(path)) {
                directory = d;
                break;
            }
        }

        for (String name : names)
            if (!directory.getFiles().contains(name))
                return false;

        return true;
    }

    @Override
    public FileInfo fetchDirectory(String emptyString, String FileName) throws IOException {
        Directory directory = new Directory();
        for (Directory d : directories) {
            if (d.getFiles().contains(FileName)) {
                directory = d;
                break;
            }

        }
        return null;
    }

    @Override
    public List<FileInfo> touchedAfterInDirectory(String path, Date dateTime) throws IOException {
        return null;
    }
}
