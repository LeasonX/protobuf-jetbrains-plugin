package io.protostuff.jetbrains.plugin.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;
import io.protostuff.jetbrains.plugin.psi.Syntax;
import io.protostuff.jetbrains.plugin.util.ProtoCompletionProviderUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SyntaxNameCompletionProvider extends CompletionProvider<CompletionParameters> {

    private static final List<LookupElement> SYNTAX_NAME =
            Stream.of(Syntax.PROTO2.getName(), Syntax.PROTO3.getName())
                    .map(ProtoCompletionProviderUtil::lookupElement)
                    .collect(Collectors.toList());

    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters,
                                  @NotNull ProcessingContext processingContext,
                                  @NotNull CompletionResultSet completionResultSet) {
        completionResultSet.addAllElements(SYNTAX_NAME);
    }
}
