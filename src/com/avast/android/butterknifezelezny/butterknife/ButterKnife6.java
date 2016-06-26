package com.avast.android.butterknifezelezny.butterknife;

/**
 * ButterKnife version 6
 *
 * @author Tomáš Kypta
 * @since 1.3
 */
public class ButterKnife6 extends AbstractButterKnife {

    private static final String mFieldAnnotationSimpleName = "InjectView";
    private static final String mSimpleBindStatement = "ButterKnife.inject";
    private static final String mSimpleUnbindStatement = "ButterKnife.reset";


    @Override
    public String getVersion() {
        return "6.1.0";
    }

    @Override
    public String getDistinctClassName() {
        return getFieldAnnotationCanonicalName();
    }

    @Override
    public String getFieldAnnotationSimpleName() {
        return mFieldAnnotationSimpleName;
    }

    @Override
    public String getSimpleBindStatement() {
        return mSimpleBindStatement;
    }

    @Override
    public String getSimpleUnbindStatement() {
        return mSimpleUnbindStatement;
    }

    @Override
    public boolean isUsingUnbinder() {
        return false;
    }

    @Override
    public String getUnbinderClassSimpleName() {
        return null;
    }
}
