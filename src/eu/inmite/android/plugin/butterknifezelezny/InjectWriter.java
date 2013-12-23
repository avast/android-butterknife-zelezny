package eu.inmite.android.plugin.butterknifezelezny;

import com.intellij.codeInsight.actions.ReformatAndOptimizeImportsProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.EverythingGlobalScope;
import eu.inmite.android.plugin.butterknifezelezny.common.Defintions;
import eu.inmite.android.plugin.butterknifezelezny.model.Element;

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
	//
	private static final String sViewHolderName = "ButterknifeViewHolder";

	public InjectWriter(PsiFile file, PsiClass clazz, String command, ArrayList<Element> elements, String layoutFileName, String fieldNamePrefix, boolean createHolder) {
		super(clazz.getProject(), command);

		mFile = file;
		mProject = clazz.getProject();
		mClass = clazz;
		mElements = elements;
		mFactory = JavaPsiFacade.getElementFactory(mProject);
		mLayoutFileName = layoutFileName;
		mFieldNamePrefix = fieldNamePrefix;
		mCreateHolder = createHolder;
	}

	@Override
	public void run() throws Throwable {
		PsiClass injectViewClass = JavaPsiFacade.getInstance(mProject).findClass("butterknife.InjectView", new EverythingGlobalScope(mProject));
		if (injectViewClass == null) {
			return; // Butterknife library is not available for project
		}

		if (mCreateHolder) {
			generateAdapter();
		} else {
			generateFields();
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
		// view holder class
		StringBuilder holderBuilder = new StringBuilder();
		holderBuilder.append(sViewHolderName);
		holderBuilder.append("(android.view.View view) {");
		holderBuilder.append("butterknife.Views.inject(this, view);");
		holderBuilder.append("}");

		PsiClass viewHolder = mFactory.createClassFromText(holderBuilder.toString(), mClass);
		viewHolder.setName(sViewHolderName);

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
			injection.append("@butterknife.InjectView("); // annotation
			injection.append(rPrefix);
			injection.append(element.id);
			injection.append(") ");
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

		mClass.addBefore(mFactory.createCommentFromText(comment.toString(), mClass), mClass.findInnerClassByName(sViewHolderName, true));
		mClass.addBefore(mFactory.createKeyword("static", mClass), mClass.findInnerClassByName(sViewHolderName, true));
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
			injection.append("@butterknife.InjectView("); // annotation
			injection.append(element.getFullID());
			injection.append(") ");
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
}
