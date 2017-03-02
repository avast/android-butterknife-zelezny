package com.avast.android.butterknifezelezny.common;

import com.avast.android.butterknifezelezny.Settings;
import com.avast.android.butterknifezelezny.butterknife.IButterKnife;
import com.avast.android.butterknifezelezny.model.Element;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;

public class Utils {

    private static final Logger log = Logger.getInstance(Utils.class);

    /**
     * Is using Android SDK?
     */
    public static Sdk findAndroidSDK() {
        Sdk[] allJDKs = ProjectJdkTable.getInstance().getAllJdks();
        for (Sdk sdk : allJDKs) {
            if (sdk.getSdkType().getName().toLowerCase().contains("android")) {
                return sdk;
            }
        }

        return null; // no Android SDK found
    }

    /**
     * Try to find layout XML file in current source on cursor's position
     *
     * @param editor
     * @param file
     * @return
     */
    public static PsiFile getLayoutFileFromCaret(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel().getOffset();

        PsiElement candidateA = file.findElementAt(offset);
        PsiElement candidateB = file.findElementAt(offset - 1);

        PsiFile layout = findLayoutResource(candidateA);
        if (layout != null) {
            return layout;
        }

        return findLayoutResource(candidateB);
    }

    /**
     * Try to find layout XML file in selected element
     *
     * @param element
     * @return
     */
    public static PsiFile findLayoutResource(PsiElement element) {
        log.info("Finding layout resource for element: " + element.getText());
        if (element == null) {
            return null; // nothing to be used
        }
        if (!(element instanceof PsiIdentifier)) {
            return null; // nothing to be used
        }

        PsiElement layout = element.getParent().getFirstChild();
        if (layout == null) {
            return null; // no file to process
        }
        if (!"R.layout".equals(layout.getText())) {
            return null; // not layout file
        }

        Project project = element.getProject();
        String name = String.format("%s.xml", element.getText());
        return resolveLayoutResourceFile(element, project, name);
    }

    private static PsiFile resolveLayoutResourceFile(PsiElement element, Project project, String name) {
        // restricting the search to the current module - searching the whole project could return wrong layouts
        Module module = ModuleUtil.findModuleForPsiElement(element);
        PsiFile[] files = null;
        if (module != null) {
            // first omit libraries, it might cause issues like (#103)
            GlobalSearchScope moduleScope = module.getModuleWithDependenciesScope();
            files = FilenameIndex.getFilesByName(project, name, moduleScope);
            if (files == null || files.length <= 0) {
                // now let's do a fallback including the libraries
                moduleScope = module.getModuleWithDependenciesAndLibrariesScope(false);
                files = FilenameIndex.getFilesByName(project, name, moduleScope);
            }
        }
        if (files == null || files.length <= 0) {
            // fallback to search through the whole project
            // useful when the project is not properly configured - when the resource directory is not configured
            files = FilenameIndex.getFilesByName(project, name, new EverythingGlobalScope(project));
            if (files.length <= 0) {
                return null; //no matching files
            }
        }

        // TODO - we have a problem here - we still can have multiple layouts (some coming from a dependency)
        // we need to resolve R class properly and find the proper layout for the R class
        for (PsiFile file : files) {
            log.info("Resolved layout resource file for name [" + name + "]: " + file.getVirtualFile());
        }
        return files[0];
    }

    /**
     * Try to find layout XML file by name
     *
     * @param file
     * @param project
     * @param fileName
     * @return
     */
    public static PsiFile findLayoutResource(PsiFile file, Project project, String fileName) {
        String name = String.format("%s.xml", fileName);
        // restricting the search to the module of layout that includes the layout we are seaching for
        return resolveLayoutResourceFile(file, project, name);
    }

    /**
     * Obtain all IDs from layout
     *
     * @param file
     * @return
     */
    public static ArrayList<Element> getIDsFromLayout(final PsiFile file) {
        final ArrayList<Element> elements = new ArrayList<Element>();

        return getIDsFromLayout(file, elements);
    }

    /**
     * Obtain all IDs from layout
     *
     * @param file
     * @return
     */
    public static ArrayList<Element> getIDsFromLayout(final PsiFile file, final ArrayList<Element> elements) {
        file.accept(new XmlRecursiveElementVisitor() {

            @Override
            public void visitElement(final PsiElement element) {
                super.visitElement(element);

                if (element instanceof XmlTag) {
                    XmlTag tag = (XmlTag) element;

                    if (tag.getName().equalsIgnoreCase("include")) {
                        XmlAttribute layout = tag.getAttribute("layout", null);

                        if (layout != null) {
                            Project project = file.getProject();
                            PsiFile include = findLayoutResource(file, project, getLayoutName(layout.getValue()));

                            if (include != null) {
                                getIDsFromLayout(include, elements);

                                return;
                            }
                        }
                    }

                    // get element ID
                    XmlAttribute id = tag.getAttribute("android:id", null);
                    if (id == null) {
                        return; // missing android:id attribute
                    }
                    String value = id.getValue();
                    if (value == null) {
                        return; // empty value
                    }

                    // check if there is defined custom class
                    String name = tag.getName();
                    XmlAttribute clazz = tag.getAttribute("class", null);
                    if (clazz != null) {
                        name = clazz.getValue();
                    }

                    try {
                        elements.add(new Element(name, value));
                    } catch (IllegalArgumentException e) {
                        // TODO log
                    }
                }
            }
        });

        return elements;
    }

