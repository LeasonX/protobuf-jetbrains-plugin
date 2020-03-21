package io.protostuff.jetbrains.plugin.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import io.protostuff.jetbrains.plugin.ProtoFileType;
import io.protostuff.jetbrains.plugin.settings.ProtobufSettings;
import io.protostuff.jetbrains.plugin.util.ProtocUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class SingleGeneratorAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        // Using the event, create and show a dialog
        Project currentProject = anActionEvent.getProject();
        String dlgTitle = anActionEvent.getPresentation().getDescription();
        VirtualFile virtualFile = anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (null != virtualFile) {
            ProtobufSettings instance = ProtobufSettings.getInstance(currentProject);
            String protoFolder = instance.getProtoFolder();
            String protocPath = instance.getProtocPath();
            String javaFileDir = instance.getJavaFileDir();
            if (null == protocPath) {
                Messages.showErrorDialog(currentProject, "No protoc path, please go to settings.",
                        "ERROR");
                return;
            }
            if (null == javaFileDir) {
                Messages.showErrorDialog(currentProject, "No java file directory, please go to settings.",
                        "ERROR");
                return;
            }
            if (null == protoFolder) {
                Messages.showErrorDialog(currentProject, "No protobuff file directory, please go to settings.",
                        "ERROR");
                return;
            }
            int resultCode = 0;
            StringBuilder errorMessageBuilder = new StringBuilder();
            assert currentProject != null;
            FileDocumentManager.getInstance()
                    .saveDocument(Objects.requireNonNull(
                    PsiDocumentManager.getInstance(currentProject).getDocument(Objects.requireNonNull(
                            PsiManager.getInstance(currentProject).findFile(virtualFile)))));
            try {
                resultCode = ProtocUtil.generate(protocPath, protoFolder, javaFileDir,
                        virtualFile.getCanonicalPath(),
                        errorLine -> errorMessageBuilder
                                .append(errorLine)
                                .append(System.getProperty("line.separator")));
            } catch (InterruptedException | IOException e) {
                Messages.showErrorDialog(currentProject, e.getMessage(),
                        "ERROR");
            }
            if (0 == resultCode) {
                //更新vfs索引
                VirtualFileManager.getInstance().asyncRefresh(() -> Messages.showInfoMessage(currentProject, "OK", dlgTitle));
            } else {
                Messages.showErrorDialog(currentProject, errorMessageBuilder.toString(),
                        "ERROR");
            }
        }

    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        PsiFile data = e.getData(LangDataKeys.PSI_FILE);
        //show action only proto file
        e.getPresentation().setEnabledAndVisible(project != null && data != null &&
                ProtoFileType.INSTANCE == data.getFileType());
    }
}
