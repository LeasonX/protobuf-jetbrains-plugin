package io.protostuff.jetbrains.plugin.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.util.ProcessingContext;
import io.protostuff.jetbrains.plugin.util.ProtoCompletionProviderUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Keywords that are valid to start a top-level entry.
 */
public class TopLevelStartKeyWordsCompletionProvider extends CompletionProvider<CompletionParameters> {
    private static final List<LookupElement> TOP_LEVEL_START_KEY_WORDS =
            Stream.of("message", "enum", "service", "extend", "import", "package", "option")
                    .map(ProtoCompletionProviderUtil::lookupElementWithSpace)
                    .collect(Collectors.toList());

    @Override
    protected void addCompletions(
            @NotNull CompletionParameters completionParameters,
            ProcessingContext processingContext,
            @NotNull CompletionResultSet result) {
        result.addAllElements(TOP_LEVEL_START_KEY_WORDS);
    }
}
