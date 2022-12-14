import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
public abstract class Storage_Spec {

    /**
     *      KREIRANJE SKLADISTA
     *
     *  prilikom kreiranja skladista 'moze' se zadati konfiguraija, postoji podrazumevana konfiguracija.
     *  konfiguracija se smesta u korenski direktorijum(skladiste)
     *
     *  konfiguracija podrzava:
     *      # velicinu 'skladista' u bajtovima
     *      # ekstenzije koje ne smeju da budu u 'skladistu'
     *      # koliko fajlova sme da se smesti u 'odredjeni direktorijum'

     */

    protected String absolutePath;
    protected File config;
    protected long size;
    protected List<String> prohibitedExt;
    protected List<Directory> directories;

    public boolean isEnoughSpace(File f) throws IOException {
        if((Files.size(Paths.get(absolutePath))+ f.length()) > size)//TODO: testiraj ovu proveru velicine u bajtovima
            return false;
        else
            return true;
    }
    public boolean isPermittedExt(String path){//provera da li se poklapa sa zabranjenim ekstenzijama
        for(String extention: prohibitedExt){
            if(path.endsWith(extention))
                return false;
        }
        return true;
    }
    //..................................................



    public abstract boolean createStorage() throws IOException;

    public abstract boolean createStorage(String path) throws IOException;
    //public abstract void createStorage(String path, File config); //???
    public abstract boolean createStorage(String path, long size) throws IOException;
    public abstract boolean createStorage(String path, List<String> extensions) throws IOException;
    public abstract boolean createStorage(String path, long size, List<String> extensions) throws IOException;

    protected abstract void updateConfig() throws IOException;



    /**
     *      OSNOVNE OPERACIJE NAD SKLADISTEM
     *
     *  kreiranje direktorijuma u skladistu na razne nacine(emmet):
     *      # child: '>'
     *          createD dir1[fileNum]>dir2>dir3 -> podfolder foldera dir1 je folder dir2, a podfolder foldera dir2 je folder dir3
     *
     *      # sibling: '+'
     *          createD dir1[fileNum]+dir2+dir3 -> folderi su istog ranga
     *      # grouping: '(', ')'
     *          createD dir1[fileNum]>(dir2>dir3)+dir4>dir5
     *
     *      # multiplication: '*'
     *          createD dir1[fileNum]>dir2*5 -> kreira se folder dir1 pa u njemu folderi dir2, dir3, dir4, dir5, dir6
     *
     *  smestanje fajlova na odredjenu putanju, ispisati gresku ako taj folder ne postoji
     *      # smestanje jednog fajla:
     *          createF fileName dir1>dir3
     *
     *      # smestanje vise fajlova:
     *          createF [file1, file2, file3]
     *          createF file.txt*3 dir1>dir2>dir3
     *
     *   ZA BRISANJE I PREUZIMANJE MOZE DA SE KORISTI SAMO JEDNA KOMANDA PA SE AUTOMATSKI PREPOZNA DA LI JE FOLDER ILI FAJL
     *
     *  brisanje foldera i fajlova:
     *      # brisanje foldera:
     *          deleteD dir1>dir2 -> brise dir2
     *
     *      # brisanje fajlova:
     *          deleteF dir1>fileName
     *
     *  preuzimanje foldera i fajlova:
     *      # preuzimanje foldera:
     *          fetchD dir1>dir2 localPath
     *
     *      # preuzimanje fajlova:
     *          fetchF dir1>file localPath
     */

    public abstract boolean createDirectory(String path) throws IOException;
    public abstract boolean createDirectory(String path, long fileNum) throws IOException;
    public abstract boolean createDirectory(List<Directory> directories) throws IOException;

    public abstract boolean createFile(String path) throws IOException;
    public abstract boolean createFile(String path, List<String> names) throws IOException;
    public abstract boolean delete(String path) throws IOException;
    public abstract boolean renameTo(String path, String newName) throws IOException;
    public abstract boolean moveFile(String filePath, String goalDirectory) throws IOException; //unutar skladista
    public abstract boolean download(String filepath, String goalAbsolutePath) throws GoogleJsonResponseException; //lokalno / izvan skladista

    /**
     *      PRETRAZIVANJE SKLADISTA
     *
     *      prolazenje kroz sve foldere i subfoldere i ispisivanje fajlova
     *      public static void main(String... args) throws Exception {
     *         Path dir = Paths.get("/path/to/dir");
     *         Files.walk(dir).forEach(path -> showFile(path.toFile()));
     *     }
     *
     *     public static void showFile(File file) {
     *         if (file.isDirectory()) {
     *             System.out.println("Directory: " + file.getAbsolutePath());
     *         } else {
     *             System.out.println("File: " + file.getAbsolutePath());
     *         }
     *     } prolazenje kroz sve foldere i subfoldere i ispisivanje fajlova
     *
     *///TODO: vraca object jer jos ne znam da li se koristi isti object na guglu i lokalno

