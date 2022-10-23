import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Local_Storage localStorage1 = new Local_Storage.Builder()
                .withPath("C:\\Users\\andri\\Desktop\\SK_Project")
                .withSize(10000000000L)
                .build();

        if (localStorage1.createStorage()){
            System.out.println("\u001B[32mSUCCESSFULY CREATED STORAGE\n\u001B[37mLocation: " + localStorage1.getAbsolutePath()
                    + "\nSize: " + localStorage1.getSize() + "bytes"
                    + "\nProhibited extensions: " + localStorage1.getProhibitedExt());
        }

    }
}
