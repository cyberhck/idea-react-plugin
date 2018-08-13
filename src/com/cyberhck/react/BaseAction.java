package com.cyberhck.react;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSStubElementTypes;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptClassImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSReferenceList;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.Nullable;

public abstract class BaseAction extends AnAction {
    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        String fileType = this.getFileType(e);
        if (fileType == null || !fileType.equals("TypeScript")) {
            this.hideAction(e);
            return;
        }
        if (!this.actingFileType(e)) {
            this.hideAction(e);
            return;
        }
    }

    @Nullable
    TypeScriptClass getTypeScriptClassFromCaret(AnActionEvent e) {
        Caret caret = e.getData(PlatformDataKeys.CARET);
        if (caret == null) {
            return null;
        }
        PsiFile file = this.getPsiFile(e);
        if (file == null) {
            return null;
        }
        return PsiTreeUtil.getParentOfType(file.findElementAt(caret.getCaretModel().getOffset()), TypeScriptClass.class);
    }

    @Nullable
    PsiFile getPsiFile(AnActionEvent e) {
        VirtualFile file = this.getVirtualFile(e);
        Project project = e.getProject();
        if (file == null || project == null) {
            return null;
        }
        return PsiUtil.getPsiFile(project, file);
    }

    @Nullable
    String getFileType(AnActionEvent e) {
        VirtualFile file = this.getVirtualFile(e);
        if (file == null) {
            return null;
        }
        return file.getFileType().getName();
    }

    @Nullable
    VirtualFile getVirtualFile(AnActionEvent e) {
        return e.getData(PlatformDataKeys.VIRTUAL_FILE);
    }

    abstract boolean actingFileType(AnActionEvent e);

    boolean isTypeScriptClass(PsiFile file) {
        ASTNode classNode = file.getNode().findChildByType(JSStubElementTypes.TYPESCRIPT_CLASS);
        return classNode != null;
    }

    boolean doesClassExtend(TypeScriptClass typeScriptClass, String className) {
        JSReferenceList extendedClasses = typeScriptClass.getExtendsList();
        if (extendedClasses == null) {
            return false;
        }
        JSClass[] cls = extendedClasses.getReferencedClasses();
        if (cls.length == 0) {
            return false;
        }
        for (JSClass cl : cls) {
            String name = cl.getName();
            if (name == null) {
                continue;
            }
            if (name.equals(className)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    JSFunction getConstructor(AnActionEvent e) {
        TypeScriptClass tsc = this.getTypeScriptClassFromCaret(e);
        if (tsc == null) {
            return null;
        }
        return tsc.getConstructor();
    }

    @Nullable
    JSFunction getMethodByName(AnActionEvent e, String name) {
        TypeScriptClass tsc = this.getTypeScriptClassFromCaret(e);
        if (tsc == null) {
            return null;
        }
        return tsc.findFunctionByName(name);
    }

    TypeScriptClassImpl getTypeScriptClass(PsiFile file) {
        ASTNode classNode = file.getNode().findChildByType(JSStubElementTypes.TYPESCRIPT_CLASS);
        if (classNode == null) {
            return null;
        }
        return new TypeScriptClassImpl(classNode);
    }

    Client getLogClient(String tag) {
        return new Client(tag, "http://localhost:9999/logs");
    }

    private void hideAction(AnActionEvent e) {
        e.getPresentation().setEnabled(false);
    }
}
