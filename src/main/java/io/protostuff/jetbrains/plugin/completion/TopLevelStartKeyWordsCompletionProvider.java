package io.protostuff.jetbrains.plugin.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.EditorModificationUtil;
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
            Stream.of("message", "enum", "service", "extend", "package", "option")
                    .map(ProtoCompletionProviderUtil::lookupElementWithSpace)
                    .collect(Collectors.toList());

    @Override
    protected void addCompletions(
            @NotNull CompletionParameters completionParameters,
            @NotNull ProcessingContext processingContext,
            @NotNull CompletionResultSet result) {
        result.addAllElements(TOP_LEVEL_START_KEY_WORDS);
        //import
        result.addElement(LookupElementBuilder.create("import").withInsertHandler(new AddSpaceAndDoubleQuoteInsertHandler(false)));
    }

    static class AddSpaceAndDoubleQuoteInsertHandler extends AddSpaceInsertHandler{

        public AddSpaceAndDoubleQuoteInsertHandler(boolean triggerAutoPopup) {
            super(triggerAutoPopup);
        }

        @Override
        public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
            super.handleInsert(context, item);
            EditorModificationUtil.insertStringAtCaret(context.getEditor(), "\"\"");
            CaretModel caretModel = context.getEditor().getCaretModel();
            caretModel.moveToOffset(caretModel.getVisualLineEnd() - 2);
        }
    }
}
