package io.protostuff.jetbrains.plugin.completion;

import com.intellij.codeInsight.completion.AddSpaceInsertHandler;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;
import io.protostuff.compiler.model.ScalarFieldType;
import io.protostuff.jetbrains.plugin.util.ProtoCompletionProviderUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class TypeReferenceCompletionProvider extends CompletionProvider<CompletionParameters> {

    private static final Set<LookupElement> TYPE_REFERENCE =
            Arrays.stream(ScalarFieldType.values())
                    .map(ScalarFieldType::getName)
                    .map(ProtoCompletionProviderUtil::lookupElementWithSpace)
                    .collect(Collectors.toSet());

    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters,
                                  @NotNull ProcessingContext processingContext,
                                  @NotNull CompletionResultSet completionResultSet) {
        completionResultSet.addAllElements(TYPE_REFERENCE);
    }
}
