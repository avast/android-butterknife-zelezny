package com.avast.android.butterknifezelezny.butterknife;

import java.util.regex.Pattern;

/**
 * @author Tomáš Kypta
 * @since 1.3
 */
public abstract class AbstractButterKnife implements IButterKnife {

    private final Pattern mFieldAnnotationPattern = Pattern.compile("^@" + getFieldAnnotationSimpleName() + "\\(([^\\)]+)\\)$", Pattern.CASE_INSENSITIVE);
    private static final String mPackageName = "butterknife";
    private final String mFieldAnnotationCanonicalName = getPackageName() + "." + getFieldAnnotationSimpleName();
    private final String mCanonicalBindStatement = getPackageName() + "." + getSimpleBindStatement();
    private final String mCanonicalUnbindStatement = getPackageName() + "." + getSimpleUnbindStatement();
    private final String mOnClickCanonicalName = getPackageName() + ".OnClick";


    @Override
    public Pattern getFieldAnnotationPattern() {
        return mFieldAnnotationPattern;
    }

    @Override
    public String getPackageName() {
        return mPackageName;
    }

    @Override
    public String getFieldAnnotationCanonicalName() {
        return mFieldAnnotationCanonicalName;
    }

    @Override
    public String getOnClickAnnotationCanonicalName() {
        return mOnClickCanonicalName;
    }

    @Override
    public String getCanonicalBindStatement() {
        return mCanonicalBindStatement;
    }

    @Override
    public String getCanonicalUnbindStatement() {
        return mCanonicalUnbindStatement;
    }
}
