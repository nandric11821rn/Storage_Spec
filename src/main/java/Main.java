
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
            System.out.println("\n[OPTIONS]\n*0 Quit\n*1 Create single directory\n*2 Create directories using grammar\n*3 Create single file\n*4 Create multiple files" +
                    "\n*5 Rename\n*6 Delete\n*7 Search storage\n\n");
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

                    String in = reader.readLine();
                    if(!in.contains(",")){
                        System.out.println("\n - invalid input\n");
                        continue;
                    }
                    String[] str = in.split(",");

                    String path = str[0];
                    ArrayList<String> names = new ArrayList<>();

                    for(int i = 1; i < str.length; i++){
                        names.add(str[i]);
                    }
                    if(local_storage.createFile(path, names)) System.out.println("\n - files successfully created\n");
                    else System.out.println("\n - invalid input\n");

                    break;
                case 5://preimenuj
                    System.out.println("* Template: \\rootPath\\fileName,newName\n");
                    String s = reader.readLine();
                    if(!s.contains(",")){
                        System.out.println("\n - invalid input\n");
                        continue;
                    }

                    String[] arr = s.split(",");

                    if(local_storage.renameTo(arr[0], arr[1])) System.out.println("\n - file successfully renamed\n");
                    else System.out.println("\n - invalid input\n");

                    break;

                case 6://obrisi
                    System.out.println("* Template: \\rootPath\\fileName\n");
                    if(local_storage.delete(reader.readLine())) System.out.println("\n - file successfully deleted\n");
                    else System.out.println("\n - invalid input\n");
                    break;

                case 7://pretrazi
                    System.out.println("\n[OPTIONS]\n*0 Return\n*1 Search directory\n*2 \n\n");
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
