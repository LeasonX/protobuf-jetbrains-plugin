package io.protostuff.jetbrains.plugin.settings;

import com.google.common.collect.Lists;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.ui.CollectionListModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

import io.protostuff.jetbrains.plugin.util.VFSUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Plugin settings form.
 *
 * @author Kostiantyn Shchepanovskyi
 */
@SuppressWarnings("WeakerAccess")
public class SettingsForm {

    private final CollectionListModel<String> includePathModel;
    private final List<String> includePathListList;
    private String protocPath;
    private String javaFileDir;
    private final Project project;
    private JPanel panel;
    private com.intellij.ui.components.JBList includePathList;
    private JButton addButton;
    private JButton removeButton;
    private JLabel includePathsLabel;
    private JTextField protocPathTextField;
    private JTextField javaFileDirTextField;
    private JButton protocButton;
    private JButton javaFileButton;

    /**
     * Create new {@link SettingsForm} instance.
     *
     * @param settings is null if settings dialog runs without a project.
     */
    @SuppressWarnings("unchecked")
    public SettingsForm(@Nullable Project project, @Nullable ProtobufSettings settings) {
        this.project = project;
        List<String> internalIncludePathList = new ArrayList<>();
        if (settings != null) {
            internalIncludePathList.addAll(settings.getIncludePaths());
            protocPath = settings.getProtocPath();
            javaFileDir = settings.getJavaFileDir();
        }
        includePathListList = Collections.unmodifiableList(internalIncludePathList);
        includePathModel = new CollectionListModel<>(internalIncludePathList, true);
        includePathList.setModel(includePathModel);
        protocPathTextField.setText(protocPath);
        javaFileDirTextField.setText(javaFileDir);
        addButton.addActionListener(e -> {
            FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
            FileChooser.chooseFile(descriptor, this.project, null, selectedFolder -> {
                String path = selectedFolder.getPath();
                if (null != project) {
                    VFSUtil.flushProtoPathVFSCache(project,path);
                    VFSUtil.addVFSChangeListener(project, path);
                }
                includePathModel.add(path);
            });
        });
        removeButton.addActionListener(e -> {
            int selectedIndex = includePathList.getSelectedIndex();
            if (selectedIndex != -1) {
                includePathModel.removeRow(selectedIndex);
            }
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
            addButton.setEnabled(false);
            removeButton.setEnabled(false);
            protocButton.setEnabled(false);
            javaFileButton.setEnabled(false);
        }

    }

    /**
     * ProtobufSettings settings = ProtobufSettings.getInstance(project);
     * List<String> includePaths = settings.getIncludePaths();
     * if (!includePaths.isEmpty()) {
     * String protoFolderPath = includePaths.get(0);
     * FileUtil.flushProtoPathVFSCache(protoFolderPath);
     * project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
     *
     * @return
     * @Override public void after(@NotNull List<? extends VFileEvent> events) {
     * FileUtil.flushProtoPathVFSCache(protoFolderPath);
     * }
     * });
     * }
     */

    public JPanel getPanel() {
        return panel;
    }

    /**
     * Returns a copy of settings contained in the form.
     */
    public ProtobufSettings getSettings() {
        ProtobufSettings settings = new ProtobufSettings();
        settings.setIncludePaths(Lists.newArrayList(includePathListList));
        settings.setProtocPath(protocPath);
        settings.setJavaFileDir(javaFileDir);
        return settings;
    }

    public void reset(ProtobufSettings source) {
        includePathModel.removeAll();
        includePathModel.add(source.getIncludePaths());
        protocPath = source.getProtocPath();
        javaFileDir = source.getJavaFileDir();
    }

}
