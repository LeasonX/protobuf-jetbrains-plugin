package io.protostuff.jetbrains.plugin;

import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.jetbrains.org/intellij/sdk/docs/tutorials/custom_language_support/language_and_filetype.html
 */
public class ProtoLanguage extends Language {
    public static final ProtoLanguage INSTANCE = new ProtoLanguage();

    private ProtoLanguage() {
        super("PROTO");
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Protobuf";
    }

    @Override
    public boolean isCaseSensitive() {
        return true;
    }

}
