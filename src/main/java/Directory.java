import java.util.List;

public class Directory {
    private String path;
    private int size;
    private List<String> ext;
    private int fileNum;

    public String getPath() {
        return path;
    }

    public int getSize() {
        return size;
    }

    public List<String> getExt() {
        return ext;
    }

    public int getFileNum() {
        return fileNum;
    }

    public Directory(DirectoryBuilder builder) {
        this.path = builder.path;
        this.size = builder.size;
        this.ext = builder.ext;
        this.fileNum = builder.fileNum;
    }

    public static class DirectoryBuilder {
        private String path;
        private int size;
        private List<String> ext;
        private int fileNum;

        public DirectoryBuilder withPath(String path) {
            this.path = path;
            return this;
        }

        public DirectoryBuilder withSize(int size) {
            this.size = size;
            return this;
        }

        public DirectoryBuilder withExt(List<String> ext) {
            this.ext = ext;
            return this;
        }
        public DirectoryBuilder withFileNum(int fileNum) {
            this.fileNum = fileNum;
            return this;
        }

        public Directory build() {
            return new Directory(this);
        }

    }
}