    /**
     * Get layout name from XML identifier (@layout/....)
     *
     * @param layout
     * @return
     */
    public static String getLayoutName(String layout) {
        if (layout == null || !layout.startsWith("@") || !layout.contains("/")) {
            return null; // it's not layout identifier
        }

        String[] parts = layout.split("/");
        if (parts.length != 2) {
            return null; // not enough parts
        }

        return parts[1];
    }

    /**
     * Display simple notification - information
     *
     * @param project
     * @param text
     */
    public static void showInfoNotification(Project project, String text) {
        showNotification(project, MessageType.INFO, text);
    }

    /**
     * Display simple notification - error
     *
     * @param project
     * @param text
     */
    public static void showErrorNotification(Project project, String text) {
        showNotification(project, MessageType.ERROR, text);
    }

    /**
     * Display simple notification of given type
     *
     * @param project
     * @param type
     * @param text
     */
    public static void showNotification(Project project, MessageType type, String text) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);

        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(text, type, null)
                .setFadeoutTime(7500)
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
    }

    /**
     * Load field name prefix from code style
     *
     * @return
     */
    public static String getPrefix() {
        if (PropertiesComponent.getInstance().isValueSet(Settings.PREFIX)) {
            return PropertiesComponent.getInstance().getValue(Settings.PREFIX);
        } else {
            CodeStyleSettingsManager manager = CodeStyleSettingsManager.getInstance();
            CodeStyleSettings settings = manager.getCurrentSettings();
            return settings.FIELD_NAME_PREFIX;
        }
    }

    public static String getViewHolderClassName() {
        return PropertiesComponent.getInstance().getValue(Settings.VIEWHOLDER_CLASS_NAME, "ViewHolder");
    }

    /**
     * Parse ID of injected element (eg. R.id.text)
     *
     * @param butterKnife ButterKnife version
     * @param annotation
     * @return
     */
    public static String getInjectionID(@NotNull IButterKnife butterKnife, String annotation) {
        String id = null;

        if (isEmptyString(annotation)) {
            return id;
        }

        Matcher matcher = butterKnife.getFieldAnnotationPattern().matcher(annotation);
        if (matcher.find()) {
            id = matcher.group(1);
        }

        return id;
    }


    public static int getInjectCount(ArrayList<Element> elements) {
        int cnt = 0;
        for (Element element : elements) {
            if (element.used) {
                cnt++;
            }
        }
        return cnt;
    }

    public static int getClickCount(ArrayList<Element> elements) {
        int cnt = 0;
        for (Element element : elements) {
            if (element.isClick) {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * Easier way to check if string is empty
     *
     * @param text
     * @return
     */
    public static boolean isEmptyString(String text) {
        return (text == null || text.trim().length() == 0);
    }

    /**
     * Check whether classpath of a module that corresponds to a {@link PsiElement} contains given class.
     *
     * @param project    Project
     * @param psiElement Element for which we check the class
     * @param className  Class name of the searched class
     * @return True if the class is present on the classpath
     * @since 1.3
     */
    public static boolean isClassAvailableForPsiFile(@NotNull Project project, @NotNull PsiElement psiElement, @NotNull String className) {
        Module module = ModuleUtil.findModuleForPsiElement(psiElement);
        if (module == null) {
            return false;
        }
        GlobalSearchScope moduleScope = module.getModuleWithDependenciesAndLibrariesScope(false);
        PsiClass classInModule = JavaPsiFacade.getInstance(project).findClass(className, moduleScope);
        return classInModule != null;
    }

    /**
     * Check whether classpath of a the whole project contains given class.
     * This is only fallback for wrongly setup projects.
     *
     * @param project   Project
     * @param className Class name of the searched class
     * @return True if the class is present on the classpath
     * @since 1.3.1
     */
    public static boolean isClassAvailableForProject(@NotNull Project project, @NotNull String className) {
        PsiClass classInModule = JavaPsiFacade.getInstance(project).findClass(className,
                new EverythingGlobalScope(project));
        return classInModule != null;
    }

    /**
     * Capitalizes a String changing the first character to upper case. No other characters are changed.

     * @param src the String to capitalize, may be null
     * @return the capitalized String, {@code null} if src is null
     * @since 1.6.0
     */
    public static String capitalize(@Nullable String src) {
        if (src == null) {
            return src;
        }
        return src.substring(0, 1).toUpperCase(Locale.US) + src.substring(1);
    }
}
