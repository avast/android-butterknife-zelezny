package com.avast.android.butterknifezelezny.form;

import com.avast.android.butterknifezelezny.iface.OnCheckBoxStateChangedListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class EntryHeader extends JPanel {

    protected JCheckBox mAllCheck;
    protected JLabel mType;
    protected JLabel mID;
    protected JLabel mEvent;
    protected JLabel mName;
    protected OnCheckBoxStateChangedListener mAllListener;

    public void setAllListener(final OnCheckBoxStateChangedListener onStateChangedListener) {
        this.mAllListener = onStateChangedListener;
    }

    public EntryHeader() {
        mAllCheck = new JCheckBox();
        mAllCheck.setPreferredSize(new Dimension(40, 26));
        mAllCheck.setSelected(false);
        mAllCheck.addItemListener(new AllCheckListener());

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

    // classes

    private class AllCheckListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent itemEvent) {
            if (mAllListener != null) {
                mAllListener.changeState(itemEvent.getStateChange() == ItemEvent.SELECTED);
            }
        }
    }
}
