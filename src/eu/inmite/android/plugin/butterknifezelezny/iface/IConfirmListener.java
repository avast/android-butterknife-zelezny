package eu.inmite.android.plugin.butterknifezelezny.iface;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import eu.inmite.android.plugin.butterknifezelezny.InjectWriter;
import eu.inmite.android.plugin.butterknifezelezny.model.Element;

import java.util.ArrayList;

public interface IConfirmListener {

	public void onConfirm(Project project, Editor editor, ArrayList<Element> elements, String fieldNamePrefix, boolean createHolder, InjectWriter.InjectType injectType);
}
