package io.protostuff.jetbrains.plugin.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.util.ProcessingContext;
import io.protostuff.jetbrains.plugin.cache.ProtoInfoCache;
import io.protostuff.jetbrains.plugin.util.ProtoCompletionProviderUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public class FileReferenceCompletionProvider extends CompletionProvider<CompletionParameters> {


    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters,
                                  @NotNull ProcessingContext processingContext,
                                  @NotNull CompletionResultSet completionResultSet) {
        //ensure get newest setting always
        Set<String> filesRelativePathsOfFolder = ProtoInfoCache.getProjectCacheFileRelativePaths(completionParameters.getPosition().getProject());
        completionResultSet.addAllElements(filesRelativePathsOfFolder.stream()
                .map(ProtoCompletionProviderUtil::lookupElementWithSlash)
                .collect(Collectors.toSet()));
    }
}
