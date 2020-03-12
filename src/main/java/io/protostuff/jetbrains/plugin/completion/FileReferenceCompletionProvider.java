package io.protostuff.jetbrains.plugin.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.util.ProcessingContext;
import io.protostuff.jetbrains.plugin.settings.ProtobufSettings;
import io.protostuff.jetbrains.plugin.util.FileUtil;
import io.protostuff.jetbrains.plugin.util.ProtoCompletionProviderUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class FileReferenceCompletionProvider extends CompletionProvider<CompletionParameters> {


    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters,
                                  @NotNull ProcessingContext processingContext,
                                  @NotNull CompletionResultSet completionResultSet) {
        //ensure get newest setting always
        ProtobufSettings settings = ProtobufSettings.getInstance(completionParameters.getPosition().getProject());
        List<String> includePaths = settings.getIncludePaths();
        if (!includePaths.isEmpty()) {
            String protoFolderPath = includePaths.get(0);
            List<String> filesRelativePathsOfFolder = FileUtil.getFilesRelativePathsOfFolder(protoFolderPath);
            completionResultSet.addAllElements(filesRelativePathsOfFolder.stream()
                    .map(ProtoCompletionProviderUtil::lookupElementWithSlash)
                    .collect(Collectors.toSet()));
        }

    }
}
