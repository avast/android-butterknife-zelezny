package com.avast.android.butterknifezelezny.form;

import com.avast.android.butterknifezelezny.iface.ICancelListener;
import com.avast.android.butterknifezelezny.iface.IConfirmListener;
import com.avast.android.butterknifezelezny.iface.OnCheckBoxStateChangedListener;
import com.avast.android.butterknifezelezny.model.Element;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class EntryList extends JPanel {

    protected Project mProject;
    protected Editor mEditor;
    protected ArrayList<Element> mElements = new ArrayList<Element>();
    protected ArrayList<String> mGeneratedIDs = new ArrayList<String>();
    protected ArrayList<Entry> mEntries = new ArrayList<Entry>();
    protected boolean mCreateHolder;
    protected boolean mSpiltOnClickMethods = false;
    protected boolean mGenerateInjectionMethod = false;
    protected String mPrefix = null;
    protected IConfirmListener mConfirmListener;
    protected ICancelListener mCancelListener;
    protected JButton mConfirm;
    protected JButton mCancel;
    protected EntryHeader mEntryHeader;

    private OnCheckBoxStateChangedListener allCheckListener = new OnCheckBoxStateChangedListener() {
        @Override
        public void changeState(boolean checked) {
            for (final Entry entry : mEntries) {
                entry.setListener(null);
                entry.getCheck().setSelected(checked);
                entry.setListener(singleCheckListener);
            }
        }
    };

    private OnCheckBoxStateChangedListener singleCheckListener = new OnCheckBoxStateChangedListener() {
        @Override
        public void changeState(boolean checked) {
            boolean result = true;
            for (Entry entry : mEntries) {
                result &= entry.getCheck().isSelected();
            }

            mEntryHeader.setAllListener(null);
            mEntryHeader.getAllCheck().setSelected(result);
            mEntryHeader.setAllListener(allCheckListener);
        }
    };

    public EntryList(Project project, Editor editor, ArrayList<Element> elements, ArrayList<String> ids,
                     boolean createHolder, boolean autoGenerateInjectionMethod,
                     IConfirmListener confirmListener, ICancelListener cancelListener) {
        mProject = project;
        mEditor = editor;
        mCreateHolder = createHolder;
        mGenerateInjectionMethod = autoGenerateInjectionMethod;
        mConfirmListener = confirmListener;
        mCancelListener = cancelListener;
        if (elements != null) {
            mElements.addAll(elements);
        }
        if (ids != null) {
            mGeneratedIDs.addAll(ids);
        }

        setPreferredSize(new Dimension(640, 360));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        addInjections();
        addButtons();
    }

    protected void addInjections() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mEntryHeader = new EntryHeader();
        contentPanel.add(mEntryHeader);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel injectionsPanel = new JPanel();
        injectionsPanel.setLayout(new BoxLayout(injectionsPanel, BoxLayout.PAGE_AXIS));
        injectionsPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        int cnt = 0;
        boolean selectAllCheck = true;
        for (Element element : mElements) {
            Entry entry = new Entry(this, element, mGeneratedIDs);
            entry.setListener(singleCheckListener);

            if (cnt > 0) {
                injectionsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
            injectionsPanel.add(entry);
            cnt++;

            mEntries.add(entry);

            selectAllCheck &= entry.getCheck().isSelected();
        }
        mEntryHeader.getAllCheck().setSelected(selectAllCheck);
        mEntryHeader.setAllListener(allCheckListener);
        injectionsPanel.add(Box.createVerticalGlue());
        injectionsPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        JBScrollPane scrollPane = new JBScrollPane(injectionsPanel);
        contentPanel.add(scrollPane);

        add(contentPanel, BorderLayout.CENTER);
        refresh();
    }

    protected void addButtons() {

        JPanel holderPanel = createCheckBoxItemPanel("Create ViewHolder", mCreateHolder, selected -> mCreateHolder = selected);
        add(holderPanel, BorderLayout.PAGE_END);

        JPanel splitOnclickMethodsPanel = createCheckBoxItemPanel("Split OnClick methods", false,
            selected -> mSpiltOnClickMethods = selected);
        add(splitOnclickMethodsPanel, BorderLayout.PAGE_END);

        JPanel generateInjectionMethod = createCheckBoxItemPanel("Auto generate injection method", mGenerateInjectionMethod,
            selected -> mGenerateInjectionMethod = selected);
        add(generateInjectionMethod, BorderLayout.PAGE_END);

        mCancel = createButton("Cancel", new CancelAction());
        mConfirm = createButton("Confirm", new ConfirmAction());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(mCancel);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(mConfirm);

        add(buttonPanel, BorderLayout.PAGE_END);
        refresh();
    }

    private JPanel createCheckBoxItemPanel(String text, boolean checked, CheckBoxSelectChangeListener listener) {
        JCheckBox checkBox = new JCheckBox();
        checkBox.setPreferredSize(new Dimension(32, 26));
        checkBox.setSelected(checked);
        checkBox.addChangeListener(e -> {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            if (listener != null) listener.onSelectChanged(selected);
        });


        JLabel label = new JLabel(text);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        panel.add(checkBox);
        panel.add(label);
        panel.add(Box.createHorizontalGlue());
        return panel;
    }

    private JButton createButton(String text, AbstractAction action) {
        JButton button = new JButton();
        button.setAction(action);
        button.setPreferredSize(new Dimension(120, 26));
        button.setText(text);
        button.setVisible(true);
        return button;
    }


    protected void refresh() {
        revalidate();

        if (mConfirm != null) {
            mConfirm.setVisible(mElements.size() > 0);
        }
    }

    protected boolean checkValidity() {
        boolean valid = true;

        for (Element element : mElements) {
            if (!element.checkValidity()) {
                valid = false;
            }
        }

        return valid;
    }

    public JButton getConfirmButton() {
        return mConfirm;
    }
    // classes

    public interface CheckBoxSelectChangeListener {
        void onSelectChanged(boolean selected);
    }

    protected class ConfirmAction extends AbstractAction {

        public void actionPerformed(ActionEvent event) {
            boolean valid = checkValidity();

            for (Entry entry : mEntries) {
                entry.syncElement();
            }

            if (valid) {
                if (mConfirmListener != null) {
                    mConfirmListener.onConfirm(mProject, mEditor, mElements, mPrefix, mCreateHolder,
                        mSpiltOnClickMethods, mGenerateInjectionMethod);
                }
            }
        }
    }

    protected class CancelAction extends AbstractAction {

        public void actionPerformed(ActionEvent event) {
            if (mCancelListener != null) {
                mCancelListener.onCancel();
            }
        }
    }
}
