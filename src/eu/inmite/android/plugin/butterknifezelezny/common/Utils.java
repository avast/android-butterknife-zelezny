package eu.inmite.android.plugin.butterknifezelezny.common;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.awt.RelativePoint;
import eu.inmite.android.plugin.butterknifezelezny.Settings;
import eu.inmite.android.plugin.butterknifezelezny.model.Element;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

	private static final Pattern sInjectionIDPattern = Pattern.compile("^@InjectView\\(([^\\)]+)\\)$", Pattern.CASE_INSENSITIVE);

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
		PsiFile[] files = FilenameIndex.getFilesByName(project, name, new EverythingGlobalScope(project));
		if (files.length <= 0) {
			return null; //no matching files
		}

		return files[0];
	}

	/**
	 * Try to find layout XML file by name
	 *
	 * @param project
	 * @param fileName
	 * @return
	 */
	public static PsiFile findLayoutResource(Project project, String fileName) {
		String name = String.format("%s.xml", fileName);
		PsiFile[] files = FilenameIndex.getFilesByName(project, name, new EverythingGlobalScope(project));
		if (files.length <= 0) {
			return null; //no matching files
		}

		return files[0];
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
                        XmlAttribute id = tag.getAttribute("android:id", null);
                        if(id != null && !isEmptyString(id.getValue())) {
                            Element includeElement = new Element(Defintions.paths.get("View"), "include".concat(getInjectionID(id.getValue())));
                            elements.add(includeElement);
                        }
						if (layout != null) {
							Project project = file.getProject();
							PsiFile include = findLayoutResource(project, getLayoutName(layout.getValue()));
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

					elements.add(new Element(name, value));
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
        String prefix = PropertiesComponent.getInstance().getValue(Settings.PREFIX);
        if (prefix == null || prefix.length() == 0) {
            CodeStyleSettingsManager manager = CodeStyleSettingsManager.getInstance();
            CodeStyleSettings settings = manager.getCurrentSettings();
            prefix = settings.FIELD_NAME_PREFIX;
        }
		if (prefix == null || prefix.length() == 0) {
			prefix = "m"; // field name
		}
		return prefix;
	}

    public static String getViewHolderClassName() {
        return PropertiesComponent.getInstance().getValue(Settings.VIEWHOLDER_CLASS_NAME, "ViewHolder");
    }

	/**
	 * Parse ID of injected element (eg. R.id.text)
	 *
	 * @param annotation
	 * @return
	 */
	public static String getInjectionID(String annotation) {
		String id = null;

		if (isEmptyString(annotation)) {
			return id;
		}

		Matcher matcher = sInjectionIDPattern.matcher(annotation);
		if (matcher.find()) {
			id = matcher.group(1);
		}

		return id;
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
}
