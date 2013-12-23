package eu.inmite.android.plugin.butterknifezelezny.form;

import javax.swing.*;
import java.awt.*;

public class EntryHeader extends JPanel {

	protected JLabel mType;
	protected JLabel mID;
	protected JLabel mName;

	public EntryHeader() {
		mType = new JLabel("Element");
		mType.setPreferredSize(new Dimension(100, 26));
		mType.setFont(new Font(mType.getFont().getFontName(), Font.BOLD, mType.getFont().getSize()));

		mID = new JLabel("ID");
		mID.setPreferredSize(new Dimension(160, 26));
		mID.setFont(new Font(mID.getFont().getFontName(), Font.BOLD, mID.getFont().getSize()));

		mName = new JLabel("Variable Name");
		mName.setPreferredSize(new Dimension(260, 26));
		mName.setFont(new Font(mName.getFont().getFontName(), Font.BOLD, mName.getFont().getSize()));

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		add(Box.createRigidArea(new Dimension(52, 0)));
		add(mType);
		add(Box.createRigidArea(new Dimension(12, 0)));
		add(mID);
		add(Box.createRigidArea(new Dimension(22, 0)));
		add(mName);
		add(Box.createHorizontalGlue());
	}
}
