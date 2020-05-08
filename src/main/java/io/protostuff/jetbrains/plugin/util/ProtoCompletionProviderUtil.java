package io.protostuff.jetbrains.plugin.util;

import com.intellij.codeInsight.completion.AddSpaceInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import io.protostuff.jetbrains.plugin.Icons;
import io.protostuff.jetbrains.plugin.bean.ImportableNode;
import io.protostuff.jetbrains.plugin.psi.ImportNode;
import io.protostuff.jetbrains.plugin.psi.ProtoRootNode;
import io.protostuff.jetbrains.plugin.psi.SyntaxStatement;

import javax.swing.*;

public final class ProtoCompletionProviderUtil {

    private static final String IMPORT_SYNTAX_TEMPLATE = "\nimport \"%s\";";

    public static LookupElement lookupElementWithSpace(String keyword) {
        return LookupElementBuilder.create(keyword).withInsertHandler(AddSpaceInsertHandler.INSTANCE);
    }

    public static LookupElement lookupElement(String keyword) {
        return LookupElementBuilder.create(keyword);
    }

    public static LookupElement lookupImportableWithSpace(String fileName, ImportableNode node) {
        int startIndex = fileName.lastIndexOf('/');
        String simpleFileName;
        if (-1 == startIndex) {
            simpleFileName = fileName;
        } else {
            simpleFileName = fileName.substring(startIndex + 1);
        }
        Icon nodeTypeIcon;
        switch (node.getImportableType()) {
            case ENUM:
                nodeTypeIcon = Icons.ENUM;
                break;
            case MESSAGE:
                nodeTypeIcon = Icons.MESSAGE;
                break;
            default:
                nodeTypeIcon = Icons.FIELD;
        }
        return LookupElementBuilder.create(node.getName())
                .withTypeText(simpleFileName, Icons.PROTO, true)
                .withPresentableText(node.getName())
                .withIcon(nodeTypeIcon)
                .withInsertHandler(
                        (insertionContext, lookupElement) -> {
                            //get psi file
                            PsiFile psiFile = insertionContext.getFile();
                            ProtoRootNode protoRootNode = PsiTreeUtil.getChildOfType(psiFile, ProtoRootNode.class);
                            ImportNode[] importNodes = PsiTreeUtil.getChildrenOfType(protoRootNode, ImportNode.class);
                            Editor editor = insertionContext.getEditor();
                            if (null == importNodes || 0 == importNodes.length) {
                                //insert after `syntax` statement
                                SyntaxStatement syntaxStatement = PsiTreeUtil.getChildOfType(protoRootNode, SyntaxStatement.class);
                                int syntaxStatementTextLength = 0;
                                int syntaxStatementTextOffset = 0;
                                if (null != syntaxStatement) {
                                    syntaxStatementTextLength = syntaxStatement.getTextLength();
                                    syntaxStatementTextOffset = syntaxStatement.getTextOffset();

                                }
                                editor.getDocument().insertString(syntaxStatementTextLength + syntaxStatementTextOffset,
                                        String.format(IMPORT_SYNTAX_TEMPLATE, node.getRelativePath()));
                            } else {
                                //check if must import
                                boolean alreadyImportOrSelf = false;
                                String virtualFilePath = psiFile.getVirtualFile().getCanonicalPath();
                                if (null != virtualFilePath && null != node.getRelativePath()) {
                                    String fixedVirtualFilePath = VFSUtil.replaceFileSeparator(virtualFilePath);
                                    //self
                                    if (fixedVirtualFilePath.contains(node.getRelativePath())) {
                                        alreadyImportOrSelf = true;
                                    } else {
                                        //already import
                                        for (ImportNode importNode : importNodes) {
                                            if (importNode.getText().contains(node.getRelativePath())) {
                                                alreadyImportOrSelf = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (!alreadyImportOrSelf) {
                                    //insert after last `import` statement
                                    ImportNode lastImportNode = importNodes[importNodes.length - 1];
                                    int lastImportNodeTextLength = lastImportNode.getTextLength();
                                    int lastImportNodeTextOffset = lastImportNode.getTextOffset();
                                    editor.getDocument().insertString(lastImportNodeTextOffset + lastImportNodeTextLength,
                                            String.format(IMPORT_SYNTAX_TEMPLATE, node.getRelativePath()));
                                }
                            }
                        });
    }

    public static LookupElement lookupElementWithSlash(String keyword) {
        int startIndex = keyword.lastIndexOf('/');
        if (-1 == startIndex) {
            return lookupElement(keyword);
        }
        String fileName = keyword.substring(startIndex + 1);
        return LookupElementBuilder.create(fileName)
                .withTypeText(keyword.substring(0, startIndex), Icons.FOLDER, true)
                .withPresentableText(fileName)
                .withIcon(Icons.PROTO)
                .withInsertHandler(
                        (insertionContext, lookupElement) -> {
                            Editor editor = insertionContext.getEditor();
                            CaretModel caretModel = editor.getCaretModel();
                            int offset = caretModel.getOffset();
                            editor.getDocument().replaceString(offset - fileName.length(), offset + 1, keyword + "\";");
                            int visualLineEnd = caretModel.getVisualLineEnd();
                            caretModel.moveToOffset(visualLineEnd);
                        });
    }

    public static PsiElement getSuperParent(int level, PsiElement element) {
        PsiElement parent = null;
        for (int i = 0; i < level; i++) {
            parent = element.getParent();
            if (null == parent) {
                break;
            }
            element = parent;
        }
        return parent;
    }
}
