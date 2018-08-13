package com.cyberhck.react;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SagaListenerMethodAction extends BaseAction {
    final private String baseClassName = "BaseSaga";
    final private String listenerMethodName = "registerListeners";

    @Override
    public void actionPerformed(AnActionEvent e) {
        this.addListener(e);
    }

    private void addListener(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        WriteCommandAction.runWriteCommandAction(project, () -> {
            this.addTakeLatest(e);
            this.addBind(e);
            this.addMethod(e);
        });
    }

    private void addTakeLatest(AnActionEvent e) {
        final Project project = e.getProject();
        TypeScriptFunction listener = this.getListenerMethod(e);
        if (project == null || listener == null) {
            return;
        }
        ASTNode takeLatestNode = this.getTakeLatest(project);
        if (takeLatestNode == null) {
            return;
        }
        listener.addBefore(takeLatestNode.getPsi(), listener.getLastChild().getLastChild());
    }

    private void addBind(AnActionEvent e) {
        final Project project = e.getProject();
        JSFunction constructor = this.getConstructor(e);
        if (project == null || constructor == null) {
            return;
        }
        ASTNode bind = this.getBindParam(project, "listen");
        if (bind == null) {
            return;
        }
        constructor.addBefore(bind.getPsi(), constructor.getLastChild().getLastChild());
    }

    private void addMethod(AnActionEvent e) {
        final Project project = e.getProject();
        JSFunction method = this.getMethodByName(e, "listen");
        TypeScriptClass tsc = this.getTypeScriptClassFromCaret(e);
        if (project == null || tsc == null || method != null) {
            return;
        }
        ASTNode listenerMethod = this.getListenerMethodImplementation(project);
        if (listenerMethod == null) {
            return;
        }
        tsc.addBefore(listenerMethod.getPsi(), this.getListenerMethod(e));
        // create method.
    }

    @Nullable
    private ASTNode getListenerMethodImplementation(@NotNull Project project) {
        return JSChangeUtil.createExpressionFromText(project, "public *listen(): IterableIterator<any> {}");
    }

    @Nullable
    private ASTNode getBindParam(@NotNull Project project, @NotNull String methodName) {
        return JSChangeUtil.createExpressionFromText(project, "this." + methodName + " = " + "this." + methodName + ".bind(this)");
    }
    @Nullable
    private ASTNode getTakeLatest(@NotNull Project project) {
        return JSChangeUtil.createExpressionFromText(project, "takeLatest($KEY$, this.$METHOD$, $END$);");
    }

    @Nullable
    private TypeScriptFunction getListenerMethod(AnActionEvent e) {
        TypeScriptClass tsc = this.getTypeScriptClassFromCaret(e);
        if (tsc == null) {
            return null;
        }
        TypeScriptFunction[] listenerMethods = tsc.findFunctionsByName(listenerMethodName);
        return listenerMethods[0];
    }

    @Override
    boolean actingFileType(AnActionEvent e) {
        TypeScriptClass typeScriptClass = this.getTypeScriptClassFromCaret(e);
        if (typeScriptClass == null) {
            return false;
        }
        return this.doesClassExtend(typeScriptClass, this.baseClassName);
    }
}
