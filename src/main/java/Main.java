

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        /*long a = Long.MAX_VALUE;
        Local_Storage localStorage1 = new Local_Storage.Builder()
                .withPath("C:\\Users\\andri\\Desktop\\SK_Project")
                .withSize(10000000000L)
                .build();

        if (localStorage1.createStorage()){
            System.out.println("\u001B[32mSUCCESSFULY CREATED STORAGE\n\u001B[37mLocation: " + localStorage1.getAbsolutePath()
                    + "\nSize: " + localStorage1.getSize() + "bytes"
                    + "\nProhibited extensions: " + localStorage1.getProhibitedExt());
        }*/

        Local_Storage localStorage = new Local_Storage();

        //localStorage.createStorage("C:\\a_test\\Pokusaj", 1000000, new ArrayList<>(Arrays.asList("png", "jpg")));
        //localStorage.createDirectory("C:\\a_test\\Pokusaj\\prpica.jpg");
        //localStorage.renameTo("C:\\a_test\\Pokusaj\\prpica.jpg", "papanovagvineja.jpg");
        //localStorage.delete("C:\\a_test\\Pokusaj\\papanovagvineja.jpg");
        //localStorage.createFile("C:\\a_test\\Pokusaj\\capture.png");

       /* if(localStorage.createStorage("C:\\Users\\andri\\Desktop\\SK_Project", 1000000, new ArrayList<>(Arrays.asList("png", "jpg")))) {
            System.out.println("Napravljeno skladiste");
        }

        List<Directory> directories = new ArrayList<>();
        directories.add(new Directory("\\dir2", 10, new ArrayList<String>()));
        directories.add(new Directory("\\dir3", 10, new ArrayList<String>()));
        directories.add(new Directory("\\dir4", 10, new ArrayList<String>()));
        //directories.add(new Directory("\\dir5\\dir6\\dir7", 10, new ArrayList<String>()));

        if(localStorage.createDirectory("\\dir1", 10)) {
            System.out.println("Napravljen direktorijum");
        }*/

        /*if (localStorage.createDirectory(directories)) {
            System.out.println("Napravljeni direktorijumi");
        }*/

        //TODO: ne pravi se stvarno fajl ali se registruju u nizu direktorijuma
        if(localStorage.createStorage("C:\\a_test\\Pokusaj1")) System.out.println("napravljen storage1 \n");
        if(localStorage.createDirectory("\\Dir1", 10)) System.out.println("napravljen dir1 \n");
        if(localStorage.createDirectory("\\Dir2", 5)) System.out.println("napravljen dir1 \n");
        if(localStorage.createDirectory("\\Dir1\\Dir3", 53)) System.out.println("napravljen dir1 \n");
        localStorage.createFile("\\Dir1\\Dir3\\proba.jpg");
    }
}
