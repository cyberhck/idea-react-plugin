package com.cyberhck.react;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public abstract class BaseAction extends AnAction {
    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        Project project = e.getProject();
        if (project == null) {
            this.hideAction(e);
            return;
        }
        VirtualFile projectFile = project.getProjectFile();
        if (projectFile == null) {
            this.hideAction(e);
            return;
        }
        this.getLogClient("FILE_TYPE").info(projectFile.getFileType().getName());
    }
    Client getLogClient(String tag) {
        return new Client(tag, "http://localhost:9999/logs");
    }

    private void hideAction(AnActionEvent e) {
        e.getPresentation().setEnabled(false);
    }
}
