package io.protostuff.jetbrains.plugin.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import io.protostuff.compiler.model.FieldModifier;
import io.protostuff.jetbrains.plugin.psi.EnumNode;
import io.protostuff.jetbrains.plugin.psi.MessageNode;
import io.protostuff.jetbrains.plugin.util.ProtoCompletionProviderUtil;
import io.protostuff.jetbrains.plugin.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

public class FieldModifierCompletionProvider extends CompletionProvider<CompletionParameters> {

    private final boolean isMustFirstLine;

    public FieldModifierCompletionProvider(boolean isMustFirstLine) {
        this.isMustFirstLine = isMustFirstLine;
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters,
                                  @NotNull ProcessingContext processingContext,
                                  @NotNull CompletionResultSet completionResultSet) {
        PsiElement parentPsiElement = ProtoCompletionProviderUtil.getSuperParent(4, completionParameters.getPosition());
        Class<? extends PsiElement> parentPsiElementClass = parentPsiElement.getClass();
        if (MessageNode.class == parentPsiElementClass) {
            completionResultSet.addAllElements(Arrays.stream(FieldModifier.values())
                    .map(FieldModifier::toString)
                    .map(ProtoCompletionProviderUtil::lookupElementWithSpace)
                    .collect(Collectors.toSet()));
        } else if (isMustFirstLine && EnumNode.class == parentPsiElementClass) {
            PsiElement[] children = parentPsiElement.getChildren();
            if (children.length >= 4) {
                completionResultSet.addAllElements(StringUtil.getAllUnderLineFormatString(children[1].getText())
                        .stream()
                        .map(item -> item = "unknown_" + item + " = -1;")
                        .map(ProtoCompletionProviderUtil::lookupElement)
                        .collect(Collectors.toSet()));
            }
        }

    }
}
