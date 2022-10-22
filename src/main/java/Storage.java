import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Storage {
   /**
     *
     * konfiguracija:
     *      # velicia u bajtovima
     *      # nedozvoljene etstenzije
     *      # koliko fajlova moze da se smesti u odredjeni direktorijum
     *      mkDir c:/mains 1024 [jpg, png, nef]
     *      mkDir c:mains 2039124
     *      mkDir c:mains [jpg, png, nef]
     *
     *      mkDir slike/slika1 size
     * */
    protected void manageConfig(Directory directory) {
        //TODO: Iplementirati konfiguraciju (ako prosledjen null postavlja se default)

    }

    public abstract void createRootDirectory(Directory directory) throws IOException;

    /*public abstract void createRootDirectory(String path, int size);

    public abstract void createRootDirectory(String path, List<String> ext);

    public abstract void createRootDirectory(String path, int size, int fileNum, List<String> ext);*/

    public abstract void createDirectory(String ...path); //TODO: pravljenje vise direktorijuma na vise lokacija




}
