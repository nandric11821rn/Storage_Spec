import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CmdParser {


    private  String getName(String cmdLine) {
        if (cmdLine.contains("[")) {
            return cmdLine.substring(0, cmdLine.indexOf('['));
        } else if (cmdLine.contains("*")) {
            return cmdLine.substring(0, cmdLine.indexOf('*'));
        }
        return cmdLine;
    }
    private  List<Directory> createDirPom(int num, int fileNum, String path) {
        List<Directory> directories = new ArrayList<>();
        if (num == 0) {
            directories.add(new Directory(path, fileNum, new ArrayList<>()));
        }
        for (int i = 1; i <= num; i++) {
            directories.add(new Directory(path + Integer.toString(i), fileNum, new ArrayList<>()));
        }
        return directories;
    }
    private  int getFileNum(String cmdLine) {
        if (cmdLine.contains("[")) {
            return Integer.parseInt(cmdLine.substring(cmdLine.indexOf("[") + 1, cmdLine.indexOf("]")));
        }
        return -1;
    }
    private  int getNumberOfDirectories(String cmdLine) {
        if (cmdLine.contains("*")) {
            return Integer.parseInt(cmdLine.substring(cmdLine.indexOf("*") + 1));
        }
        return 0;
    }
    private int spliterIndex(String s) {

        int a = s.contains(">") ? s.indexOf('>') : s.length();
        int b = s.contains("+") ? s.indexOf('+') : s.length();

        return Math.min(a, b);
    }

    public List<Directory> createDirectories(StringBuilder path, String cmdLine) {
        List<Directory> directories = new ArrayList<>();

        int endIndex = spliterIndex(cmdLine);

        while(cmdLine.length() != 0) {
            String s = cmdLine.substring(0, endIndex);
            if (!s.isEmpty()) {

                if (s.contains("(")) {
                    //System.out.println(cmdLine.substring(cmdLine.indexOf("(") + 1, cmdLine.indexOf(")")));
                    StringBuilder pomPath = new StringBuilder(path);
                    directories.addAll(createDirectories(pomPath, cmdLine.substring(cmdLine.indexOf("(") + 1, cmdLine.indexOf(")"))));

                    endIndex = cmdLine.indexOf(")");
                    cmdLine = cmdLine.substring(endIndex + 1);
                    endIndex = spliterIndex(cmdLine);
                    continue;
                }

                directories.addAll(createDirPom(getNumberOfDirectories(s), getFileNum(s), path + "\\" + getName(s)));
                if (endIndex == cmdLine.length()) {
                    break;
                }

                if (cmdLine.contains(">") && cmdLine.charAt(endIndex) == '>') {
                    path.append("\\").append(getName(s));
                }
            }

            cmdLine = cmdLine.substring(endIndex + 1);
            endIndex = spliterIndex(cmdLine);
        }
        return directories;
    }
}
