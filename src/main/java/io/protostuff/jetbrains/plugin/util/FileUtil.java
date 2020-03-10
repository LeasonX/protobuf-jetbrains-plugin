package io.protostuff.jetbrains.plugin.util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

public final class FileUtil {

    private static final PathMatcher PROTO_PATH_MATCHER = FileSystems.getDefault().getPathMatcher("glob:**.proto");

    private FileUtil() {
        throw new UnsupportedOperationException("Utility class can not be instantiated");
    }

    public static List<String> getFilesRelativePathsOfFolder(String folderPath) {
        Path path = Paths.get(folderPath);
        List<String> fileRelativePaths = new LinkedList<>();
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (PROTO_PATH_MATCHER.matches(file)) {
                        fileRelativePaths.add(path.relativize(file).toString());
                    }
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileRelativePaths;
    }

}
