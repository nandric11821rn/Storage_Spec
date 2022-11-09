
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.stream.events.Characters;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws IOException {//TODO: ZA SADA KORISTICU SAMO LOKALNI, U SAMOSTALNOJ VERZIJI, ZAMENICEMO SPECIFIKACIJOM!!!

        //TODO: napraviti neki StorageManager i menjanje izabrane implementaije (kao sa vezbi) kad se pretvori u izolovani projekat

        //ovde ce da bude odabir implementacije, SAMO ZA SADA JE:
        Local_Storage local_storage = new Local_Storage();

        BufferedReader reader = new BufferedReader (new InputStreamReader(System.in));

        while (true){
            System.out.println("* Type location of storage creation: ");
            if(local_storage.createStorage(reader.readLine())){ //Todo: ako neko unese bezveze tekst, stvorice ti se unutar projekta storage fajl. ispraviti da se ne moze ovo!
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
                    if(local_storage.createDirectory(reader.readLine())) System.out.println("\n - directory successfully created\n");
                    else System.out.println("\n - invalid input\n");
                    break;

                case 2://napravi vise direktorijuma
                    //TODO: @Nikola - ovo je za tvoju logiku da ubacis (dir1[10]>(dir2[10]*5)+dir3>dir4 i takve stvari)
                    CmdParser cmdParser = new CmdParser();
                    if(local_storage.createDirectory(cmdParser.createDirectories(new StringBuilder(), reader.readLine()))) {
                        System.out.println("\n - directories successfully created\n");
                    }
                    else {
                        System.out.println("\n - invalid input\n");
                    }
                    break;
                case 3://napravi fajl
                    System.out.println("* Template: \\rootPath\\newFileName.extension\n");
                    if(local_storage.createFile(reader.readLine())) System.out.println("\n - file successfully created\n");
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
                    if(local_storage.createFile(path, names)) System.out.println("\n - files successfully created\n");
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

                    if(local_storage.renameTo(arr[0], arr[1])) System.out.println("\n - file successfully renamed\n");
                    else System.out.println("\n - invalid input\n");

                    break;

                case 6://obrisi
                    System.out.println("* Template: \\rootPath\\fileName\n");
                    if(local_storage.delete(reader.readLine())) System.out.println("\n - file successfully deleted\n");
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

                    if(local_storage.moveFile(arr[0], arr[1])) System.out.println("\n - file successfully moved\n");
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

                    if(local_storage.download(arr[0], arr[1])) System.out.println("\n - file successfully downloaded\n");
                    else System.out.println("\n - invalid input\n");
                    break;

                case 9://pretrazi------------------------------------------------------
                    boolean back = false;
                    while(!back) {
                        System.out.println("\n[SEARCH OPTIONS:]\n*0 return\n*1 Search directory\n*2 Search subdirectories\n*3 Search all\n*4 By extension\n*5 By substring\n" +
                                "*6 Is file in directory\n*7 Are files in directory\n*8 Fetch directory\n*9 Modified after\n\n");
                        int search = -1;
                        if((search = isInt(reader.readLine())) == -1) continue;

                        switch (search){
                            case 0://back
                                back = true;
                                break;

                            case 1://search directory
                                System.out.println("* Template: \\rootPath\\directoryName\n");
                                List<FileInfo> resultSet = local_storage.searchDirectory(reader.readLine());

                                if(resultSet == null){
                                    System.out.println("\n - file doesn't exist\n");
                                    continue;
                                }
                                dealWithResultSet(resultSet,local_storage,reader);

                                break;

                            case 2: //subdirectories

                                break;

                            case 3://all

                                break;

                            case 4://extension

                                break;

                            case 5://substring

                                break;

                            case 6://isInDirectory(1)

                                break;

                            case 7://isInDirectory(1+)

                                break;

                            case 8://fetch directory

                                break;

                            case 9://modified after

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
        boolean flag = true;
        while(flag){
            System.out.println("\n[RESULT OPTIONS:]\n*1 Print\n*2 Sort\n*3 Filter");
            int pick = -1;
            if((pick = isInt(reader.readLine())) == -1) continue;

            switch (pick){
                case 1: //stampaj
                    System.out.println("\n");
                    for(FileInfo f : resultSet){
                        System.out.println(f);
                    }
                    flag = false;
                    break;

                case 2://sortiraj

                    break;

                case 3://filterisi

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
