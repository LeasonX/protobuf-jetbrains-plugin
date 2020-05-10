package io.protostuff.jetbrains.plugin.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.util.ProcessingContext;
import io.protostuff.compiler.model.ScalarFieldType;
import io.protostuff.jetbrains.plugin.bean.ImportableNode;
import io.protostuff.jetbrains.plugin.cache.ProtoInfoCache;
import io.protostuff.jetbrains.plugin.util.ProtoCompletionProviderUtil;
import org.apache.commons.collections.MapUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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

        Map<String, Set<ImportableNode>> importableNodeMap = ProtoInfoCache.getImportableNodeMap(completionParameters.getPosition().getProject());
        if (MapUtils.isNotEmpty(importableNodeMap)) {
            importableNodeMap.forEach((fileName, nodes) -> nodes.forEach((node) -> {
                completionResultSet.addElement(ProtoCompletionProviderUtil.lookupImportableWithSpace(fileName, node));
            }));
        }

        //add scalar field type
        completionResultSet.addAllElements(TYPE_REFERENCE);
    }


}
