import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Local_Storage extends Storage_Spec {

    public Local_Storage() {
        this.absolutePath = null;
        this.size = Long.MAX_VALUE;
        this.prohibitedExt = new ArrayList<>();
        this.directories = new ArrayList<>();
    }

    private File getRootStorage(File[] files) {
        //TODO: funk treba da vrati skladiste ako postoji koje je roditelj!!!
        //objasnices mi na diskordu
        return null;
    }

    @Override
    public boolean createStorage() throws IOException {
        if (absolutePath != null) {
            return createStorage();
        }
        return false;
    }

    @Override
    public boolean createStorage(Path path) throws IOException {
        return createStorage(path, getSize(), getProhibitedExt());
    }

    @Override
    public boolean createStorage(Path path, long size) throws IOException {
        return createStorage(path, size, getProhibitedExt());
    }

    @Override
    public boolean createStorage(Path path, List<String> extensions) throws IOException {
        return createStorage(path, getSize(), extensions);
    }

    @Override
    public boolean createStorage(Path path, long size, List<String> extensions) throws IOException {
        File file = new File(path.toString());
        if (!file.mkdir()) {
            return false;
        }

        setAbsolutePath(path);
        setSize(size);
        setProhibitedExt(extensions);

        File config = new File(getAbsolutePath() + "\\config.json");
        System.out.println(config.getAbsolutePath());
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
        File f = new File(getAbsolutePath().toString() + path.toString());
        if (f.mkdir()){
            Directory d = new Directory(Paths.get(getAbsolutePath() + path), fileNum, new ArrayList<>());
            directories.add(d);

            updateConfig();
            return true;
        }

        return false;
    }

    @Override
    public boolean createDirectory(List<Directory> directories) throws IOException {

        for (Directory d : directories) {
            File f = new File(getAbsolutePath() + d.getName());
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

    public Directory findParentFromDirList(File f){//pronalazi roditeljski direktorijum iz liste direktorijuma ako postoji, inace null.
        for(Directory directory: directories) {
            String potentialParent = absolutePath + "\\" + directory.getName();
           // System.out.println("da li isti: " + potentialParent.equals(f.getParent())+ " ("+ potentialParent +"=?"+f.getParent());
            if(potentialParent.equals(f.getParent())){
                Directory parent = new Directory();
                parent = directory;
                return parent;
            }
        }
        return null;
    }

    @Override
    public boolean createFile(String path) throws IOException {//TODO: RADI SAMO ZA FAJLOVE (ne za direktorijume)

        File f = new File(absolutePath + path);
       // System.out.println("roditelj je: " + f.getParent() + "\n\n");
       // System.out.println("absolute path to file:" + absolutePath + "+" + path);
        Directory parent = new Directory();
        //System.out.println("\nf.getpath: "+f.getParent()+"\n");
        if((parent = findParentFromDirList(f)) != null){

            //--------------provere za skladiste:
            if(!isPermittedExt(path)) return false;//ima li zabranjenu ekstenziju
            if(!isEnoughSpace(f)) return false;//ima li dovoljno prostora
            //-----------provere za direktorijum:
            if(parent.getFileNumberLimit() == parent.getFiles().size()) //ako ce da bude previse fajlova ako dodamo ovaj
                return false;
        }else return false;

        try {
            if (f.createNewFile()) {
                //System.out.println("\n\nparent: " + parent.toString());
                parent.getFiles().add(path);
                updateConfig();

                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean createFile(String path, List<String> names) throws IOException {
        //TODO: names? ako hocemo u dubinu ili tako nesto, to cemo u test aplikaciji
        for (String name: names) {
            File f = new File(getAbsolutePath() + name);
            if (!f.createNewFile()) {
                return false;
            }
        }
        return true;
        //updateConfig();
    }

    @Override
    public boolean delete(String path) throws IOException{//TODO: RADI SAMO ZA FAJLOVE (ne za direktorijume)
        File f = new File(getAbsolutePath() + path);
        Directory parent = new Directory();
        if((parent = findParentFromDirList(f)) != null){
            if (f.delete()) {
                //System.out.println(directories);
                parent.getFiles().remove(path);
                //System.out.println(directories);
                updateConfig();
                return true;
            }
            else {
                return false;
            }
        }else return false;
    }

    @Override
    public boolean renameTo(String path, String newName) throws IOException {
        Path oldFile = Paths.get(path);
        File f = new File(getAbsolutePath() + path);

        Directory parent = new Directory();
        if((parent = findParentFromDirList(f)) != null){
            try{
                Files.move(oldFile, oldFile.resolveSibling(newName));
                parent.getFiles().remove(path);
                parent.getFiles().add(newName); //TODO: NIJE ISPRAVNO> TREBA DA SE PATH(do root-a) DODA, NE SAMO IME
                updateConfig();
                return true;
            }
            catch (IOException e) {
                return false;
            }
        }return false;
    }

    @Override
    public String toString() {
        return "path: " + getAbsolutePath()
                + "\nsize: " + getSize()
                + "\nconfig: " + getConfig().getName()
                + "\nprohibitedExt: " + getProhibitedExt();
    }

    private List<Path> listFiles(Path path) throws IOException {

        List<Path> result;
        try (Stream<Path> walk = Files.walk(path)) {
            result = walk.collect(Collectors.toList());
        }
        return result;

    }
}
