package io.protostuff.jetbrains.plugin.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import io.protostuff.jetbrains.plugin.ProtoFileType;
import io.protostuff.jetbrains.plugin.bean.ImportableNode;
import io.protostuff.jetbrains.plugin.cache.ProtoInfoCache;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public final class VFSUtil {

    private VFSUtil() {
        throw new UnsupportedOperationException("Utility class can not be instantiated");
    }

    public static String getRelativePathWithImportableNodeText(Project project, String importableNodeText) {
        Map<String, Set<ImportableNode>> importableNodeMap = ProtoInfoCache.getImportableNodeMap(project);
        for (Map.Entry<String, Set<ImportableNode>> entry : importableNodeMap.entrySet()) {
            Set<ImportableNode> importableNodes = entry.getValue();
            Map<String, List<ImportableNode>> nameImportableNodesMap = importableNodes
                    .stream()
                    .collect(Collectors.groupingBy(ImportableNode::getName));
            List<ImportableNode> importableNodeList = nameImportableNodesMap.get(importableNodeText);
            if (CollectionUtils.isNotEmpty(importableNodeList)) {
                return importableNodeList.get(0).getRelativePath();
            }
        }
        return null;
    }

//    public static Set<String> getAllImportableNodeText(Project project) {
//        Map<String, Set<ImportableNode>> importableNodeMap = PsiCache.getImportableNodeMap(project);
//        //get all importable node
//        Collection<Set<ImportableNode>> importableNodeMapValues = importableNodeMap.values();
//        Set<ImportableNode> allImportableNodes = new HashSet<>();
//        importableNodeMapValues.forEach(allImportableNodes::addAll);
//        return allImportableNodes.stream().map(ImportableNode::getName).collect(Collectors.toSet());
//    }

    public static void flushProtoPathVFSCache(@NotNull Project project, @NotNull String folderPath) {
        Set<String> projectCacheFileRelativePaths = new HashSet<>();
        Set<String> projectCacheFileAbstractPaths = new HashSet<>();
        Path path = Paths.get(folderPath);
        VirtualFile folderVirtualFile = VfsUtil.findFile(path, true);
        if (null != folderVirtualFile) {
            VfsUtil.processFileRecursivelyWithoutIgnored(folderVirtualFile, virtualFile -> {
                if (ProtoFileType.FILE_EXTENSION.equals(virtualFile.getExtension())) {
                    projectCacheFileAbstractPaths.add(replaceFileSeparator(Paths.get(virtualFile.getPath()).toString()));
                    projectCacheFileRelativePaths.add(replaceFileSeparator(path.relativize(
                            Paths.get(virtualFile.getPath())).toString()));
                }
                return true;
            });
        }
        ProtoInfoCache.putProjectCacheFileAbstractPaths(project, projectCacheFileAbstractPaths);
        ProtoInfoCache.putProjectCacheFileRelativePaths(project, projectCacheFileRelativePaths);
    }

    public static boolean needFlushVirtualFileCacheEvent(VFileEvent event) {
        return !(event instanceof VFileContentChangeEvent);
    }

    public static String replaceFileSeparator(String filePath) {
        return filePath.replaceAll("\\\\", "/");
    }

    public static void addVFSChangeListener(@NotNull Project project, @NotNull String protoFolderPath) {
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                //flush proto file list
                //flush message or enum that is importable
                for (VFileEvent event : events) {
                    if (VFSUtil.needFlushVirtualFileCacheEvent(event)) {
                        VFSUtil.flushProtoPathVFSCache(project, protoFolderPath);
                        //touch off psi tree flush(FIXME optimizable)
                        PsiUtil.flushAllImportableMessageOrEnumCache(project, protoFolderPath);
                    }
                }
            }
        });
    }

    public static String getSimpleFileName(String filePath) {
        int lastSlashIndex = filePath.lastIndexOf('/');
        return -1 == lastSlashIndex ? filePath : filePath.substring(lastSlashIndex + 1);
    }

}
