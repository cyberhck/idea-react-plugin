package com.cyberhck.react;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParserFacade;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class SagaListenerMethodAction extends BaseAction {
    final private String baseClassName = "BaseSaga";
    final private String listenerMethodName = "registerListeners";

    @Override
    public void actionPerformed(AnActionEvent e) {
        this.addListener(e);
    }

    private void addListener(AnActionEvent e) {
        // first get the listener method and add listen action.
        // then add the actual method later make it live action, for now hardcode
        TypeScriptFunction listener = this.getListenerMethod(e);
        Project project = e.getProject();
        if (listener == null || project == null) {
            return;
        }
        ASTNode node = this.getTakeLatest(project);
        if (node == null) {
            return;
        }
        // add child. but in other context.
        CommandProcessor.getInstance().executeCommand(project, () -> {
            ApplicationManager.getApplication().runWriteAction(() -> {
                ASTNode block = listener.getNode().findChildByType(JSElementTypes.BLOCK_STATEMENT);
                TypeScriptClass tsc = this.getTypeScriptClassFromCaret(e);
                if (block == null || tsc == null) {
                    return;
                }
                ASTNode rbrace = block.findChildByType(JSTokenTypes.RBRACE);
                if (rbrace == null) {
                    return;
                }
                JSFunction jsFun = (JSFunction) tsc;
                listener.addBefore(node.getPsi(), rbrace.getPsi());
//                listener.addBefore(node.getPsi(), rbrace.getPsi());
            });
        }, null, null);
    }

    PsiElement getWhitespace(@NotNull Project project) {
        return PsiParserFacade.SERVICE.getInstance(project).createWhiteSpaceFromText("\n");
    }
    @Nullable
    private ASTNode getTakeLatest(@NotNull Project project) {
        return JSChangeUtil.createExpressionFromText(project, "takeLatest(SOMETHING, this.listen)");
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
