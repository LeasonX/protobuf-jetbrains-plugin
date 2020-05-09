package io.protostuff.jetbrains.plugin.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import io.protostuff.jetbrains.plugin.ProtoFileType;
import io.protostuff.jetbrains.plugin.bean.ImportableNode;
import io.protostuff.jetbrains.plugin.psi.ProtoRootNode;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class VFSUtil {

    private final static Map<Project, Set<String>> CACHE_FILE_ABSTRACT_PATHS_MAP = new ConcurrentHashMap<>();

    //use memory to reduce time
    private final static Map<Project, Set<String>> CACHE_FILE_RELATIVE_PATHS_MAP = new ConcurrentHashMap<>();

    //key -> project; value -> {key -> file path; value -> importable node}
    private final static Map<Project, Map<String, Set<ImportableNode>>> CACHE_IMPORTABLE_NODE_MAP = new ConcurrentHashMap<>();

    private VFSUtil() {
        throw new UnsupportedOperationException("Utility class can not be instantiated");
    }

    public static Set<String> getFilesRelativePathsOfFolder(Project project) {
        return CACHE_FILE_RELATIVE_PATHS_MAP.get(project);
    }

    public static Map<String, Set<ImportableNode>> getImportableNodeMap(Project project) {
        return CACHE_IMPORTABLE_NODE_MAP.get(project);
    }

    public static String getRelativePathWithImportableNodeText(Project project, String importableNodeText) {
        Map<String, Set<ImportableNode>> importableNodeMap = getImportableNodeMap(project);
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


    public static Set<String> getAllImportableNodeText(Project project) {
        Map<String, Set<ImportableNode>> importableNodeMap = getImportableNodeMap(project);
        //get all importable node
        Collection<Set<ImportableNode>> importableNodeMapValues = importableNodeMap.values();
        Set<ImportableNode> allImportableNodes = new HashSet<>();
        importableNodeMapValues.forEach(allImportableNodes::addAll);
        return allImportableNodes.stream().map(ImportableNode::getName).collect(Collectors.toSet());
    }

    public static void flushProtoPathVFSCache(Project project, String folderPath) {
        if (null == folderPath) {
            return;
        }
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
        CACHE_FILE_ABSTRACT_PATHS_MAP.put(project, projectCacheFileAbstractPaths);
        CACHE_FILE_RELATIVE_PATHS_MAP.put(project, projectCacheFileRelativePaths);
    }

    public static void flushAllImportableMessageOrEnumCache(@NotNull Project project, String folderPath) {
        //full flush(first)
        Set<String> projectProtoFilesPath = CACHE_FILE_ABSTRACT_PATHS_MAP.get(project);
        if (CollectionUtils.isNotEmpty(projectProtoFilesPath)) {
            projectProtoFilesPath.forEach(path -> {
                VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
                if (null != virtualFile) {
                    flushImportableMessageOrEnumCacheFromVirtualFile(project, virtualFile, folderPath);
                }
            });
        }
    }

    public static void flushImportableMessageOrEnumCacheFromVirtualFile(@NotNull Project project, @NotNull VirtualFile virtualFile, String folderPath) {
        //convert to psi file
        PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
        //get all importable message and enum
        ProtoRootNode protoRootNode = PsiTreeUtil.getChildOfType(file, ProtoRootNode.class);
        Set<ImportableNode> messageAndEnumNames = PsiUtil.getImportableNodes(protoRootNode, folderPath);
        Map<String, Set<ImportableNode>> filePathMessageAndEnumNamesMap = CACHE_IMPORTABLE_NODE_MAP.get(project);
        if (null == filePathMessageAndEnumNamesMap) {
            filePathMessageAndEnumNamesMap = new HashMap<>();
        }
        filePathMessageAndEnumNamesMap.put(replaceFileSeparator(virtualFile.getPath()), messageAndEnumNames);
        CACHE_IMPORTABLE_NODE_MAP.put(project, filePathMessageAndEnumNamesMap);
    }

    public static void addVFSChangeListener(Project project, String protoFolderPath) {
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                //flush proto file list
                //flush message or enum that is importable
                for (VFileEvent event : events) {
                    if (needFlushVirtualFileCacheEvent(event)) {
                        flushProtoPathVFSCache(project, protoFolderPath);
                    }
                    VirtualFile virtualFile = event.getFile();
                    if (null != virtualFile) {
                        if (!ProtoFileType.FILE_EXTENSION.equals(virtualFile.getExtension())
                                || !replaceFileSeparator(virtualFile.getPath().toLowerCase()).contains(protoFolderPath.toLowerCase())) {
                            continue;
                        }
                        flushImportableMessageOrEnumCacheFromVirtualFile(project, virtualFile, protoFolderPath);
                    }
                }
            }
        });
    }

    private static boolean needFlushVirtualFileCacheEvent(VFileEvent event) {
        return !(event instanceof VFileContentChangeEvent);
    }

    public static String replaceFileSeparator(String filePath) {
        return filePath.replaceAll("\\\\", "/");
    }

}
