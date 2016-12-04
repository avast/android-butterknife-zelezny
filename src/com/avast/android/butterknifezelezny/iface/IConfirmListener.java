package com.avast.android.butterknifezelezny.iface;

import com.avast.android.butterknifezelezny.model.Element;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;

public interface IConfirmListener {

    public void onConfirm(Project project, Editor editor, ArrayList<Element> elements, String fieldNamePrefix, boolean createHolder, boolean splitOnclickMethods);
}
