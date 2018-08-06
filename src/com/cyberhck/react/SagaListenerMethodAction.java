package com.cyberhck.react;

import com.intellij.openapi.actionSystem.AnActionEvent;

public class SagaListenerMethodAction extends BaseAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // try to notify too.
        // TODO: insert action logic here
        this.getLogClient("ACTION_PERFORMED").info("action performed");
    }
}
