
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.stream.events.Characters;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws IOException {//TODO: ZA SADA KORISTICU SAMO LOKALNI, U SAMOSTALNOJ VERZIJI, ZAMENICEMO SPECIFIKACIJOM!!!

        //TODO: napraviti neki StorageManager i menjanje izabrane implementacije (kao sa vezbi) kad se pretvori u izolovani projekat

        //ovde ce da bude odabir implementacije, SAMO ZA SADA JE:
        Local_Storage storage = new Local_Storage();

        BufferedReader reader = new BufferedReader (new InputStreamReader(System.in));

        while (true){
            System.out.println("* Type location of storage creation: ");
            if(storage.createStorage(reader.readLine())){ //Todo: ako neko unese bezveze tekst, stvorice ti se unutar projekta storage fajl. ispraviti da se ne moze ovo!
                System.out.println("\n - storage successfully created\n");
                break;
            }
            System.out.println("\n - invalid input\n");
        }

        boolean exit = false;
        while(!exit){

            //================pomocne:
            String[] arr; //za splitovanje inputa
            String in; //za input posle odabira metode
            //=======================

            System.out.println("\n[OPTIONS]\n*0 Quit\n*1 Create single directory\n*2 Create directories using grammar\n*3 Create single file\n*4 Create multiple files" +
                    "\n*5 Rename\n*6 Delete\n*7 Move file\n*8 Download file\n*9 Search storage\n\n");
            int input = -1;

            if((input = isInt(reader.readLine())) == -1) continue;

            switch(input){
                case 0://izlazi
                    exit = true;
                    break;

                case 1://napravi jedan direktorijum//TODO: pucaju ti metode ako ima pogresan unos npr ako nije uneta '\' (tacnije metoda getname u Directory)
                    System.out.println("* Template: \\rootPath\\newDirectoryName\n");//TODO: @Nikola implementiraj svoju logiku za ogranicenja onako kako si mislio. ovo je samo kostur
                    if(storage.createDirectory(reader.readLine())) System.out.println("\n - directory successfully created\n");
                    else System.out.println("\n - invalid input\n");
                    break;

                case 2://napravi vise direktorijuma
                    //TODO: @Nikola - ovo je za tvoju logiku da ubacis (dir1[10]>(dir2[10]*5)+dir3>dir4 i takve stvari)
                    CmdParser cmdParser = new CmdParser();
                    if(storage.createDirectory(cmdParser.createDirectories(new StringBuilder(), reader.readLine()))) {
                        System.out.println("\n - directories successfully created\n");
                    }
                    else {
                        System.out.println("\n - invalid input\n");
                    }
                    break;
                case 3://napravi fajl
                    System.out.println("* Template: \\rootPath\\newFileName.extension\n");
                    if(storage.createFile(reader.readLine())) System.out.println("\n - file successfully created\n");
                    else System.out.println("\n - invalid input\n");
                    break;

                case 4://napravi vise fajlova
                    System.out.println("* Template: \\rootPath\\destination,fileName1,fileName2,fileName3,...\n");

                    in = reader.readLine();
                    if(!in.contains(",")){
                        System.out.println("\n - invalid input\n");
                        continue;
                    }
                    arr = in.split(",");

                    String path = arr[0];
                    ArrayList<String> names = new ArrayList<>();

                    for(int i = 1; i < arr.length; i++){
                        names.add(arr[i]);
                    }
                    if(storage.createFile(path, names)) System.out.println("\n - files successfully created\n");
                    else System.out.println("\n - invalid input\n");

                    break;
                case 5://preimenuj
                    System.out.println("* Template: \\rootPath\\fileName,newName\n");
                    in = reader.readLine();
                    if(!in.contains(",")){
                        System.out.println("\n - invalid input\n");
                        continue;
                    }

                    arr = in.split(",");

                    if(storage.renameTo(arr[0], arr[1])) System.out.println("\n - file successfully renamed\n");
                    else System.out.println("\n - invalid input\n");

                    break;

                case 6://obrisi
                    System.out.println("* Template: \\rootPath\\fileName\n");
                    if(storage.delete(reader.readLine())) System.out.println("\n - file successfully deleted\n");
                    else System.out.println("\n - invalid input\n");
                    break;

                case 7://pomeri
                    System.out.println("* Template: \\rootPath\\fileName,\\rootPath\\destinationFolder\n");
                    in = reader.readLine();
                    if(!in.contains(",")){
                        System.out.println("\n - invalid input\n");
                        continue;
                    }
                    arr = in.split(",");

                    if(storage.moveFile(arr[0], arr[1])) System.out.println("\n - file successfully moved\n");
                    else System.out.println("\n - invalid input\n");
                    break;

                case 8://preuzmi
                    System.out.println("* Template: \\rootPath\\fileName,DestinationAbsolutePath\n");
                    in = reader.readLine();
                    if(!in.contains(",")){
                        System.out.println("\n - invalid input\n");
                        continue;
                    }
                    arr = in.split(",");

                    if(storage.download(arr[0], arr[1])) System.out.println("\n - file successfully downloaded\n");
                    else System.out.println("\n - invalid input\n");
                    break;

                case 9://pretrazi------------------------------------------------------
                    boolean back = false;
                    while(!back) {
                        System.out.println("\n[SEARCH OPTIONS:]\n*0 return\n*1 Search directory\n*2 Search subdirectories\n*3 Search to directory's deepest level\n*4 By extension\n*5 By substring\n" +
                                "*6 Is file in directory\n*7 Are files in directory\n*8 Fetch directory\n*9 Modified after\n\n");
                        List<FileInfo> resultSet = null;
                        String[] split;
                        String str;

                        int search = -1;
                        if((search = isInt(reader.readLine())) == -1) continue;

                        switch (search){
                            case 0://back
                                back = true;
                                break;

                            case 1://search directory
                                System.out.println("* Template: \\rootPath\\directoryName\n");
                                resultSet = storage.searchDirectory(reader.readLine());

                                if(resultSet == null){
                                    System.out.println("\n - file doesn't exist\n");
                                    continue;
                                }
                                dealWithResultSet(resultSet,storage,reader);

                                break;

                            case 2: //subdirectories
                                System.out.println("* Template: \\rootPath\\directoryName\n");
                                resultSet = storage.searchSubdirectories(reader.readLine());

                                if(resultSet == null){
                                    System.out.println("\n - file doesn't exist\n");
                                    continue;
                                }
                                dealWithResultSet(resultSet,storage,reader);
                                break;

                            case 3://all
                                System.out.println("* Template: \\rootPath\\directoryName\n");
                                resultSet = storage.searchAll(reader.readLine());

                                if(resultSet == null){
                                    System.out.println("\n - file doesn't exist\n");
                                    continue;
                                }
                                dealWithResultSet(resultSet,storage,reader);
                                break;

                            case 4://extension
                                System.out.println("* Template: .(extension)");
                                resultSet = storage.searchByExtension(reader.readLine());

                                if(resultSet == null){
                                    System.out.println("\n - no files with that extension found\n");
                                    continue;
                                }
                                dealWithResultSet(resultSet,storage,reader);
                                break;

                            case 5://substring
                                System.out.println("* Template: Any substring");
                                resultSet = storage.searchBySubstring(reader.readLine());

                                if(resultSet == null){
                                    System.out.println("\n - no files with that substring found\n");
                                    continue;
                                }
                                dealWithResultSet(resultSet,storage,reader);
                                break;

                            case 6://isInDirectory(1)
                                System.out.println("* Template: \\rootPath\\directoryName,filename\n");
                                str = reader.readLine();
                                if(!str.contains(",")){
                                    System.out.println("\n - invalid input\n");
                                    break;
                                }
                                split = str.split(",");
                                if(storage.isInDirectory(split[0],split[1]))
                                    System.out.println("\n - file " + split[1] + " is in directory " + split[0] + ".\n");
                                else
                                    System.out.println("\n - file " + split[1] + " is not in directory " + split[0] + ".\n");

                                break;

                            case 7://isInDirectory(1+)
                                System.out.println("* Template: \\rootPath\\directoryName,filename1,filename2,...\n");
                                str = reader.readLine();
                                if(!str.contains(",")){
                                    System.out.println("\n - invalid input\n");
                                    break;
                                }
                                split = str.split(",");

                                List<String> files = new ArrayList<>();
                                for(int i = 1; i < split.length; i++){
                                    files.add(split[i]);
                                }
                                if(storage.isInDirectory(split[0],files))
                                    System.out.println("\n - files are in directory " + split[0] + ".\n");
                                else
                                    System.out.println("\n - not all files are in directory " + split[0] + ".\n");
                                break;

                            case 8://fetch directory
                                System.out.println("* Template: filename\n");
                                str = reader.readLine();
                                FileInfo parent;

                                parent = storage.fetchDirectory("", str);
                                if(parent == null)
                                    System.out.println("\n - file "+ str +" has not been found\n");
                                else
                                    System.out.println("\n - file "+ str +" is in directory:\n"+parent);

                                break;

                            case 9://modified after
                                System.out.println("* Template: \\rootPath\\directoryName,dd/mm/yyyy-hh:mm:ss\n");

                                str = reader.readLine();
                                if(!str.contains(",")){
                                    System.out.println("\n - invalid input\n");
                                    break;
                                }

                                split = str.split(",");

                                DateFormat df = new SimpleDateFormat("dd/mm/yyyy-hh:mm:ss");
                                Date dt = null;
                                try {
                                    dt = df.parse(split[1]);
                                } catch (ParseException e) {
                                    System.out.println("\n - invalid date format\n");
                                    break;
                                }
                                resultSet = storage.touchedAfterInDirectory(split[0], dt);

                                if(resultSet == null)
                                    System.out.println("\n - no such files in directory " + split[0] + ".\n");

                                dealWithResultSet(resultSet, storage, reader);

                                break;
                        }
                    }
                    break;

                default:
                    System.out.println("\n - invalid request\n");
            }
        }

       /* Local_Storage localStorage = new Local_Storage();
        localStorage.createStorage("C:\\a_test\\SK_Project");*/
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

    public static void dealWithResultSet(List<FileInfo> resultSet, Local_Storage storage, BufferedReader reader) throws IOException {
        List<FileInfo> toPrint = resultSet;
        boolean flag = true;
        while(flag){
            System.out.println("\n[RESULT OPTIONS:]\n*1 Print and return\n*2 Sort\n*3 Filter\n\n");
            String input;
            String[] split;

            int pick = -1;
            if((pick = isInt(reader.readLine())) == -1) continue;

            switch (pick){
                case 1: //stampaj
                    System.out.println("\n");
                    for(FileInfo f : toPrint){
                        System.out.println(f);
                    }
                    flag = false;
                    break;

                case 2://sortiraj
                    System.out.println("\n[sort by:]\n*1 Name\n*2 Size\n*3 Date\n---------\n*a ascending\n*d descending\n---------\n* Template: (a or d),(a number from 1 to 3)\nex: a,2\n");
                    input = reader.readLine();
                    if(!input.contains(",")){
                        System.out.println("\n - invalid input\n");
                        break;
                    }
                    split = input.split(",");
                    boolean desc;

                    if(split[0].equalsIgnoreCase("a")) desc = false;
                    else desc = true;

                    if(split[1].equals("1"))
                        toPrint = storage.sortResultSet(toPrint, IncludeResult.NAME, desc);
                    else if(split[1].equals("2"))
                        toPrint = storage.sortResultSet(toPrint, IncludeResult.SIZE, desc);
                    else if(split[1].equals("3"))
                        toPrint = storage.sortResultSet(toPrint, IncludeResult.MODIFICATION_DATE, desc);
                    else System.out.println("\n - invalid input\n");
                    System.out.println("\n- sorted\n");
                    break;

                case 3://filterisi
                    System.out.println("\n[include:]\n*1 Size\n*2 Root path\n*3 Creation date\n*4 Modification date\n---------\n* Template: (1-4),(1-4),...\n");
                    input = reader.readLine();
                    split = input.split(",");

                    if(split.length == 0){
                        System.out.println("\n- no attributes added");
                        continue;
                    }
                    List<IncludeResult> criteria = new ArrayList<>();
                    for(String attr : split){
                        if(attr.equals("1"))
                            criteria.add(IncludeResult.SIZE);
                        else if(attr.equals("2"))
                            criteria.add(IncludeResult.ROOT_PATH);
                        else if(attr.equals("3"))
                            criteria.add(IncludeResult.CREATION_DATE);
                        else if(attr.equals("4"))
                            criteria.add(IncludeResult.MODIFICATION_DATE);
                        else{
                            System.out.println("\n - invalid input\n");
                            break;
                        }
                    }
                    toPrint = storage.filterResultSet(criteria,toPrint);
                    System.out.println("\n- filtered\n");
                    break;
            }

        }
    }

    public static int isInt(String str){ //provera pri biranju opcija
        int input;
        try{ //u slucaju da neko unese nesto sto nije broj a da treba broj
            input = Integer.valueOf(str);
        } catch (NumberFormatException e) {
            System.out.println("\n - invalid input\n");
            return -1;
        }
        return input;
    }
}
