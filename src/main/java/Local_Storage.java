import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
        if(absolutePath == null) return false; //stavio ovo da nam ne puca vise kada zaboravimo da izbrisemo
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
    public boolean createFile(String path) throws IOException {

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
                parent.getFiles().add(getNameFromPathString(path));
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
    public boolean createFile(String path, List<String> names) throws IOException { //dodavanje vise fajlova u 1 direktorijum
        for (String name: names) {
            String p = path + "\\" + name;
            if(!createFile(p))
                return false;
        }
        return true;
    }

    @Override
    public boolean delete(String path) throws IOException{
        File f = new File(getAbsolutePath() + path);

        if(f.isDirectory()){//ako je u pitanju direktorijum
           // System.out.println("name of dir to delete: " + f.getName() );
            for(Directory directory: directories) {
                if(directory.getName().equals(f.getName())){
                    if(f.delete()) {
                        directories.remove(directory);
                        return true;
                    }
                    return false;
                }
            }
            return false;
        }
        //ako je u pitanju obican fajl
        Directory parent = new Directory();
        if((parent = findParentFromDirList(f)) != null){
            if (f.delete()) {
                //System.out.println(directories);
                parent.getFiles().remove(getNameFromPathString(path));
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
        File f = new File(getAbsolutePath() + path);

        if(f.isDirectory()) { //preimenovanje direktorijuma (sve radi kako treba)
            String s = getAbsolutePath().toString();
            String[] arr = path.toString().split("\\\\");

            for (int i = 0; i < arr.length - 1; i++) {
                if (!arr[i].equals("")) {
                    s = s + "\\" + arr[i];
                }
            }
            s = s + "\\" + newName;
            File rename = new File(s);

            if (f.renameTo(rename)) { //updatovanje config-a
                for(Directory directory: directories) {
                    //System.out.println(directory.getPath().toString() + " =? " + f.getPath()+"\n");
                    if(directory.getPath().toString().equals(f.getPath())){
                        directory.setPath(Paths.get(s));
                        directory.setName(newName);
                        updateConfig();
                        break;
                    }
                }
                //System.out.println(directories);
                return true;
            }else
                return false;
        }else{ //preimenovanje fajlova
            Directory parent = new Directory();
            if((parent = findParentFromDirList(f)) != null){
                Path old = Paths.get(getAbsolutePath() + path);
                try {
                    Files.move(old, old.resolveSibling(newName));
                }catch (IOException e) {
                    return false;
                }
                parent.getFiles().remove(getNameFromPathString(path));
                parent.getFiles().add(newName);
                updateConfig();
                //System.out.println(directories);
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean moveFile(String filePath, String goalDirectory) {
        return false;
    }

    @Override
    public boolean download(String path, String goalDirectory) {
        return false;
    }

    //---------------------------------------------------------------pretrazivanje
    /**
     * //TODO: da napravimo novu klasu "ResultFile" i Enum za filtriranje i vracanje korisniku profiltrir. podataka o fajlovima
     *  da se filtrira po enumima. podrazumevana filtracija je po imenu, za svaki uneti enum, dodace se ta vrednost u rezultat.
     */
    @Override
    public List<Object> searchDirectory(String path) {
        return null;
    }

    @Override
    public List<Object> searchSubdirectories(String path) {
        return null;
    }

    @Override
    public List<Object> searchAll(String path) {
        return null;
    }

    @Override
    public List<Object> searchByExtension(String path) {
        return null;
    }

    @Override
    public List<Object> searchBySubstring(String path) {
        return null;
    }

    @Override
    public boolean isInDirectory(String name) {
        return false;
    }

    @Override
    public boolean isInDirectory(List<String> names) {
        return false;
    }

    @Override
    public Object fetchDirectory(String FileName) {
        return null;
    }

    @Override
    public List<Object> TouchedAfterInDirectory(Date date) {
        return null;
    }

    @Override
    public List<Object> FilterResults(List<Enum> Criteria) {
        return null;
    }
//--------------------------------------------------------------------------------Helpful:
    private Directory findParentFromDirList(File f){//pronalazi roditeljski direktorijum iz liste direktorijuma ako postoji, inace null.
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

    private String getNameFromPathString(String path){ //vraca ime fajla iz string-putanje
        String[] arr = path.toString().split("\\\\");
        String name = arr[arr.length-1];
        return name;
    }

    public boolean isEnoughSpace(File f) throws IOException {
        if((Files.size(absolutePath)+ f.length()) > size)
            return false;
        else
            return true;
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