    public abstract List<FileInfo> searchDirectory(String path) throws IOException; //sve fajlove u zadatom direktorijumu
    public abstract List<FileInfo> searchSubdirectories(String path) throws IOException;//sve fajlove iz svih direktorijuma u nekom direktorijumu,
    public abstract List<FileInfo> searchAll(String path) throws IOException;//fajlove u zadatom direktorijumu i svim poddirektorijumima,
    public abstract List<FileInfo> searchByExtension(String extension) throws IOException;//vrati fajlove sa odre??enom ekstenzijom (u citavom skladistu)
    public abstract List<FileInfo> searchBySubstring(String substring) throws IOException;//fajlove koji sadr??e,po??inju,zavr??avaju nekim zadatim podstringom
    public abstract boolean isInDirectory(String path, String name) throws IOException;//da li odre??eni direktorijum sadr??i fajl sa odre??enim imenom,
    public abstract boolean isInDirectory(String path, List<String> names) throws IOException;//-||-ili vi??e fajlova sa zadatom listom imena
    public abstract FileInfo fetchDirectory(String emptyString, String FileName) throws IOException;//vratiti u kom folderu se nalazi fajl sa odre??enim zadatim imenom

    //public abstract sort();//obezbediti zadavanje razli??itih kriterijuma sortiranja, npr po nazivu,datumu kreiranje/modifikacije, rastu??e/opadaju??e
    public abstract List<FileInfo> touchedAfterInDirectory(String path, Date dateTime) throws IOException;//fajlove koji su kreirani/modifikovani u nekom periodu, u nekom dir

    //---------------------------------------------------------------------Implemented in specification:
    public List<FileInfo> filterResultSet(List<IncludeResult> Criteria, List<FileInfo> fileList){//omogu??iti filtriranje podataka koji se prikazuju za fajlove rezultata
        if(fileList == null) return null;
        for(IncludeResult c : Criteria){
            for(FileInfo f : fileList){
                switch (c){
                    case SIZE:
                        f.setSizeFlag(true);
                        break;
                    case ROOT_PATH:
                        f.setPathFlag(true);
                        break;
                    case CREATION_DATE:
                        f.setCreationFlag(true);
                        break;
                    case MODIFICATION_DATE:
                        f.setModifiedFlag(true);
                        break;
                }
            }
        }
        return fileList;
    }
    //TODO: ideja: inicijalno samo ime fajla, ako se doda kriterijum (buduci enum) i on ce se pridruziti. vratice se lista custom file objekata
    public List<FileInfo> sortResultSet(List<FileInfo> fileList, IncludeResult criteria, boolean descending){
        if(fileList == null) return null;
        switch (criteria){
            case NAME:
                return sortByName(fileList, descending);
            case SIZE:
                return sortBySize(fileList, descending);
            case MODIFICATION_DATE:
                return sortByModDate(fileList, descending);
            default:
                return fileList;
        }
    }
    //da sortira na osnovu kriterijuma rastuce ili opadajuce (default rastuce)
    private List<FileInfo> sortByName(List<FileInfo> fileList, boolean descending){
        FileInfo[] arr = new FileInfo[fileList.size()];
        arr = fileList.toArray(arr);
        Arrays.sort(arr, new Comparator<FileInfo>(){
            @Override
            public int compare(FileInfo fi1, FileInfo fi2) {
                if(descending)
                    return fi2.getName().compareTo(fi1.getName());
                else
                    return fi1.getName().compareTo(fi2.getName());
            }
        });

        ArrayList<FileInfo> result = new ArrayList<>();
        for(FileInfo fi : arr){
            result.add(fi);
        }
        return result;
    }
    private List<FileInfo> sortByModDate(List<FileInfo> fileList, boolean descending){
        FileInfo[] arr = new FileInfo[fileList.size()];
        arr = fileList.toArray(arr);

        Arrays.sort(arr, new Comparator<FileInfo>(){
            @Override
            public int compare(FileInfo fi1, FileInfo fi2) {//izmenjeno je -> sad je DATE u fileInfo!
                if(descending) {
                    return fi2.getLastModifiedTime().compareTo(fi1.getLastModifiedTime());
                }else {
                    return fi1.getLastModifiedTime().compareTo(fi2.getLastModifiedTime());
                }
            }
        });

        ArrayList<FileInfo> result = new ArrayList<>();
        for(FileInfo fi : arr){
            //System.out.println(fi.getLastModifiedTime().toMillis() + "  ->  " + fi.getName());
            result.add(fi);
        }
        return result;
    }
    private List<FileInfo> sortBySize(List<FileInfo> fileList, boolean descending){
        FileInfo[] arr = new FileInfo[fileList.size()];
        arr = fileList.toArray(arr);
        Arrays.sort(arr, new Comparator<FileInfo>(){
            @Override
            public int compare(FileInfo fi1, FileInfo fi2) {
                if(descending) {
                    //return (fi2.getSize()).compareTo(fi1.getSize());
                    if(fi2.getSize() > fi1.getSize())
                        return 1;
                    if(fi1.getSize() > fi2.getSize())
                        return -1;
                    else
                        return 0;
                }else {
                    if(fi1.getSize() > fi2.getSize())
                        return 1;
                    if(fi2.getSize() > fi1.getSize())
                        return -1;
                    else
                        return 0;
                }
            }
        });

        ArrayList<FileInfo> result = new ArrayList<>();
        for(FileInfo fi : arr){
            result.add(fi);
        }
        return result;
    }

    @Override
    public String toString() {
        return "Absolute path: " + absolutePath + "\nConfig: " + config.getAbsolutePath() + "\nDirectories: " + directories;
    }
}
