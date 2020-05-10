package io.protostuff.jetbrains.plugin.util;

import com.intellij.codeInsight.completion.AddSpaceInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import io.protostuff.jetbrains.plugin.Icons;
import io.protostuff.jetbrains.plugin.bean.ImportableNode;
import io.protostuff.jetbrains.plugin.psi.ImportNode;
import io.protostuff.jetbrains.plugin.psi.ProtoRootNode;
import io.protostuff.jetbrains.plugin.psi.SyntaxStatement;

import javax.swing.Icon;

public final class ProtoCompletionProviderUtil {

    private static final String IMPORT_SYNTAX_TEMPLATE = "\nimport \"%s\";";

    public static LookupElement lookupElementWithSpace(String keyword) {
        return LookupElementBuilder.create(keyword).withInsertHandler(AddSpaceInsertHandler.INSTANCE);
    }

    public static LookupElement lookupElement(String keyword) {
        return LookupElementBuilder.create(keyword);
    }

    public static LookupElement lookupImportableWithSpace(String fileName, ImportableNode node) {
        String simpleFileName = VFSUtil.getSimpleFileName(fileName);
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
                            Editor editor = insertionContext.getEditor();
                            autoCompleteImportNode(psiFile, editor, node.getRelativePath());
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

    public static void autoCompleteImportNode(PsiFile psiFile, Editor editor, String importableFileRelativePath) {

        ProtoRootNode protoRootNode = PsiTreeUtil.getChildOfType(psiFile, ProtoRootNode.class);
        ImportNode[] importNodes = PsiTreeUtil.getChildrenOfType(protoRootNode, ImportNode.class);

        if (null == importNodes || 0 == importNodes.length) {
            //insert after `syntax` statement
            SyntaxStatement syntaxStatement = PsiTreeUtil.getChildOfType(protoRootNode, SyntaxStatement.class);
            if (null != syntaxStatement) {
                int syntaxStatementTextLength = syntaxStatement.getTextLength();
                int syntaxStatementTextOffset = syntaxStatement.getTextOffset();
                //add extra blank line before
                editor.getDocument().insertString(syntaxStatementTextLength + syntaxStatementTextOffset,
                        "\n" + String.format(IMPORT_SYNTAX_TEMPLATE, importableFileRelativePath));
            } else {
                //add extra blank line after
                editor.getDocument().insertString(0, String.format(IMPORT_SYNTAX_TEMPLATE, importableFileRelativePath) + "\n");
            }
        } else {
            //check if must import
            boolean alreadyImportOrSelf = false;
            String virtualFilePath = psiFile.getVirtualFile().getCanonicalPath();
            if (null != virtualFilePath && null != importableFileRelativePath) {
                String fixedVirtualFilePath = VFSUtil.replaceFileSeparator(virtualFilePath);
                //self
                if (fixedVirtualFilePath.contains(importableFileRelativePath)) {
                    alreadyImportOrSelf = true;
                } else {
                    //already import
                    for (ImportNode importNode : importNodes) {
                        if (importNode.getText().contains(importableFileRelativePath)) {
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
                        String.format(IMPORT_SYNTAX_TEMPLATE, importableFileRelativePath));
            }
        }
    }
}
