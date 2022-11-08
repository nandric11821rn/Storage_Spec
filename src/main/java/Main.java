
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
        localStorage.createStorage("C:\\a_test\\SK_Project");

        localStorage.createDirectory("\\dir1");
        localStorage.createDirectory("\\dir2");
        localStorage.createDirectory("\\dir2\\dir3");
        localStorage.createFile("\\dir2\\atekst.txt");
        localStorage.createFile("\\dir2\\sslika.png");
        localStorage.createFile("\\dir2\\dir3\\ftekaka.txt");
        localStorage.createDirectory("\\dir2\\dir3\\imhppy");
        localStorage.createFile("\\dir2\\dir3\\imhppy\\dtekaka.txt");
        localStorage.createFile("\\dir2\\dir3\\imhppy\\ehpie.png");

        List<FileInfo> fi = localStorage.searchDirectory("\\dir2");

        List<IncludeResult> criteria = new ArrayList<>();
        criteria.add(IncludeResult.MODIFICATION_DATE);
        criteria.add(IncludeResult.SIZE);

        fi = localStorage.filterResultSet(criteria, fi);
        System.out.println(localStorage.sortResultSet(fi,IncludeResult.NAME,false));

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

       /* List<IncludeResult> criteria = new ArrayList<>();
        criteria.add(IncludeResult.MODIFICATION_DATE);
        criteria.add(IncludeResult.SIZE);

        List<FileInfo> fi = localStorage.searchAll("");
        fi = localStorage.filterResultSet(criteria,fi);
        System.out.println(localStorage.sortResultSet(fi, IncludeResult.NAME,false));*/

        //CMD TEST:
//        CmdParser cmdParser = new CmdParser();
//
//        String cmdLine = "dir1[10]>(dir2[10]*5)+dir3>dir4";
//        List<Directory> directories = cmdParser.createDirectories(new StringBuilder(), cmdLine);
//        System.out.println(directories);
//
//
//        //Remote_Storage TEST:
//        Remote_Storage remote_storage = new Remote_Storage();
//        remote_storage.createStorage("Test1");
//        remote_storage.createDirectory(directories);
//        remote_storage.createFile("\\dir1\\text.txt");

    }
}
