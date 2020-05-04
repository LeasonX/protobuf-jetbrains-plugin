package io.protostuff.jetbrains.plugin;

import com.intellij.codeInsight.completion.*;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.tree.IElementType;
import io.protostuff.compiler.parser.ProtoLexer;
import io.protostuff.compiler.parser.ProtoParser;
import io.protostuff.jetbrains.plugin.completion.*;
import io.protostuff.jetbrains.plugin.psi.ProtoRootNode;

public class ProtoCodeCompletionContributor extends CompletionContributor {

    public ProtoCodeCompletionContributor() {
        handleAtSyntaxName();
        handleInProtoRootNode();
        handleAtFileReference();
        handleInCurly();
        handleAfterSemicolon();
        handleFieldName();

        handleAfterFieldModifier(ProtoLexer.OPTIONAL);
        handleAfterFieldModifier(ProtoLexer.REQUIRED);
        handleAfterFieldModifier(ProtoLexer.REPEATED);
    }

    void handleAtFileReference() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(ProtoParserDefinition.token(ProtoLexer.STRING_VALUE))
                        .withParent(PlatformPatterns.psiElement(ProtoParserDefinition.rule(ProtoParser.RULE_fileReference)))
                        .withLanguage(ProtoLanguage.INSTANCE),
                new FileReferenceCompletionProvider()
        );
    }

    void handleInProtoRootNode() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement()
                        .withParent(PsiErrorElement.class)
                        .withSuperParent(2, ProtoRootNode.class)
                        .withLanguage(ProtoLanguage.INSTANCE),
                new TopLevelStartKeyWordsCompletionProvider()
        );
    }

    void handleAtSyntaxName() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(ProtoParserDefinition.token(ProtoLexer.STRING_VALUE))
                        .withParent(PlatformPatterns.psiElement(ProtoParserDefinition.rule(ProtoParser.RULE_syntaxName)))
                        .withLanguage(ProtoLanguage.INSTANCE),
                new SyntaxNameCompletionProvider()
        );
    }

    private void handleAfterLeaf(IElementType elementType, boolean isFirstLine) {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement()
                        .afterLeaf(PlatformPatterns.psiElement(elementType))
                        .andNot(PlatformPatterns.psiElement().withSuperParent(2, ProtoRootNode.class))
                        .withLanguage(ProtoLanguage.INSTANCE),
                new FieldModifierCompletionProvider(isFirstLine)
        );
    }

    private void handleFieldName() {
        //only support enum type or message type
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement()
                        .withLanguage(ProtoLanguage.INSTANCE),
                new FieldNameCompletionProvider()
        );
    }

    void handleAfterSemicolon() {
        handleAfterLeaf(ProtoParserDefinition.token(ProtoLexer.SEMICOLON), false);
    }

    void handleInCurly() {
        handleAfterLeaf(ProtoParserDefinition.token(ProtoLexer.LCURLY), true);
    }


    void handleAfterFieldModifier(int token) {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement()
                        .afterLeaf(PlatformPatterns.psiElement(ProtoParserDefinition.token(token)))
                        .withLanguage(ProtoLanguage.INSTANCE),
                new TypeReferenceCompletionProvider()
        );
    }

}
