package io.protostuff.jetbrains.plugin.util;

import com.intellij.psi.PsiElement;
import io.protostuff.jetbrains.plugin.bean.ImportableNode;
import io.protostuff.jetbrains.plugin.enums.ImportableType;
import io.protostuff.jetbrains.plugin.psi.EnumNode;
import io.protostuff.jetbrains.plugin.psi.ImportNode;
import io.protostuff.jetbrains.plugin.psi.MessageNode;
import io.protostuff.jetbrains.plugin.psi.ProtoRootNode;
import io.protostuff.jetbrains.plugin.settings.ProtobufSettings;
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
}
