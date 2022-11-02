
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws IOException {
        Local_Storage localStorage = new Local_Storage();
        localStorage.createStorage(Paths.get("C:\\a_test\\SK_Project"));

        localStorage.createDirectory("\\dir1");
        localStorage.createDirectory("\\dir2");
        localStorage.createDirectory("\\dir2\\dir3");
        localStorage.createFile("\\dir2\\atekst.txt");
        localStorage.createFile("\\dir2\\sslika.png");
        localStorage.createFile("\\dir2\\dir3\\ftekaka.txt");
        localStorage.createDirectory("\\dir2\\dir3\\imhppy");
        localStorage.createFile("\\dir2\\dir3\\imhppy\\dtekaka.txt");
        localStorage.createFile("\\dir2\\dir3\\imhppy\\ehpie.png");

        //localStorage.delete("\\dir2\\gooef.jpg");
        //localStorage.delete("\\dir2\\dir3");
       // localStorage.renameTo("\\dir2\\dir3","Wii7");
        //localStorage.renameTo("\\dir2","Wii");
        //localStorage.renameTo("\\dir1","PP");
        //localStorage.renameTo("\\dir2\\gooef.jpg", "image.jpg");
        //if(localStorage.moveFile("\\dir2\\gooef.jpg", "\\dir1")) System.out.println("all good dir");
        //localStorage.moveFile("\\dir2\\gooef.jpg","\\dir1");
        //if(localStorage.download("\\dir2", "C:\\a_copyTest")) System.out.println("all good bro");
        /*List<String> s = new ArrayList<>();
        s.add("tekst.txt");
        s.add("dir");
        s.add("slika.png");
        System.out.println(localStorage.isInDirectory("\\dir2", s));*/
        //System.out.println(localStorage.fetchDirectory("","hpie.png"));

        List<IncludeResult> criteria = new ArrayList<>();
        criteria.add(IncludeResult.MODIFICATION_DATE);
        criteria.add(IncludeResult.SIZE);

        List<FileInfo> fi = localStorage.searchAll("");
        fi = localStorage.filterResultSet(criteria,fi);
        System.out.println(localStorage.sortResultSet(fi, IncludeResult.NAME,false));


    }
}
