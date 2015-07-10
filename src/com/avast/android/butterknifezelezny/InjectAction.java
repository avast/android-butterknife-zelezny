package com.avast.android.butterknifezelezny;

import com.avast.android.butterknifezelezny.common.Definitions;
import com.avast.android.butterknifezelezny.common.Utils;
import com.avast.android.butterknifezelezny.form.EntryList;
import com.avast.android.butterknifezelezny.iface.ICancelListener;
import com.avast.android.butterknifezelezny.iface.IConfirmListener;
import com.avast.android.butterknifezelezny.model.Element;
import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.util.PsiUtilBase;

import javax.swing.*;
import java.util.ArrayList;

public class InjectAction extends BaseGenerateAction implements IConfirmListener, ICancelListener {

    protected JFrame mDialog;

    @SuppressWarnings("unused")
    public InjectAction() {
        super(null);
    }

    @SuppressWarnings("unused")
    public InjectAction(CodeInsightActionHandler handler) {
        super(handler);
    }

    @Override
    protected boolean isValidForClass(final PsiClass targetClass) {
        PsiClass bindClass = JavaPsiFacade.getInstance(targetClass.getProject()).findClass("butterknife.Bind", new EverythingGlobalScope(targetClass.getProject()));
        PsiClass injectViewClass = JavaPsiFacade.getInstance(targetClass.getProject()).findClass("butterknife.InjectView", new EverythingGlobalScope(targetClass.getProject()));

        return ((bindClass != null || injectViewClass != null) && super.isValidForClass(targetClass) && Utils.findAndroidSDK() != null && !(targetClass instanceof PsiAnonymousClass));
    }

    @Override
    public boolean isValidForFile(Project project, Editor editor, PsiFile file) {
        PsiClass bindClass = JavaPsiFacade.getInstance(project).findClass("butterknife.Bind", new EverythingGlobalScope(project));
        PsiClass injectViewClass = JavaPsiFacade.getInstance(project).findClass("butterknife.InjectView", new EverythingGlobalScope(project));

        return ((bindClass != null || injectViewClass != null) && super.isValidForFile(project, editor, file) && Utils.getLayoutFileFromCaret(editor, file) != null);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        Editor editor = event.getData(PlatformDataKeys.EDITOR);

        actionPerformedImpl(project, editor);
    }

    @Override
    public void actionPerformedImpl(Project project, Editor editor) {
        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        PsiFile layout = Utils.getLayoutFileFromCaret(editor, file);

        if (layout == null) {
            Utils.showErrorNotification(project, "No layout found");
            return; // no layout found
        }

        ArrayList<Element> elements = Utils.getIDsFromLayout(layout);
        if (!elements.isEmpty()) {
            showDialog(project, editor, elements);
        } else {
            Utils.showErrorNotification(project, "No IDs found in layout");
        }
    }

    public void onConfirm(Project project, Editor editor, ArrayList<Element> elements, String fieldNamePrefix, boolean createHolder) {
        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        PsiFile layout = Utils.getLayoutFileFromCaret(editor, file);

        closeDialog();

        // count selected elements
        int cnt = 0;
        for (Element element : elements) {
            if (element.used) {
                cnt++;
            }
        }

        if (cnt > 0) { // generate injections
            new InjectWriter(file, getTargetClass(editor, file), "Generate Injections", elements, layout.getName(), fieldNamePrefix, createHolder).execute();

            if (cnt == 1) {
                Utils.showInfoNotification(project, "One injection added to " + file.getName());
            } else {
                Utils.showInfoNotification(project, String.valueOf(cnt) + " injections added to " + file.getName());
            }
        } else { // just notify user about no element selected
            Utils.showInfoNotification(project, "No injection was selected");
        }
    }

    public void onCancel() {
        closeDialog();
    }

    protected void showDialog(Project project, Editor editor, ArrayList<Element> elements) {
        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        PsiClass clazz = getTargetClass(editor, file);

        // get parent classes and check if it's an adapter
        boolean createHolder = false;
        PsiReferenceList list = getTargetClass(editor, file).getExtendsList();
        for (PsiJavaCodeReferenceElement element : list.getReferenceElements()) {
            if (Definitions.adapters.contains(element.getQualifiedName())) {
                createHolder = true;
            }
        }

        // get already generated injections
        ArrayList<String> ids = new ArrayList<String>();
        PsiField[] fields = clazz.getAllFields();
        String[] annotations;
        String id;

        for (PsiField field : fields) {
            annotations = field.getFirstChild().getText().split(" ");

            for (String annotation : annotations) {
                id = Utils.getInjectionID(annotation.trim());
                if (!Utils.isEmptyString(id)) {
                    ids.add(id);
                }
            }
        }

        EntryList panel = new EntryList(project, editor, elements, ids, createHolder, this, this);

        mDialog = new JFrame();
        mDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mDialog.getRootPane().setDefaultButton(panel.getConfirmButton());
        mDialog.getContentPane().add(panel);
        mDialog.pack();
        mDialog.setLocationRelativeTo(null);
        mDialog.setVisible(true);
    }

    protected void closeDialog() {
        if (mDialog == null) {
            return;
        }

        mDialog.setVisible(false);
        mDialog.dispose();
    }
}
