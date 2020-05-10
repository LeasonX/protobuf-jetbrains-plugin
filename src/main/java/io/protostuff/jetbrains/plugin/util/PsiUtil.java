package io.protostuff.jetbrains.plugin.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeAnyChangeAbstractAdapter;
import com.intellij.psi.util.PsiTreeUtil;
import io.protostuff.jetbrains.plugin.ProtoFileType;
import io.protostuff.jetbrains.plugin.bean.ImportableNode;
import io.protostuff.jetbrains.plugin.cache.ProtoInfoCache;
import io.protostuff.jetbrains.plugin.enums.ImportableType;
import io.protostuff.jetbrains.plugin.psi.EnumNode;
import io.protostuff.jetbrains.plugin.psi.ImportNode;
import io.protostuff.jetbrains.plugin.psi.MessageNode;
import io.protostuff.jetbrains.plugin.psi.ProtoRootNode;
import io.protostuff.jetbrains.plugin.settings.ProtobufSettings;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public final class PsiUtil {

    /**
     * get importable nodes form proto root node
     *
     * @param rootNode   proto root node
     * @param folderPath proto folder path in project settings
     * @return all importable nodes proto root node
     */
    public static Set<ImportableNode> getImportableNodes(ProtoRootNode rootNode, @Nullable String folderPath) {
        Set<ImportableNode> result = new HashSet<>();
        if (null == rootNode) {
            return Collections.emptySet();
        }
        PsiElement[] children = rootNode.getChildren();
        if (0 == children.length) {
            return Collections.emptySet();
        }
        Path path = null;
        if (null != folderPath) {
            path = Paths.get(folderPath);
        }
        for (PsiElement child : children) {
            ImportableNode importableNode = new ImportableNode();
            if (child instanceof MessageNode || child instanceof EnumNode) {
                if (child instanceof MessageNode) {
                    importableNode.setName(((MessageNode) child).getName());
                    importableNode.setImportableType(ImportableType.MESSAGE);
                } else {
                    importableNode.setName(((EnumNode) child).getName());
                    importableNode.setImportableType(ImportableType.ENUM);
                }
                importableNode.setVirtualFile(rootNode.getContainingFile().getVirtualFile());
                if (null != path) {
                    importableNode.setRelativePath(VFSUtil.replaceFileSeparator(path.relativize(
                            Paths.get(importableNode.getVirtualFile().getPath())).toString()));
                }
                result.add(importableNode);
            }
        }
        return result;
    }

    public static Set<String> getMessageAndEnumNames(ProtoRootNode rootNode) {
        if (null == rootNode) {
            return Collections.emptySet();
        }
        ProtobufSettings settings = ProtobufSettings.getInstance(rootNode.getProject());
        String folder = null;
        if (null != settings) {
            folder = settings.getProtoFolder();
        }
        return getImportableNodes(rootNode, folder).stream().map(ImportableNode::getName).collect(Collectors.toSet());
    }

    /**
     * get current file import proto files` importable message and enum names
     *
     * @param protoRootNode proto root node of current file
     * @return importable message and enum names of current file import proto files
     */
    public static Set<String> getImportableMessageAndEnumNames(ProtoRootNode protoRootNode) {
        Set<String> result = new TreeSet<>();
        List<ImportNode> imports = protoRootNode.getImports();
        for (ImportNode anImport : imports) {
            ProtoRootNode targetProto = anImport.getTargetProto();
            result.addAll(getMessageAndEnumNames(targetProto));
        }
        return result;
    }

    public static void addPsiChangeListener(@NotNull Project project, @NotNull String protoFolderPath) {
        PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeAnyChangeAbstractAdapter() {
            @Override
            protected void onChange(@Nullable PsiFile psiFile) {
                if (null != psiFile) {
                    if (!ProtoFileType.FILE_EXTENSION.equalsIgnoreCase(psiFile.getFileType().getName())
                            || !VFSUtil.replaceFileSeparator(psiFile.getVirtualFile().getPath().toLowerCase()).contains(protoFolderPath.toLowerCase())) {
                        return;
                    }
                    flushImportableMessageOrEnumCacheFromPsiFile(project, psiFile, protoFolderPath);
                }
            }
        });
    }

    public static void flushImportableMessageOrEnumCacheFromPsiFile(@NotNull Project project, @NotNull PsiFile psiFile, String folderPath) {
        //get all importable message and enum
        ProtoRootNode protoRootNode = PsiTreeUtil.getChildOfType(psiFile, ProtoRootNode.class);
        Set<ImportableNode> messageAndEnumNames = PsiUtil.getImportableNodes(protoRootNode, folderPath);
        Map<String, Set<ImportableNode>> filePathMessageAndEnumNamesMap = ProtoInfoCache.getImportableNodeMap(project);
        if (null == filePathMessageAndEnumNamesMap) {
            filePathMessageAndEnumNamesMap = new HashMap<>();
        }
        filePathMessageAndEnumNamesMap.put(VFSUtil.replaceFileSeparator(psiFile.getVirtualFile().getPath()), messageAndEnumNames);
        ProtoInfoCache.putProjectImportableNodeMap(project, filePathMessageAndEnumNamesMap);
    }

    public static void flushAllImportableMessageOrEnumCache(@NotNull Project project, @NotNull String folderPath) {
        //full flush(first)
        Set<String> projectProtoFilesAbstractPath = ProtoInfoCache.getProjectCacheFileAbstractPaths(project);
        if (CollectionUtils.isNotEmpty(projectProtoFilesAbstractPath)) {
            projectProtoFilesAbstractPath.forEach(abstractPath -> {
                VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(abstractPath);
                if (null != virtualFile) {
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                    if (null != psiFile) {
                        PsiUtil.flushImportableMessageOrEnumCacheFromPsiFile(project, psiFile, folderPath);
                    }
                }
            });
            //clean file`s importable nodes if has been deleted
            Map<String, Set<ImportableNode>> importableNodeMap = ProtoInfoCache.getImportableNodeMap(project);
            importableNodeMap.keySet().removeIf(key ->!projectProtoFilesAbstractPath.contains(key));
        }
    }
}
