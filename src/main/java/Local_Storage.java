import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
        }else{
            return false; // mora ovo zbog test aplikacije
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
        return createDirectory(path, size);
    }

    @Override
    public boolean createDirectory(String path, long fileNum) throws IOException {
        if(absolutePath == null) return false; //stavio ovo da nam ne puca vise kada zaboravimo da izbrisemo
        File f = new File(getAbsolutePath().toString() + path.toString());
        if (f.mkdir()){
            Directory d = new Directory( path, fileNum, new ArrayList<>());
            directories.add(d);

            updateConfig();
            return true;
        }

        return false;
    }

    @Override
    public boolean createDirectory(List<Directory> directories) throws IOException {

        for (Directory d : directories) {
            File f = new File(getAbsolutePath() + d.getPath());
            if (!f.mkdir()) {
                System.out.println("Neuspesno kreiran direktorijum " + d.getName());
                return false;
            }
            this.directories.add(d);
        }
        updateConfig();
        return true;
    }

    @Override
    public boolean createFile(String path) throws IOException {

        File f = new File(absolutePath + path);
        //System.out.println("roditelj je: " + f.getParent() + "\n\n");
        //System.out.println("\nabsolute path to file:" + absolutePath + "+" + path);
        Directory parent = new Directory();
        //System.out.println("\nf.getpath: "+f.getParent()+"\n");
        //System.out.println("parent from dirlist: \n"+ findParentFromDirList(f) );
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

                parent.getFiles().add(getNameFromPathString(path));
                updateConfig();
                //System.out.println("\n\nparent: " + parent.toString());

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
        if(names.isEmpty()) return false;

        for (String name: names) {
            String p = path + "\\" + name;
            System.out.println(path + "\\" + name);
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
                //System.out.println("\ndel: "+ directory.getPath().equals(getRootPathFromAbsolute(Paths.get(f.getPath())))+" "+directory.getPath()+" =? "+f.getPath());
                if(directory.getPath().equals(getRootPathFromAbsolute(Paths.get(f.getPath())))){
                    if(f.delete()) {
                        directories.remove(directory);
                        updateConfig();
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
                    if(directory.getPath().equals(getRootPathFromAbsolute(Paths.get(f.getPath())))){
                        directory.setPath(s);
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
    // goaldirectory je samo putanja (od korenskog fajla) do direkorijuma u koji se premesta (u istom skladistu)
    @Override
    public boolean moveFile(String filePath, String goalDirectory) throws IOException {
        File old = new File(getAbsolutePath() + filePath);
        if(old.isDirectory()){
            for(Directory directory: directories) {
                //System.out.println(directory.getPath().toString()+" =? "+old.getPath().toString()+"\n");
                if(directory.getPath().equals(getRootPathFromAbsolute(Paths.get(old.getPath())))){
                    Path newPath = null;
                    try {
                       newPath = Files.move(Paths.get(getAbsolutePath()+filePath), Paths.get(getAbsolutePath()+goalDirectory+"\\"+getNameFromPathString(filePath)), StandardCopyOption.ATOMIC_MOVE);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                    if(newPath != null) {
                        directory.setPath(getRootPathFromAbsolute(newPath));
                        updateConfig();
                        return true;
                    }
                    return false;
                }
            }
        }else{
            Directory parent = new Directory();
            if((parent = findParentFromDirList(old)) != null){ //treba da se izbrise iz fajlova starog roditelja
                Path newPath = null;
                try {
                    newPath = Files.move(Paths.get(getAbsolutePath()+filePath), Paths.get(getAbsolutePath()+goalDirectory+"\\"+getNameFromPathString(filePath)), StandardCopyOption.ATOMIC_MOVE);
                }catch (IOException e){
                    return false;
                }
                parent.getFiles().remove(getNameFromPathString(filePath));

                for(Directory directory: directories) {//pa da se upise u listu fajlova novog roditelja
                    //System.out.println("equals: "+directory.getPath().toString().equals(goalDirectory)+" "+directory.getPath()+" =? "+goalDirectory);
                    if(directory.getPath().toString().equals(goalDirectory)){
                        directory.getFiles().add(getNameFromPathString(filePath));
                        updateConfig();
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean download(String filepath, String goalAbsolutePath) {//kopiranje izvan skladista bez menjanja skladista
        Path copyPath = null;
        try {
            copyPath = Files.move(Paths.get(getAbsolutePath()+filepath), Paths.get(goalAbsolutePath+"\\"+getNameFromPathString(filepath)), StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //---------------------------------------------------------------pretrazivanje
    /**
     * //TODO: da napravimo novu klasu "ResultFile" i Enum za filtriranje i vracanje korisniku profiltrir. podataka o fajlovima
     *  da se filtrira po enumima. podrazumevana filtracija je po imenu, za svaki uneti enum, dodace se ta vrednost u rezultat.
     */
    @Override
    public List<FileInfo> searchDirectory(String path) throws IOException {//fajlove u direktorijumu
        File dir = new File(getAbsolutePath() + path);

        if(!dir.exists() || !dir.isDirectory()) return null;

        File[] fileList = dir.listFiles();
        ArrayList<FileInfo> abtFiles = new ArrayList<>();
        for(File file : fileList) {
            abtFiles.add(new FileInfo(file, getRootPathFromAbsolute(Paths.get(file.getAbsolutePath()))));
        }
        return abtFiles;
    }

    @Override
    public List<FileInfo> searchSubdirectories(String path) throws IOException {//fajlove u poddir-ovima koji su u prosledjenom (samo 1 nivo)
        File dir = new File(getAbsolutePath() + path);

        if(!dir.exists() || !dir.isDirectory()) return null;

        File[] fileList = dir.listFiles();
        ArrayList<FileInfo> abtFiles = new ArrayList<>();
        for(File file : fileList){ //prolazak kroz fajlove u prosledjenom
            if(file.isDirectory()){//ako je direktorijum, prodji kroz sve njegove faljove
                File subDir = new File(file.getAbsolutePath());
                File[] subFileList = subDir.listFiles();
                for(File subFile : subFileList){
                    abtFiles.add(new FileInfo(subFile, getRootPathFromAbsolute(Paths.get(subFile.getAbsolutePath()))));
                }
            }
        }
        return abtFiles;
    }

    @Override
    public List<FileInfo> searchAll(String path) throws IOException { //fajlove iz dir-a i poddir-ova do najdubljeg nivoa
        //System.out.println("\n curr path: " + path);
        File dir = new File(getAbsolutePath() + path);

        if(!dir.exists() || !dir.isDirectory()) return null;

        File[] fileList = dir.listFiles();
        ArrayList<FileInfo> abtFiles = new ArrayList<>();
        for(File file : fileList){
            //System.out.println(file.getName());
            if(!file.isDirectory())//ako nije dir, dodaj ga u niz
                abtFiles.add(new FileInfo(file, getRootPathFromAbsolute(Paths.get(file.getAbsolutePath()))));
            else//ako jeste, rekurzivan poziv da ide najdublje sto moze
                abtFiles.addAll(searchAll(getRootPathFromAbsolute(Paths.get(file.getPath()))));
        }
        return abtFiles;
    }

    @Override
    public List<FileInfo> searchByExtension(String extension) throws IOException {
        List<FileInfo> abtFiles = new ArrayList<>();
        abtFiles = searchAll("");

        ArrayList<FileInfo> resultSet = new ArrayList<>();

        for(FileInfo f : abtFiles){
            if(f.getName().endsWith(extension))
                resultSet.add(f);
        }
        return resultSet;
    }

    @Override
    public List<FileInfo> searchBySubstring(String substring) throws IOException { //sve fajlove koji sadrze substr
        List<FileInfo> abtFiles = new ArrayList<>();
        abtFiles = searchAll("");

        ArrayList<FileInfo> resultSet = new ArrayList<>();

        for(FileInfo f : abtFiles){
            if(f.getName().contains(substring))
                resultSet.add(f);
        }
        return resultSet;
    }

    @Override
    public boolean isInDirectory(String path, String name) throws IOException { //da l postoji file sa imenom u zadatom direktorijumu
        List<FileInfo> abtFiles = new ArrayList<>();
        abtFiles = searchDirectory(path); //svi fajlovi unutar unetog direktorijuma

        for(FileInfo f : abtFiles){
            if(f.getName().equals(name))
                return true;
        }
        return false;
    }

    @Override
    public boolean isInDirectory(String path, List<String> names) throws IOException { // -||- fajlovi sa imenima u z. direktorijumu
        ArrayList<String> fnames = (ArrayList<String>) names;
        for(int i = 0; i < fnames.size(); i++){
            if(!isInDirectory(path, fnames.get(i)))
                return false;
        }
        return true;
    }

    @Override//korisnik treba da prosledi korenski direktorijum skladista // PRAZAN STRING TREBA DA SE PROSLEDI
    public FileInfo fetchDirectory(String emptyString, String fileName) throws IOException {//vraca direktorijum u kojem se nalazi zadati fajl (prvo pojavljivanje)
        File dir = new File(getAbsolutePath() + emptyString);
        File[] fileList = dir.listFiles();

        FileInfo returnFile;

        for(File f: fileList){
            //System.out.println("\n"+ f.getName() + " =? " + fileName);
            if(f.isDirectory()) {
                if (f.getName().equals(fileName)){
                    returnFile = new FileInfo(dir, getRootPathFromAbsolute(Paths.get(dir.getAbsolutePath())));
                    return returnFile;
                }
                returnFile = fetchDirectory(getRootPathFromAbsolute(Paths.get(f.getAbsolutePath())), fileName);
                if(returnFile != null)
                    return returnFile;
            }
            if (f.getName().equals(fileName)) {
                returnFile = new FileInfo(dir, getRootPathFromAbsolute(Paths.get(dir.getAbsolutePath())));
                return returnFile;
            }
        }
        return null;
    }
//todo: touchedAfterInDirectory nije jos testirana metoda
    @Override
    public List<FileInfo> TouchedAfterInDirectory(String path, LocalDateTime dateTime) throws IOException { //kreirani/modifikovani u periodu-posle zadatog vremena (samo 1 nivo od prosledjenog direktorijuma)
        //ja cu samo modifikacije gledati jer ako napravljen fajl, i nije menjan, creationdate=modificationdate.
        List<FileInfo> abtFiles = new ArrayList<>();
        abtFiles = searchDirectory(path); //svi fajlovi unutar unetog direktorijuma
        if(abtFiles == null) return null;

        ArrayList<FileInfo> resultSet = new ArrayList<>();

        for(FileInfo f : abtFiles){
            LocalDateTime dt = LocalDateTime.ofInstant(f.getLastModifiedTime().toInstant(), ZoneId.systemDefault());
            if(dt.isAfter(dateTime)){
                resultSet.add(f);
            }
        }
        return resultSet;
    }
//TODO:     naredne 2 metode da se naprave u specifikaciji, ne ovde.
    //@Override
    //public List<FileInfo> FilterResultSet(List<Enum> Criteria, List<FileInfo> fileList) {
     //   return null;
    //}

    //@Override
    //public List<FileInfo> sortResultSet(List<FileInfo> fileList, IncludeResult Criteria, boolean descending) {
    //    return null;
    //}

    //--------------------------------------------------------------------------------Helpful:
    private String getRootPathFromAbsolute(Path path){//treba da vrati
        String s = path.toString();
        return s.substring(getAbsolutePath().length());
    }
    private Directory findParentFromDirList(File f){//pronalazi roditeljski direktorijum iz liste direktorijuma ako postoji, inace null.
        for(Directory directory: directories) {
            String potentialParent = absolutePath + directory.getPath();

           //System.out.println("da li isti: " + potentialParent.equals(f.getParent())+ " ("+ potentialParent +" =? "+f.getParent());
           if(potentialParent.equals(f.getParent())){
                Directory parent = new Directory();
                parent = directory;
               //System.out.println("\n");
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
        if((Files.size(Paths.get(absolutePath))+ f.length()) > size)
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