package io.protostuff.jetbrains.plugin.util;

import com.intellij.codeInsight.completion.AddSpaceInsertHandler;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.psi.PsiElement;
import io.protostuff.jetbrains.plugin.Icons;
import io.protostuff.jetbrains.plugin.psi.EnumNode;
import io.protostuff.jetbrains.plugin.psi.ImportNode;
import io.protostuff.jetbrains.plugin.psi.MessageNode;
import io.protostuff.jetbrains.plugin.psi.ProtoRootNode;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class ProtoCompletionProviderUtil {
    public static LookupElement lookupElementWithSpace(String keyword) {
        return LookupElementBuilder.create(keyword).withInsertHandler(AddSpaceInsertHandler.INSTANCE);
    }

    public static LookupElement lookupElement(String keyword) {
        return LookupElementBuilder.create(keyword);
    }

    public static LookupElement lookupElementWithSlash(String keyword) {
        int startIndex = keyword.indexOf('/');
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

    public static Set<String> getMessageAndEnumNames(ProtoRootNode rootNode) {
        Set<String> result = new TreeSet<>();
        if (null == rootNode) {
            return Collections.emptySet();
        }
        PsiElement[] children = rootNode.getChildren();
        if (0 == children.length) {
            return Collections.emptySet();
        }
        for (PsiElement child : children) {
            if (child instanceof MessageNode) {
                result.add(((MessageNode) child).getName());
            } else if (child instanceof EnumNode) {
                result.add(((EnumNode) child).getName());
            }
        }
        return result;
    }

    public static Set<String> getAvailableImportMessageAndEnumNames(ProtoRootNode protoRootNode) {
        Set<String> result = new TreeSet<>();
        List<ImportNode> imports = protoRootNode.getImports();
        for (ImportNode anImport : imports) {
            ProtoRootNode targetProto = anImport.getTargetProto();
            result.addAll(ProtoCompletionProviderUtil.getMessageAndEnumNames(targetProto));
        }
        return result;
    }
}
