package com.avast.android.butterknifezelezny;

import com.avast.android.butterknifezelezny.butterknife.ButterKnifeFactory;
import com.avast.android.butterknifezelezny.butterknife.IButterKnife;
import com.avast.android.butterknifezelezny.common.Definitions;
import com.avast.android.butterknifezelezny.common.Utils;
import com.avast.android.butterknifezelezny.model.Element;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.EverythingGlobalScope;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class InjectWriter extends WriteCommandAction.Simple {

    protected PsiFile mFile;
    protected Project mProject;
    protected PsiClass mClass;
    protected ArrayList<Element> mElements;
    protected PsiElementFactory mFactory;
    protected String mLayoutFileName;
    protected String mFieldNamePrefix;
    protected boolean mCreateHolder;
    protected boolean mSplitOnclickMethods;

    public InjectWriter(PsiFile file, PsiClass clazz, String command, ArrayList<Element> elements, String layoutFileName, String fieldNamePrefix, boolean createHolder, boolean splitOnclickMethods) {
        super(clazz.getProject(), command);

        mFile = file;
        mProject = clazz.getProject();
        mClass = clazz;
        mElements = elements;
        mFactory = JavaPsiFacade.getElementFactory(mProject);
        mLayoutFileName = layoutFileName;
        mFieldNamePrefix = fieldNamePrefix;
        mCreateHolder = createHolder;
        mSplitOnclickMethods = splitOnclickMethods;
    }

    @Override
    public void run() throws Throwable {
        final IButterKnife butterKnife = ButterKnifeFactory.findButterKnifeForPsiElement(mProject, mFile);
        if (butterKnife == null) {
            return; // Butterknife library is not available for project
        }

        if (mCreateHolder) {
            generateAdapter(butterKnife);
        } else {
            if (Utils.getInjectCount(mElements) > 0) {
                generateFields(butterKnife);
            }
            generateInjects(butterKnife);
            if (Utils.getClickCount(mElements) > 0) {
                generateClick();
            }
            Utils.showInfoNotification(mProject, String.valueOf(Utils.getInjectCount(mElements)) + " injections and " + String.valueOf(Utils.getClickCount(mElements)) + " onClick added to " + mFile.getName());
        }

        // reformat class
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mProject);
        styleManager.optimizeImports(mFile);
        styleManager.shortenClassReferences(mClass);
        new ReformatCodeProcessor(mProject, mClass.getContainingFile(), null, false).runWithoutProgress();
    }

    protected void generateClick() {
        if (Utils.getClickCount(mElements) == 1) {
            generateSingleClickMethod();
        } else {
            if (mSplitOnclickMethods) {
                generateMultipleClickMethods();
            } else {
                generateSingleClickMethodForSeveralIds();
            }
        }
    }

    private void generateSingleClickMethod() {
        StringBuilder method = new StringBuilder();
        method.append("@butterknife.OnClick(");
        for (Element element : mElements) {
            if (element.isClick) {
                method.append(element.getFullID() + ")");
            }
        }
        method.append("public void onViewClicked() {}");
        mClass.add(mFactory.createMethodFromText(method.toString(), mClass));
    }

    private void generateMultipleClickMethods() {
        for (Element element : mElements) {
            if (element.isClick) {
                StringBuilder method = new StringBuilder();
                method.append("@butterknife.OnClick(" + element.getFullID() + ")");
                method.append("public void on" + Utils.capitalize(element.fieldName) + "Clicked() {}");
                mClass.add(mFactory.createMethodFromText(method.toString(), mClass));
            }
        }
    }

    private void generateSingleClickMethodForSeveralIds() {
        StringBuilder method = new StringBuilder();
        method.append("@butterknife.OnClick({");
        int currentCount = 0;
        for (Element element : mElements) {
            if (element.isClick) {
                currentCount++;
                if (currentCount == Utils.getClickCount(mElements)) {
                    method.append(element.getFullID() + "})");
                } else {
                    method.append(element.getFullID() + ",");
                }
            }
        }
        method.append("public void onViewClicked(android.view.View view) {switch (view.getId()){");
        for (Element element : mElements) {
            if (element.isClick) {
                method.append("case " + element.getFullID() + ": break;");
            }
        }
        method.append("}}");
        mClass.add(mFactory.createMethodFromText(method.toString(), mClass));
    }

    /**
     * Create ViewHolder for adapters with injections
     */
    protected void generateAdapter(@NotNull IButterKnife butterKnife) {
        // view holder class
        StringBuilder holderBuilder = new StringBuilder();
        holderBuilder.append(Utils.getViewHolderClassName());
        holderBuilder.append("(android.view.View view) {");
        holderBuilder.append(butterKnife.getCanonicalBindStatement());
        holderBuilder.append("(this, view);");
        holderBuilder.append("}");

        PsiClass viewHolder = mFactory.createClassFromText(holderBuilder.toString(), mClass);
        viewHolder.setName(Utils.getViewHolderClassName());

        // add injections into view holder
        for (Element element : mElements) {
            if (!element.used) {
                continue;
            }

            String rPrefix;
            if (element.isAndroidNS) {
                rPrefix = "android.R.id.";
            } else {
                rPrefix = "R.id.";
            }

            StringBuilder injection = new StringBuilder();
            injection.append('@');
            injection.append(butterKnife.getFieldAnnotationCanonicalName());
            injection.append('(');
            injection.append(rPrefix);
            injection.append(element.id);
            injection.append(") ");
            if (element.nameFull != null && element.nameFull.length() > 0) { // custom package+class
                injection.append(element.nameFull);
            } else if (Definitions.paths.containsKey(element.name)) { // listed class
                injection.append(Definitions.paths.get(element.name));
            } else { // android.widget
                injection.append("android.widget.");
                injection.append(element.name);
            }
            injection.append(" ");
            injection.append(element.fieldName);
            injection.append(";");

            viewHolder.add(mFactory.createFieldFromText(injection.toString(), mClass));
        }

        mClass.add(viewHolder);

        // add view holder's comment
        StringBuilder comment = new StringBuilder();
        comment.append("/**\n");
        comment.append(" * This class contains all butterknife-injected Views & Layouts from layout file '");
        comment.append(mLayoutFileName);
        comment.append("'\n");
        comment.append("* for easy to all layout elements.\n");
        comment.append(" *\n");
        comment.append(" * @author\tButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)\n");
        comment.append("*/");

//        mClass.addBefore(mFactory.createCommentFromText(comment.toString(), mClass), mClass.findInnerClassByName(Utils.getViewHolderClassName(), true));
        mClass.addBefore(mFactory.createKeyword("static", mClass), mClass.findInnerClassByName(Utils.getViewHolderClassName(), true));
    }

    /**
     * Create fields for injections inside main class
     */
    protected void generateFields(@NotNull IButterKnife butterKnife) {
        // add injections into main class
        for (Element element : mElements) {
            if (!element.used) {
                continue;
            }

            StringBuilder injection = new StringBuilder();
            injection.append('@');
            injection.append(butterKnife.getFieldAnnotationCanonicalName());
            injection.append('(');
            injection.append(element.getFullID());
            injection.append(") ");
            if (element.nameFull != null && element.nameFull.length() > 0) { // custom package+class
                injection.append(element.nameFull);
            } else if (Definitions.paths.containsKey(element.name)) { // listed class
                injection.append(Definitions.paths.get(element.name));
            } else { // android.widget
                injection.append("android.widget.");
                injection.append(element.name);
            }
            injection.append(" ");
            injection.append(element.fieldName);
            injection.append(";");

            mClass.add(mFactory.createFieldFromText(injection.toString(), mClass));
        }
    }

    private boolean containsButterKnifeInjectLine(PsiMethod method, String line) {
        final PsiCodeBlock body = method.getBody();
        if (body == null) {
            return false;
        }
        PsiStatement[] statements = body.getStatements();
        for (PsiStatement psiStatement : statements) {
            String statementAsString = psiStatement.getText();
            if (psiStatement instanceof PsiExpressionStatement && (statementAsString.contains(line))) {
                return true;
            }
        }
        return false;
    }

    protected void generateInjects(@NotNull IButterKnife butterKnife) {
        PsiClass activityClass = JavaPsiFacade.getInstance(mProject).findClass(
                "android.app.Activity", new EverythingGlobalScope(mProject));
        PsiClass fragmentClass = JavaPsiFacade.getInstance(mProject).findClass(
                "android.app.Fragment", new EverythingGlobalScope(mProject));
        PsiClass supportFragmentClass = JavaPsiFacade.getInstance(mProject).findClass(
                "android.support.v4.app.Fragment", new EverythingGlobalScope(mProject));

        // Check for Activity class
        if (activityClass != null && mClass.isInheritor(activityClass, true)) {
            generateActivityBind(butterKnife);
        // Check for Fragment class
        } else if ((fragmentClass != null && mClass.isInheritor(fragmentClass, true)) || (supportFragmentClass != null && mClass.isInheritor(supportFragmentClass, true))) {
            generateFragmentBindAndUnbind(butterKnife);
        }
    }

    private void generateActivityBind(@NotNull IButterKnife butterKnife) {
        if (mClass.findMethodsByName("onCreate", false).length == 0) {
            // Add an empty stub of onCreate()
            StringBuilder method = new StringBuilder();
            method.append("@Override protected void onCreate(android.os.Bundle savedInstanceState) {\n");
            method.append("super.onCreate(savedInstanceState);\n");
            method.append("\t// TODO: add setContentView(...) invocation\n");
            method.append(butterKnife.getCanonicalBindStatement());
            method.append("(this);\n");
            method.append("}");

            mClass.add(mFactory.createMethodFromText(method.toString(), mClass));
        } else {
            PsiMethod onCreate = mClass.findMethodsByName("onCreate", false)[0];
            if (!containsButterKnifeInjectLine(onCreate, butterKnife.getSimpleBindStatement())) {
                for (PsiStatement statement : onCreate.getBody().getStatements()) {
                    // Search for setContentView()
                    if (statement.getFirstChild() instanceof PsiMethodCallExpression) {
                        PsiReferenceExpression methodExpression
                            = ((PsiMethodCallExpression) statement.getFirstChild())
                            .getMethodExpression();
                        // Insert ButterKnife.inject()/ButterKnife.bind() after setContentView()
                        if (methodExpression.getText().equals("setContentView")) {
                            onCreate.getBody().addAfter(mFactory.createStatementFromText(
                                butterKnife.getCanonicalBindStatement() + "(this);", mClass), statement);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void generateFragmentBindAndUnbind(@NotNull IButterKnife butterKnife) {
        boolean generateUnbinder = false;
        String unbinderName = null;
        if (butterKnife.isUsingUnbinder()) {
            unbinderName = getNameForUnbinder(butterKnife);
        }

        // onCreateView() doesn't exist, let's create it
        if (mClass.findMethodsByName("onCreateView", false).length == 0) {
            // Add an empty stub of onCreateView()
            StringBuilder method = new StringBuilder();
            method.append("@Override public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, android.os.Bundle savedInstanceState) {\n");
            method.append("\t// TODO: inflate a fragment view\n");
            method.append("android.view.View rootView = super.onCreateView(inflater, container, savedInstanceState);\n");
            if (butterKnife.isUsingUnbinder()) {
                method.append(unbinderName);
                method.append(" = ");
            }
            method.append(butterKnife.getCanonicalBindStatement());
            method.append("(this, rootView);\n");
            method.append("return rootView;\n");
            method.append("}");

            mClass.add(mFactory.createMethodFromText(method.toString(), mClass));
        } else {
            // onCreateView() exists, let's update it with an inject/bind statement
            PsiMethod onCreateView = mClass.findMethodsByName("onCreateView", false)[0];
            if (!containsButterKnifeInjectLine(onCreateView, butterKnife.getSimpleBindStatement())) {
                for (PsiStatement statement : onCreateView.getBody().getStatements()) {
                    if (statement instanceof PsiReturnStatement) {
                        String returnValue = ((PsiReturnStatement) statement).getReturnValue().getText();
                        // there's layout inflatiion
                        if (returnValue.contains("R.layout")) {
                            onCreateView.getBody().addBefore(mFactory.createStatementFromText("android.view.View view = " + returnValue + ";", mClass), statement);
                            StringBuilder bindText = new StringBuilder();
                            if (butterKnife.isUsingUnbinder()) {
                                bindText.append(unbinderName);
                                bindText.append(" = ");
                            }
                            bindText.append(butterKnife.getCanonicalBindStatement());
                            bindText.append("(this, view);");
                            PsiStatement bindStatement = mFactory.createStatementFromText(bindText.toString(), mClass);
                            onCreateView.getBody().addBefore(bindStatement, statement);
                            statement.replace(mFactory.createStatementFromText("return view;", mClass));
                        } else {
                            // Insert ButterKnife.inject()/ButterKnife.bind() before returning a view for a fragment
                            StringBuilder bindText = new StringBuilder();
                            if (butterKnife.isUsingUnbinder()) {
                                bindText.append(unbinderName);
                                bindText.append(" = ");
                            }
                            bindText.append(butterKnife.getCanonicalBindStatement());
                            bindText.append("(this, ");
                            bindText.append(returnValue);
                            bindText.append(");");
                            PsiStatement bindStatement = mFactory.createStatementFromText(bindText.toString(), mClass);
                            onCreateView.getBody().addBefore(bindStatement, statement);
                        }
                        break;
                    }
                }
            }
        }

        // Insert ButterKnife.reset(this)/ButterKnife.unbind(this)/unbinder.unbind()
        if (butterKnife.isUnbindSupported()) {
            // Create onDestroyView method if it's missing
            if (mClass.findMethodsByName("onDestroyView", false).length == 0) {
                StringBuilder method = new StringBuilder();
                method.append("@Override public void onDestroyView() {\n");
                method.append("super.onDestroyView();\n");
                method.append(generateUnbindStatement(butterKnife, unbinderName, true));
                method.append("}");

                mClass.add(mFactory.createMethodFromText(method.toString(), mClass));
            } else {
                // there's already onDestroyView(), let's add the unbind statement
                PsiMethod onDestroyView = mClass.findMethodsByName("onDestroyView", false)[0];
                if (!containsButterKnifeInjectLine(onDestroyView, butterKnife.getSimpleUnbindStatement())) {
                    StringBuilder unbindText = generateUnbindStatement(butterKnife, unbinderName, false);
                    final PsiStatement unbindStatement = mFactory.createStatementFromText(unbindText.toString(), mClass);
                    onDestroyView.getBody().addBefore(unbindStatement, onDestroyView.getBody().getLastBodyElement());
                }
            }
        }

        // create unbinder field if necessary
        if (butterKnife.isUsingUnbinder() && (mClass.findFieldByName(unbinderName, false) == null)) {
            String unbinderFieldText = butterKnife.getUnbinderClassCanonicalName() + " " + unbinderName + ";";
            mClass.add(mFactory.createFieldFromText(unbinderFieldText, mClass));
        }
    }

    private static StringBuilder generateUnbindStatement(@NotNull IButterKnife butterKnife, String unbinderName,
                                                         boolean partOfMethod) {
        StringBuilder unbindText = new StringBuilder();
        if (butterKnife.isUsingUnbinder()) {
            unbindText.append(unbinderName);
            unbindText.append(butterKnife.getSimpleUnbindStatement());
            unbindText.append("();");
            if (partOfMethod) {
                unbindText.append('\n');
            }
        } else {
            unbindText.append(butterKnife.getCanonicalUnbindStatement());
            unbindText.append("(this);");
            if (partOfMethod) {
                unbindText.append('\n');
            }
        }
        return unbindText;
    }

    /**
     * Generate unique name for the unbinder.
     *
     * @param butterKnife Version of the ButterKnife.
     * @return Name for the unbinder variable.
     */
    private String getNameForUnbinder(@NotNull IButterKnife butterKnife) {
        // first, look for existing unbinder
        for (PsiField field : mClass.getFields()) {
            if (field.getType().getCanonicalText().equals(butterKnife.getUnbinderClassCanonicalName())) {
                return field.getNameIdentifier().getText();
            }
        }
        // find available name for unbinder field
        String unbinderName = "unbinder";
        int idx = 1;
        while (mClass.findFieldByName(unbinderName, false) != null) {
            unbinderName = "unbinder" + idx++;
        }
        return unbinderName;
    }
}