package io.protostuff.jetbrains.plugin.bean;

import com.intellij.openapi.vfs.VirtualFile;
import io.protostuff.jetbrains.plugin.enums.ImportableType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ImportableNode {

    private String name;

    private ImportableType importableType;

    //vfs of node
    private VirtualFile virtualFile;

    //relative path with proto folder path
    @Nullable
    private String relativePath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ImportableType getImportableType() {
        return importableType;
    }

    public void setImportableType(ImportableType importableType) {
        this.importableType = importableType;
    }

    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    public void setVirtualFile(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportableNode that = (ImportableNode) o;
        return Objects.equals(name, that.name) &&
                importableType == that.importableType &&
                Objects.equals(virtualFile, that.virtualFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, importableType, virtualFile);
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }
}
