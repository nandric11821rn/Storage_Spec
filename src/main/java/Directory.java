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
    private String path; // \\dir1\\dir2
    private long fileNumberLimit;
    private List<String> files;

    public Directory(String path, long fileNumberLimit, List<String> files) {
        this.name = getName(path);
        this.path = path;
        this.fileNumberLimit = fileNumberLimit;
        this.files = files;
    }

    private String getName(String path) {
        char[] cPath = path.toCharArray();
        int i = cPath.length - 1;
        while (cPath[i] != '\\') {
            i--;
        }

        return path.substring(i + 1);
    }

    public void setName(String name) {
        char[] cPath = path.toCharArray();
        int i = cPath.length - 1;
        while (cPath[i] != '\\') {
            i--;
        }

        this.name = name;
        this.path = path.substring(0, i) + name;
    }
    @Override
    public String toString() {
        return "DIRECTORY\nname: " + name + "\npath: " + path.toString() + "\nfileLimit: " + fileNumberLimit + "\nfiles:" + files.toString();
    }
}
