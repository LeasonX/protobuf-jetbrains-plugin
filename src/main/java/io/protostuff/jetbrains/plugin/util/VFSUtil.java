package io.protostuff.jetbrains.plugin.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import io.protostuff.jetbrains.plugin.ProtoFileType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class VFSUtil {

    private final static Map<Project, List<String>> CACHE_FILE_RELATIVE_PATHS_MAP = new ConcurrentHashMap<>();

    private VFSUtil() {
        throw new UnsupportedOperationException("Utility class can not be instantiated");
    }

    public static List<String> getFilesRelativePathsOfFolder(Project project) {
        return CACHE_FILE_RELATIVE_PATHS_MAP.get(project);
    }

    public static void flushProtoPathVFSCache(Project project, String folderPath) {
        if (null == folderPath) {
            return;
        }
        List<String> projectCacheFileRelativePaths = new LinkedList<>();
        Path path = Paths.get(folderPath);
        VirtualFile folderVirtualFile = VfsUtil.findFile(path, true);
        if (null != folderVirtualFile) {
            VfsUtil.processFileRecursivelyWithoutIgnored(folderVirtualFile, virtualFile -> {
                if (ProtoFileType.FILE_EXTENSION.equals(virtualFile.getExtension())) {
                    projectCacheFileRelativePaths.add(replaceFileSeparator(path.relativize(
                            Paths.get(virtualFile.getPath())).toString()));
                }
                return true;
            });
        }
        CACHE_FILE_RELATIVE_PATHS_MAP.put(project, projectCacheFileRelativePaths);
    }

    public static void addVFSChangeListener(Project project, String protoFolderPath) {
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                flushProtoPathVFSCache(project, protoFolderPath);
            }
        });
    }

    public static String replaceFileSeparator(String filePath) {
        return filePath.replaceAll("\\\\", "/");
    }

}
