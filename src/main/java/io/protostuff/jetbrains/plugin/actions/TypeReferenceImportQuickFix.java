package io.protostuff.jetbrains.plugin.actions;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.protostuff.jetbrains.plugin.util.ProtoCompletionProviderUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class TypeReferenceImportQuickFix extends BaseIntentionAction {

    private String importableFileRelativePath;

    public TypeReferenceImportQuickFix(String importableFileRelativePath) {
        this.importableFileRelativePath = importableFileRelativePath;
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getText() {
        return "Fix import";
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return "Type reference";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        ProtoCompletionProviderUtil.autoCompleteImportNode(psiFile, editor, importableFileRelativePath);
    }
}
