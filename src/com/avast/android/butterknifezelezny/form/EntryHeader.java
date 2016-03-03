package com.avast.android.butterknifezelezny.form;

import javax.swing.*;
import java.awt.*;

public class EntryHeader extends JPanel {

    protected JCheckBox mAllCheck;
    protected JLabel mType;
    protected JLabel mID;
    protected JLabel mEvent;
    protected JLabel mName;

    public EntryHeader() {
        mAllCheck = new JCheckBox();
        mAllCheck.setPreferredSize(new Dimension(40, 26));
        mAllCheck.setSelected(false);

        mType = new JLabel("Element");
        mType.setPreferredSize(new Dimension(100, 26));
        mType.setFont(new Font(mType.getFont().getFontName(), Font.BOLD, mType.getFont().getSize()));

        mID = new JLabel("ID");
        mID.setPreferredSize(new Dimension(100, 26));
        mID.setFont(new Font(mID.getFont().getFontName(), Font.BOLD, mID.getFont().getSize()));

        mEvent = new JLabel("OnClick");
        mEvent.setPreferredSize(new Dimension(100, 26));
        mEvent.setFont(new Font(mEvent.getFont().getFontName(), Font.BOLD, mEvent.getFont().getSize()));

        mName = new JLabel("Variable Name");
        mName.setPreferredSize(new Dimension(100, 26));
        mName.setFont(new Font(mName.getFont().getFontName(), Font.BOLD, mName.getFont().getSize()));

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(Box.createRigidArea(new Dimension(1, 0)));
        add(mAllCheck);
        add(Box.createRigidArea(new Dimension(11, 0)));
        add(mType);
        add(Box.createRigidArea(new Dimension(12, 0)));
        add(mID);
        add(Box.createRigidArea(new Dimension(12, 0)));
        add(mEvent);
        add(Box.createRigidArea(new Dimension(22, 0)));
        add(mName);
        add(Box.createHorizontalGlue());
    }

    public JCheckBox getAllCheck() {
        return mAllCheck;
    }

    public boolean isAllChecked() {
        return mAllCheck.isSelected();
    }
}
