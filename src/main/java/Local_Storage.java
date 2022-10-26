import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Local_Storage extends Storage_Spec {

    public Local_Storage() {
        this.absolutePath = null;
        this.size = Long.MAX_VALUE;
        this.prohibitedExt = new ArrayList<>();
        this.directories = new ArrayList<>();
    }

    public Local_Storage(Builder builder) {
        this.absolutePath = builder.absolutePath;
        this.size = builder.size;
        this.prohibitedExt = builder.prohibitedExt;
        this.directories = new ArrayList<>();
    }

    public static class Builder {
        private String absolutePath;
        private long size;
        private List<String> prohibitedExt;

        public Builder withPath(String path) {
            this.absolutePath = path;
            return this;
        }

        public Builder withSize(long size) {
            this.size = size;
            return this;
        }

        public Builder withProhibitedExtensions(List<String> prohibitedExt) {
            this.prohibitedExt = prohibitedExt;
            return this;
        }

        public Local_Storage build() {
            return new Local_Storage(this);
        }
    }

    private File getRootStorage(File[] files) {
        //TODO: funk treba da vrati skladiste ako postoji koje je roditelj!!!
        //objasnices mi na diskordu
        return null;
    }

    @Override
    public boolean createStorage() throws IOException {
        if (absolutePath != null) {
            return createStorage(absolutePath);
        }
        return false;
    }

    @Override
    public boolean createStorage(String path) throws IOException {
        return createStorage(path, getSize(), getProhibitedExt());
    }

    @Override
    public boolean createStorage(String path, long size) throws IOException {
        return createStorage(path, size, getProhibitedExt());
    }

    @Override
    public boolean createStorage(String path, List<String> extensions) throws IOException {
        return createStorage(path, getSize(), extensions);
    }

    @Override
    public boolean createStorage(String path, long size, List<String> extensions) throws IOException {
        File file = new File(path);
        if (!file.mkdir()) {
            return false;
        }

        setAbsolutePath(path);
        setSize(size);
        setProhibitedExt(extensions);

        File config = new File(getAbsolutePath() + "\\config.json");
        if (config.createNewFile()) {
            setConfig(config);
        }
        updateConfig();
        return true;
    }

    @Override
    protected void updateConfig() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(config, this);
    }

    //---------------------------------------------------------------for sub-files (not root)

    /**
     * TODO: sta se desi ako je komanda createD dir>file.ext>dir2? mi resavamo gresku?
     * da napravimo klasu ErrorHandler i prosledjujemo greske ili ispisujemo? - za Test_Projekat
     * jer ce nam on parsirati komande sa konzole, ako je korisnik lose uneo komandu
     * da uopste ni ne zove funkcije
     *
     *
     */

    @Override
    public boolean createDirectory(String path) throws IOException {
        return createDirectory(path, -1);
    }

    @Override
    public boolean createDirectory(String path, long fileNum) throws IOException {
        File f = new File(path);
        if (f.getParentFile().isDirectory() && f.mkdir()){
            directories.add(new Directory(path, fileNum, null));
            updateConfig();
            return true;
        }

        return false;
    }

    @Override
    public boolean createDirectory(String path, List<Directory> directories) throws IOException {
        /**
         * \\dir1
         * \\dir2
         * \\dir3\\dir4
         */
        for (Directory d : directories) {
            File f = new File(getAbsolutePath() + d.getPath());
            this.directories.add(d);
        }

        return false;
    }

    @Override
    public boolean createDirectory(String path, Map<String, Integer> directories) throws IOException {
        //TODO: koja je poenta ove mape- nedovrseno
        File f = new File(path);

        if (!f.exists()){
            f.mkdirs();
            if(f.exists()) {
                System.out.println("Dir created successfully");

                //setAbsolutePath(path);
                //setDirectories(directories);
                //updateConfig();
            }
            else
                System.out.println("Dir creation failed");
        }

        return false;
    }

    @Override
    public void createFile(String path) throws IOException {

        File f = new File(path);
        f.createNewFile();

        //updateConfig();
    }

    @Override
    public void createFile(String path, List<String> names) throws IOException {
        //TODO: names? ako hocemo u dubinu ili tako nesto, to cemo u test aplikaciji
        //updateConfig();
    }

    @Override
    public void delete(String path) throws IOException{

        File f = new File(path);

        if (f.delete()) {
            System.out.println("Deleted successfully");
            //updateConfig();
        }
        else {
            System.out.println("Failed to delete");
        }
    }

    @Override
    public void renameTo(String path, String newName) throws IOException {

        Path oldFile = Paths.get(path);
        try{
            Files.move(oldFile, oldFile.resolveSibling(newName));
            System.out.println("File Successfully Renamed");

            //updateConfig();
        }
        catch (IOException e) {
            System.out.println("Rename attempt failed");
        }
    }

    @Override
    public String toString() {
        return "path: " + getAbsolutePath()
                + "\nsize: " + getSize()
                + "\nconfig: " + getConfig().getName()
                + "\nprohibitedExt: " + getProhibitedExt();
    }
}
