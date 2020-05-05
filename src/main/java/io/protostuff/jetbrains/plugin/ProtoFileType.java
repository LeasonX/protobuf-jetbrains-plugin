package io.protostuff.jetbrains.plugin;

import com.intellij.openapi.fileTypes.LanguageFileType;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * http://www.jetbrains.org/intellij/sdk/docs/tutorials/custom_language_support/language_and_filetype.html
 */
public class ProtoFileType extends LanguageFileType {

    public static final String FILE_EXTENSION = "proto";

    public static final ProtoFileType INSTANCE = new ProtoFileType();

    protected ProtoFileType() {
        super(ProtoLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "PROTO";
    }

    @NotNull
    @Override
    public String getDescription() {
        return ProtostuffBundle.message("filetype.description.proto");
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return FILE_EXTENSION;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return Icons.PROTO;
    }
}
