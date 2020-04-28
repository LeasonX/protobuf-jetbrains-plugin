package io.protostuff.jetbrains.plugin.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import io.protostuff.compiler.model.ScalarFieldType;
import io.protostuff.jetbrains.plugin.psi.ImportNode;
import io.protostuff.jetbrains.plugin.psi.MessageNode;
import io.protostuff.jetbrains.plugin.psi.EnumNode;
import io.protostuff.jetbrains.plugin.psi.ProtoPsiFileRoot;
import io.protostuff.jetbrains.plugin.psi.ProtoRootNode;
import io.protostuff.jetbrains.plugin.util.ProtoCompletionProviderUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.TreeSet;
import java.util.Collections;
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

        ProtoRootNode protoRoot = ((ProtoPsiFileRoot) completionParameters.getPosition().getContainingFile()).getProtoRoot();
        //add current file
        Set<String> messageAndEnumNames = new TreeSet<>(ProtoCompletionProviderUtil.getMessageAndEnumNames(protoRoot));
        // add import
        Set<String> availableImportMessageAndEnumNames = ProtoCompletionProviderUtil.getAvailableImportMessageAndEnumNames(protoRoot);
        messageAndEnumNames.addAll(availableImportMessageAndEnumNames);
        completionResultSet.addAllElements(messageAndEnumNames
                .stream()
                .map(ProtoCompletionProviderUtil::lookupElementWithSpace)
                .collect(Collectors.toSet()));
        //add scalar field type
        completionResultSet.addAllElements(TYPE_REFERENCE);
    }


}
