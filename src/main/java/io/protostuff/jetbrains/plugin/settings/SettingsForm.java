package io.protostuff.jetbrains.plugin.settings;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;

import javax.swing.*;

import io.protostuff.jetbrains.plugin.util.ProtocUtil;
import io.protostuff.jetbrains.plugin.util.PsiUtil;
import io.protostuff.jetbrains.plugin.util.VFSUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Plugin settings form.
 *
 * @author Kostiantyn Shchepanovskyi
 */
public class SettingsForm {

    private String protoFolderPath;
    private String protocPath;
    private String javaFileDir;
    private final Project project;
    private JPanel panel;
    private JTextField protocPathTextField;
    private JTextField javaFileDirTextField;
    private JTextField protoFolderTextField;
    private JButton protoFolderButton;
    private JButton protocButton;
    private JButton javaFileButton;
    private JLabel protoFolderLabel;

    /**
     * Create new {@link SettingsForm} instance.
     *
     * @param settings is null if settings dialog runs without a project.
     */
    public SettingsForm(@Nullable Project project, @Nullable ProtobufSettings settings) {
        this.project = project;
        if (settings != null) {
            protoFolderPath = settings.getProtoFolder();
            protocPath = settings.getProtocPath();
            javaFileDir = settings.getJavaFileDir();
        }
        protoFolderTextField.setText(protoFolderPath);
        protocPathTextField.setText(protocPath);
        javaFileDirTextField.setText(javaFileDir);
        protoFolderButton.addActionListener(e -> {
            FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
            FileChooser.chooseFile(descriptor, this.project, null, selectedFolder -> {
                protoFolderPath = selectedFolder.getPath();
                if (null != project) {
                    VFSUtil.flushProtoPathVFSCache(project, protoFolderPath);
                    PsiUtil.flushAllImportableMessageOrEnumCache(project, protoFolderPath);
                    VFSUtil.addVFSChangeListener(project, protoFolderPath);
                    PsiUtil.addPsiChangeListener(project, protoFolderPath);
                }
                protoFolderTextField.setText(protoFolderPath);
                //auto complete protoc and java file setting
                if (ProtocUtil.isWindowsOS) {
                    //protoc path auto complete
                    String protocFilePath = protoFolderPath + "/protoc.exe";
                    File protocFile = new File(protocFilePath);
                    if (protocFile.exists()) {
                        protocPath = protocFilePath;
                        protocPathTextField.setText(protocFilePath);
                    }
                }
                //auto complete java file folder
                Path resourceFolder = Paths.get(protoFolderPath).getParent();
                if (null != resourceFolder) {
                    Path mainFolder = resourceFolder.getParent();
                    if (null != mainFolder) {
                        Path javaPath = mainFolder.resolve("java");
                        String javaPathStr;
                        if (new File(javaPathStr = VFSUtil.replaceFileSeparator(javaPath.toString())).exists()) {
                            javaFileDir = javaPathStr;
                            javaFileDirTextField.setText(javaPathStr);
                        }
                    }
                }
            });
        });
        protocButton.addActionListener(e -> {
            FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
            FileChooser.chooseFile(descriptor, this.project, null, selectedFolder -> {
                protocPath = selectedFolder.getPath();
                protocPathTextField.setText(protocPath);
            });
        });
        javaFileButton.addActionListener(e -> {
            FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
            FileChooser.chooseFile(descriptor, this.project, null, selectedFolder -> {
                javaFileDir = selectedFolder.getPath();
                javaFileDirTextField.setText(javaFileDir);
            });
        });

        if (settings == null) {
            protoFolderButton.setEnabled(false);
            protocButton.setEnabled(false);
            javaFileButton.setEnabled(false);
        }

    }

    public JPanel getPanel() {
        return panel;
    }

    /**
     * Returns a copy of settings contained in the form.
     */
    public ProtobufSettings getSettings() {
        ProtobufSettings settings = new ProtobufSettings();
        settings.setProtoFolder(protoFolderPath);
        settings.setProtocPath(protocPath);
        settings.setJavaFileDir(javaFileDir);
        return settings;
    }

    public void reset(ProtobufSettings source) {
        protoFolderPath = source.getProtoFolder();
        protocPath = source.getProtocPath();
        javaFileDir = source.getJavaFileDir();
    }

}
