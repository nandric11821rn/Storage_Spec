import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

//TODO: ideja: default- printa se samo ime u toString-u/ inace samo sa true- flagovima
@Getter
@Setter
public class FileInfo {
    private String name;
    private Path absolutePath;

    private String pathFromRoot;
    private boolean pathFlag = true;

    private FileTime creationTime;
    private boolean creationFlag = true;

    private FileTime lastModifiedTime;
    private boolean modifiedFlag = true;

    private long size;
    private boolean sizeFlag = true;

    //TODO: a flagovi da se u funkciji filter postave  (defaultno su false)
    public FileInfo(File file, String pathFromRoot) throws IOException {
        this.name = file.getName();
        this.absolutePath = Paths.get(file.getAbsolutePath()); //za svaki slucaj sto ne bismo imali
        this.pathFromRoot = pathFromRoot;

        BasicFileAttributes attr = Files.readAttributes(absolutePath, BasicFileAttributes.class);
        this.creationTime = attr.creationTime();
        this.lastModifiedTime = attr.lastModifiedTime();
        this.size = attr.size();
    }

    @Override
    public String toString() {

        String s = name + " ---------------\n";
        if(pathFlag) s = s + "path: " + pathFromRoot + "\n";
        if(creationFlag) s = s + "created: " + creationTime.toString() + "\n";
        if(modifiedFlag) s = s + "modified: " + lastModifiedTime.toString() + "\n";
        if(sizeFlag) s = s + "size: " + size + "b\n";

        return s;
    }
}
