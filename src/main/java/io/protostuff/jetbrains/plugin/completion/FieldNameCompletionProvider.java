package io.protostuff.jetbrains.plugin.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import io.protostuff.jetbrains.plugin.psi.GenericNameNode;
import io.protostuff.jetbrains.plugin.psi.TypeReferenceNode;
import io.protostuff.jetbrains.plugin.util.ProtoCompletionProviderUtil;
import io.protostuff.jetbrains.plugin.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class FieldNameCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters,
                                  @NotNull ProcessingContext processingContext,
                                  @NotNull CompletionResultSet completionResultSet) {
        PsiElement grandParentElement = ProtoCompletionProviderUtil.getSuperParent(2, completionParameters.getPosition());
        if (grandParentElement.getClass() == GenericNameNode.class) {
            for (PsiElement child : grandParentElement.getParent().getChildren()) {
                if(child.getClass() == TypeReferenceNode.class){
                    String typeReferenceText = child.getText();
                    completionResultSet.addAllElements(StringUtil.getAllUnderLineFormatString(typeReferenceText)
                            .stream()
                            .map(ProtoCompletionProviderUtil::lookupElement)
                            .collect(Collectors.toSet()));
                    break;
                }
            }
        }
    }
}
