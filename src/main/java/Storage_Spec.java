import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
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

    protected long fileNum;

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

    public abstract void createDirectory(String path) throws IOException;
    public abstract void createDirectory(String path, long fileNum) throws IOException;
    public abstract void createDirectory(String path, List<Directory> directories) throws IOException;
    public abstract void createDirectory(String path, Map<String, Integer> directories) throws IOException;
    public abstract void createFile(String path) throws IOException;
    public abstract void createFile(String path, List<String> names) throws IOException;
    public abstract void delete(String path) throws IOException;
    public abstract void renameTo(String path, String newName) throws IOException;

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
     */


}
