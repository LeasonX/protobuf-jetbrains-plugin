package io.protostuff.jetbrains.plugin.settings;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;

import javax.swing.*;

import io.protostuff.jetbrains.plugin.util.VFSUtil;
import org.jetbrains.annotations.Nullable;

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
                    VFSUtil.addVFSChangeListener(project, protoFolderPath);
                }
                protoFolderTextField.setText(protoFolderPath);
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
