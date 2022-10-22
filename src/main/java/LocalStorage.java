import java.io.File;
import java.io.IOException;

public class LocalStorage extends Storage {

    @Override
    public void createRootDirectory(Directory directory) throws IOException {
        File file = new File(directory.getPath());
        File config = new File(directory.getPath() + "\\config.json");
        file.mkdir();
        config.createNewFile();

        manageConfig(directory);

    }

    @Override
    public void createDirectory(String... path) {
        manageConfig(directory);
    }
}
