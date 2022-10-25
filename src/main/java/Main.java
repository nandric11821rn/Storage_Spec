

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
    }
}
