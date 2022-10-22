import java.io.File;
import java.io.IOException;

public class main {
    public static void main(String[] args) throws IOException {
        String cmd = "mkDir p-C:\\Users\\andri\\Desktop\\SK\\Vezbe s-1024 ext-[jpg,png,nef]";
        String[] arr = cmd.split(" ");
        String method = "mkDir";

        LocalStorage ls = new LocalStorage();
        File f = new File("\\Users\\andri\\Desktop\\config.json");
        f.createNewFile();

        switch (method) {
            case "mkDir":
                Directory directory = new Directory.DirectoryBuilder()
                        .withPath("C:\\Users\\andri\\Desktop\\SK\\Vezbe")
                        .withSize(1024)
                        .withFileNum(5)
                        .build();
                ls.createRootDirectory(directory);
                break;

        }


    }
}
