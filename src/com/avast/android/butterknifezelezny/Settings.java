package com.avast.android.butterknifezelezny;

import com.avast.android.butterknifezelezny.common.Utils;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Settings UI for the plugin.
 *
 * @author David Vávra (vavra@avast.com)
 */
public class Settings implements Configurable {

    public static final String PREFIX = "butterknifezelezny_prefix";
    public static final String VIEWHOLDER_CLASS_NAME = "butterknifezelezny_viewholder_class_name";
    public static final String AUTO_GENERATE_ACTIVITY_METHOD= "butterknifezelezny_auto_generate_activity_method";
    public static final String AUTO_GENERATE_FRAGMENT_METHOD = "butterknifezelezny_auto_generate_fragment_method";

    private JPanel mPanel;
    private JTextField mHolderName;
    private JTextField mPrefix;
    private JCheckBox mAutoGenerateActivityMethod;
    private JCheckBox mAutoGenerateFragmentMethod;

    @Nls
    @Override
    public String getDisplayName() {
        return "ButterKnifeZelezny";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        reset();
        return mPanel;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        PropertiesComponent.getInstance().setValue(PREFIX, mPrefix.getText());
        PropertiesComponent.getInstance().setValue(VIEWHOLDER_CLASS_NAME, mHolderName.getText());
        Utils.saveBoolean(AUTO_GENERATE_ACTIVITY_METHOD, mAutoGenerateActivityMethod.isSelected());
        Utils.saveBoolean(AUTO_GENERATE_FRAGMENT_METHOD, mAutoGenerateFragmentMethod.isSelected());
    }

    @Override
    public void reset() {
        mPrefix.setText(Utils.getPrefix());
        mHolderName.setText(Utils.getViewHolderClassName());
        mAutoGenerateActivityMethod.setSelected(Utils.isAutoGenerateActivityMethod());
        mAutoGenerateFragmentMethod.setSelected(Utils.isAutoGenerateFragmentMethod());
    }

    @Override
    public void disposeUIResources() {

    }
}
