package lk.ac.iit.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileFilter {

    public static void main(String... args) {
        try {
            long modifiedTime = getSetUIDFileModifiedTime(Paths.get("/home/user/IIT"), "mysetuidfile.txt");
            displayResults(getFilesHandledBetweenTime(Paths.get("/home/user/IIT"), modifiedTime * 1000, (modifiedTime *
                    1000
            + (30 * 60 * 1000)), FileOperation.MODIFIED));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static List<Path> getFilesHandledBetweenTime(Path directory, long startTime, long endTime, FileOperation
            operation) throws IOException {
        List<Path> filteredFiles = new ArrayList<>();

        if (directory != null) {
            List<Path> files = listFiles(directory);
            for (Path file : files) {
                BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);

                switch (operation) {
                    case CREATED:
                        long creationTime = attributes.creationTime().toMillis();
                        if ((creationTime > startTime) && (creationTime < endTime)) {
                            filteredFiles.add(file);
                        }
                        break;
                    case ACCESSED:
                        long accessedTime = attributes.lastAccessTime().toMillis();
                        if ((accessedTime > startTime) && (accessedTime < endTime)) {
                            filteredFiles.add(file);
                        }
                        break;
                    case MODIFIED:
                        long modifiedTime = attributes.lastModifiedTime().toMillis();
                        if ((modifiedTime > startTime) && (modifiedTime < endTime)) {
                            filteredFiles.add(file);
                        }
                        break;
                }
            }
        }

        return filteredFiles;
    }

    private static long getSetUIDFileModifiedTime(Path directory, String fileName) throws IOException,
            InterruptedException {
        String find = "find " + directory.toAbsolutePath().toString() + " -user root -name " + fileName +
                " -printf \'%A@\\n\' -perm 4000";
        System.out.println(find);
        String[] commands = {"/bin/bash", "-c", "echo 123456| sudo -S " + find};

        Process process = Runtime.getRuntime().exec(commands);
        BufferedReader br = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        String output = br.readLine();

        process.waitFor();
        System.out.println("exit: " + process.exitValue());
        process.destroy();

        //  in seconds
        return (long) (Double.parseDouble(output));
    }

    private static List<Path> listFiles(Path directory) throws IOException {
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    files.addAll(listFiles(entry));
                } else {
                    files.add(entry);
                }
            }
        }
        return files;
    }

    private static void displayResults(List<Path> files) {
        Optional.ofNullable(files)
                .ifPresent(list -> list.stream()
                        .forEach(file -> {
                            try {
                                BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
                                System.out.println(file.toString() + "\tcreation time: " + attributes.creationTime() +
                                        "\tlast access time: " + attributes.lastAccessTime() + "\tlast modified time: " +
                                        attributes.lastModifiedTime());
                            } catch (IOException e) {
                                // ignore the exception
                            }
                        }));
    }
}
