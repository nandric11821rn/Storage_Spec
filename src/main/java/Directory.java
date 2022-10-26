import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Directory {
    private String path;
    private long fileNumberLimit;
    private List<String> files;

    public Directory(String path, long fileNumberLimit, List<String> files) {
        this.path = path;
        this.fileNumberLimit = fileNumberLimit;
        this.files = new ArrayList<>(files);
    }
}
