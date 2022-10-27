
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
        localStorage.createStorage(Paths.get("C:\\Users\\andri\\Desktop\\SK_Project"));

        localStorage.createDirectory("\\dir1");
        localStorage.createDirectory("\\dir2");



    }
}
