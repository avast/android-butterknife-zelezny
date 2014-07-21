package eu.inmite.android.plugin.butterknifezelezny;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import eu.inmite.android.plugin.butterknifezelezny.common.Utils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Settings of the plugin.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class Settings implements Configurable {

    public static final String PREFIX = "butterknifezelezny_prefix";
    JTextField mPrefix;

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
        System.out.println("create");
        JPanel panel = new JPanel();
        panel.add(new JLabel("Prefix for generated members: "));
        mPrefix = new JTextField(Utils.getPrefix(), 5);
        panel.add(mPrefix);
        return panel;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        System.out.println("apply");
        PropertiesComponent.getInstance().setValue(PREFIX, mPrefix.getText());
    }

    @Override
    public void reset() {

    }

    @Override
    public void disposeUIResources() {

    }
}
