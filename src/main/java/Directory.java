import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.file.Path;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Directory {

    private String name;
    private Path path;
    private long fileNumberLimit;
    private List<String> files;

    public Directory(Path path, long fileNumberLimit, List<String> files) {
        this.name = path.getFileName().toString();
        this.path = path;
        this.fileNumberLimit = fileNumberLimit;
        this.files = files;
    }

    @Override
    public String toString() {
        return "DIRECTORY\nname: " + name + "\npath: " + path.toString() + "\nfileLimit: " + fileNumberLimit + "\nfiles:" + files.toString();
    }
}
