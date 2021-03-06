package io.protostuff.jetbrains.plugin.settings;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("WeakerAccess")
@State(name = "ProtobufSettings", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public class ProtobufSettings implements PersistentStateComponent<ProtobufSettings> {

    private String protoFolder;
    private String protocPath;
    private String javaFileDir;

    public static ProtobufSettings getInstance(Project project) {
        return ServiceManager.getService(project, ProtobufSettings.class);
    }

    /**
     * Get a list of {@link VirtualFile} for include paths.
     */
    @NotNull
    public List<VirtualFile> getIncludePathsVf() {
        List<VirtualFile> result = new ArrayList<>();
        if(null != protoFolder){
            VirtualFile path = LocalFileSystem.getInstance().findFileByPath(protoFolder);
            if (path != null && path.isDirectory()) {
                result.add(path);
            }
        }
        return result;
    }

    @Override
    public ProtobufSettings getState() {
        return this;
    }

    @Override
    public void loadState(ProtobufSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void copyFrom(ProtobufSettings settings) {
        setProtoFolder(settings.getProtoFolder());
        setProtocPath(settings.getProtocPath());
        setJavaFileDir(settings.getJavaFileDir());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtobufSettings that = (ProtobufSettings) o;
        return Objects.equals(protoFolder, that.protoFolder) &&
                Objects.equals(protocPath, that.protocPath) &&
                Objects.equals(javaFileDir, that.javaFileDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protoFolder, protocPath, javaFileDir);
    }

    public String getProtocPath() {
        return protocPath;
    }

    public void setProtocPath(String protocPath) {
        this.protocPath = protocPath;
    }

    public String getJavaFileDir() {
        return javaFileDir;
    }

    public void setJavaFileDir(String javaFileDir) {
        this.javaFileDir = javaFileDir;
    }

    public void setProtoFolder(String protoFolder) {
        this.protoFolder = protoFolder;
    }

    public String getProtoFolder() {
        return protoFolder;
    }
}
