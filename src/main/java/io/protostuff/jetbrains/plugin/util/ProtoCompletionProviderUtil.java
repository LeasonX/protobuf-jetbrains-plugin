package io.protostuff.jetbrains.plugin.util;

import com.intellij.codeInsight.completion.AddSpaceInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;

public final class ProtoCompletionProviderUtil {
    public static LookupElement lookupElementWithSpace(String keyword) {
        return LookupElementBuilder.create(keyword).withInsertHandler(AddSpaceInsertHandler.INSTANCE);
    }

    public static LookupElement lookupElement(String keyword) {
        return LookupElementBuilder.create(keyword);
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
