package io.protostuff.jetbrains.plugin.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import io.protostuff.jetbrains.plugin.ProtoFileType;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class VFSUtil {

    private final static List<String> CACHE_FILE_RELATIVE_PATHS = new CopyOnWriteArrayList<>();

    private VFSUtil() {
        throw new UnsupportedOperationException("Utility class can not be instantiated");
    }

    public static List<String> getFilesRelativePathsOfFolder() {
        return CACHE_FILE_RELATIVE_PATHS;
    }

    public static void flushProtoPathVFSCache(String folderPath) {
        CACHE_FILE_RELATIVE_PATHS.clear();
        Path path = Paths.get(folderPath);
        VirtualFile folderVirtualFile = VfsUtil.findFile(path, true);
        if (null != folderVirtualFile) {
            VfsUtil.processFileRecursivelyWithoutIgnored(folderVirtualFile, virtualFile -> {
                if (ProtoFileType.FILE_EXTENSION.equals(virtualFile.getExtension())) {
                    CACHE_FILE_RELATIVE_PATHS.add(path.relativize(Paths.get(virtualFile.getPath())).toString());
                }
                return true;
            });
        }
    }

    public static void addVFSChangeListener(Project project, String protoFolderPath) {
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                flushProtoPathVFSCache(protoFolderPath);
            }
        });
    }

}
