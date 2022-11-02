import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
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

    protected Path absolutePath;
    protected File config;
    protected long size;
    protected List<String> prohibitedExt;
    protected List<Directory> directories;

    public boolean isPermittedExt(String path){//provera da li se poklapa sa zabranjenim ekstenzijama
        for(String extention: prohibitedExt){
            if(path.endsWith(extention))
                return false;
        }
        return true;
    }
    //..................................................



    public abstract boolean createStorage() throws IOException;

    public abstract boolean createStorage(Path path) throws IOException;
    //public abstract void createStorage(String path, File config); //???
    public abstract boolean createStorage(Path path, long size) throws IOException;
    public abstract boolean createStorage(Path path, List<String> extensions) throws IOException;
    public abstract boolean createStorage(Path path, long size, List<String> extensions) throws IOException;

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
    public abstract boolean createDirectory(String path, Map<String, Integer> directories) throws IOException;
    public abstract boolean createFile(String path) throws IOException;
    public abstract boolean createFile(String path, List<String> names) throws IOException;
    public abstract boolean delete(String path) throws IOException;
    public abstract boolean renameTo(String path, String newName) throws IOException;
    public abstract boolean moveFile(String filePath, String goalDirectory) throws IOException; //unutar skladista
    public abstract boolean download(String filepath, String goalAbsolutePath); //lokalno / izvan skladista

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
    public abstract List<FileInfo> searchByExtension(String extension) throws IOException;//vrati fajlove sa određenom ekstenzijom (u citavom skladistu)
    public abstract List<FileInfo> searchBySubstring(String substring) throws IOException;//fajlove koji sadrže,počinju,završavaju nekim zadatim podstringom
    public abstract boolean isInDirectory(String path, String name) throws IOException;//da li određeni direktorijum sadrži fajl sa određenim imenom,
    public abstract boolean isInDirectory(String path, List<String> names) throws IOException;//-||-ili više fajlova sa zadatom listom imena
    public abstract FileInfo fetchDirectory(String emptyString, String FileName) throws IOException;//vratiti u kom folderu se nalazi fajl sa određenim zadatim imenom

    //public abstract sort();//obezbediti zadavanje različitih kriterijuma sortiranja, npr po nazivu,datumu kreiranje/modifikacije, rastuće/opadajuće
    public abstract List<FileInfo> TouchedAfterInDirectory(String path, LocalDateTime dateTime) throws IOException;//fajlove koji su kreirani/modifikovani u nekom periodu, u nekom dir
    public List<FileInfo> filterResultSet(List<IncludeResult> Criteria, List<FileInfo> fileList){//omogućiti filtriranje podataka koji se prikazuju za fajlove rezultata

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
        switch (criteria){
            case NAME:
                return sortByName(fileList, true);
            case SIZE:
                break;
            case MODIFICATION_DATE:
                break;
        }
        return null;
    }
    //da sortira na osnovu kriterijuma rastuce ili opadajuce (default rastuce)
    private List<FileInfo> sortByName(List<FileInfo> fileList, boolean descending){
        FileInfo[] arr = new FileInfo[fileList.size()];
        arr = fileList.toArray(arr);
        Arrays.sort(arr, new Comparator<FileInfo>(){
            @Override
            public int compare(FileInfo fi1, FileInfo fi2) {
                if(descending)
                    return fi1.getName().compareTo(fi2.getName());
                else
                    return fi2.getName().compareTo(fi1.getName());
            }
        });

        ArrayList<FileInfo> result = new ArrayList<>();
        for(FileInfo fi : arr){
            result.add(fi);
        }
        return result;
    }
    private List<FileInfo> sortByModDate(List<FileInfo> fileList, boolean descending){

        return null;
    }
    private List<FileInfo> sortBySize(List<FileInfo> fileList, boolean descending){

        return null;
    }
}
