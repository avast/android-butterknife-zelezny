package eu.inmite.android.plugin.butterknifezelezny;

import com.intellij.codeInsight.actions.ReformatAndOptimizeImportsProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.EverythingGlobalScope;
import eu.inmite.android.plugin.butterknifezelezny.common.Defintions;
import eu.inmite.android.plugin.butterknifezelezny.model.Element;

import java.util.ArrayList;

public class InjectWriter extends WriteCommandAction.Simple {

    public enum InjectType {BUTTERKNIFE, NATIVE_FINDS};

    protected PsiFile mFile;
	protected Project mProject;
	protected PsiClass mClass;
	protected ArrayList<Element> mElements;
	protected PsiElementFactory mFactory;
	protected String mLayoutFileName;
	protected String mFieldNamePrefix;
    protected boolean mCreateHolder;
	protected InjectType mInjectType;
	//
	private static final String sButterKnifeViewHolderName = "ButterknifeViewHolder";
    private static final String sNativeFindsViewHolderName = "ViewHolder";

	public InjectWriter(PsiFile file, PsiClass clazz, String command, ArrayList<Element> elements, String layoutFileName, String fieldNamePrefix, boolean createHolder, InjectType injectType) {
		super(clazz.getProject(), command);

		mFile = file;
		mProject = clazz.getProject();
		mClass = clazz;
		mElements = elements;
		mFactory = JavaPsiFacade.getElementFactory(mProject);
		mLayoutFileName = layoutFileName;
		mFieldNamePrefix = fieldNamePrefix;
        mCreateHolder = createHolder;
		mInjectType = injectType;
	}

	@Override
	public void run() throws Throwable {
		PsiClass injectViewClass = JavaPsiFacade.getInstance(mProject).findClass("butterknife.InjectView", new EverythingGlobalScope(mProject));
		if (injectViewClass == null && mInjectType == InjectType.BUTTERKNIFE) {
			return; // Butterknife library is not available for project
		}

		if (mCreateHolder) {
            generateAdapter();
		} else {
			generateFields();
            if(mInjectType == InjectType.NATIVE_FINDS) {
                generateFinds();
            }
		}

		// reformat class
		JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mProject);
		styleManager.optimizeImports(mFile);
		styleManager.shortenClassReferences(mClass);

		new ReformatAndOptimizeImportsProcessor(mProject, mClass.getContainingFile(), false).runWithoutProgress();
	}

    /**
	 * Create ViewHolder for adapters with injections
	 */
	protected void generateAdapter() {
        String holderClassName;
        if(mInjectType == InjectType.BUTTERKNIFE) {
            holderClassName = sButterKnifeViewHolderName;
        } else {
            holderClassName = sNativeFindsViewHolderName;
        }
		// view holder class
		StringBuilder holderBuilder = new StringBuilder();
        holderBuilder.append(holderClassName);
		holderBuilder.append("(android.view.View view) {");
        if(mInjectType == InjectType.BUTTERKNIFE) {
            holderBuilder.append("butterknife.Views.inject(this, view);");
        } else {
            holderBuilder.append(generateFindViews("view"));
        }
		holderBuilder.append("}");

		PsiClass viewHolder = mFactory.createClassFromText(holderBuilder.toString(), mClass);
		viewHolder.setName(holderClassName);

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
            if(mInjectType == InjectType.BUTTERKNIFE) {
                injection.append("@butterknife.InjectView("); // annotation
                injection.append(rPrefix);
                injection.append(element.id);
                injection.append(") ");
            }
			if (element.nameFull != null && element.nameFull.length() > 0) { // custom package+class
				injection.append(element.nameFull);
			} else if (Defintions.paths.containsKey(element.name)) { // listed class
				injection.append(Defintions.paths.get(element.name));
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
		comment.append(" * @author\tAndroid Butter Zelezny, plugin for IntelliJ IDEA/Android Studio by Inmite (www.inmite.eu)\n");
		comment.append("*/");

		mClass.addBefore(mFactory.createCommentFromText(comment.toString(), mClass), mClass.findInnerClassByName(holderClassName, true));
		mClass.addBefore(mFactory.createKeyword("static", mClass), mClass.findInnerClassByName(holderClassName, true));
	}

	/**
	 * Create fields for injections inside main class
	 */
	protected void generateFields() {
		// add injections into main class
		for (Element element : mElements) {
			if (!element.used) {
				continue;
			}

			StringBuilder injection = new StringBuilder();
            if(mInjectType == InjectType.BUTTERKNIFE) {
                injection.append("@butterknife.InjectView("); // annotation
                injection.append(element.getFullID());
                injection.append(") ");
            } else if(mInjectType == InjectType.NATIVE_FINDS) {
                injection.append("private ");
            }
			if (element.nameFull != null && element.nameFull.length() > 0) { // custom package+class
				injection.append(element.nameFull);
			} else if (Defintions.paths.containsKey(element.name)) { // listed class
				injection.append(Defintions.paths.get(element.name));
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

    /**
     * Create finds inside main class
     */
    protected void generateFinds() {
        StringBuilder injection = new StringBuilder();
        injection.append("private void findViews() {");
        injection.append(generateFindViews(""));
        injection.append("}");

        mClass.add(mFactory.createMethodFromText(injection.toString(), mClass));
    }

    private String generateFindViews(String findPrefix) {
        StringBuilder injection = new StringBuilder();
        // add finds into main class
        for (Element element : mElements) {
            if (!element.used) {
                continue;
            }

            injection.append(element.fieldName);
            injection.append(" = ");
            injection.append("(");
            if (element.nameFull != null && element.nameFull.length() > 0) { // custom package+class
                injection.append(element.nameFull);
            } else if (Defintions.paths.containsKey(element.name)) { // listed class
                injection.append(Defintions.paths.get(element.name));
            } else { // android.widget
                injection.append("android.widget.");
                injection.append(element.name);
            }
            injection.append(") ");
            if(findPrefix != null && !"".equals(findPrefix)) {
                injection.append(findPrefix);
                injection.append(".");
            }
            injection.append("findViewById(");
            injection.append(element.getFullID());
            injection.append(");");
        }
        return injection.toString();
    }
}
