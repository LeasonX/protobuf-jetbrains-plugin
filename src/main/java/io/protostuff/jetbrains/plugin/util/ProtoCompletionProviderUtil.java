package io.protostuff.jetbrains.plugin.util;

import com.intellij.codeInsight.completion.AddSpaceInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import io.protostuff.jetbrains.plugin.Icons;

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
        return LookupElementBuilder.create(keyword)
                .withTypeText(keyword.substring(0, startIndex), Icons.FOLDER, true)
                .withPresentableText(keyword.substring(startIndex + 1))
                .withIcon(Icons.PROTO);
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