package com.avast.android.butterknifezelezny.butterknife;

import java.util.regex.Pattern;

/**
 * Abstraction of ButterKnife versions
 *
 * @author Tomáš Kypta
 * @since 1.3
 */
public interface IButterKnife {

    String getVersion();

    /**
     * Class name used to determine version of ButterKnife that is available for generation.
     *
     * @return Class name of a class in ButterKnife library
     */
    String getDistinctClassName();

    /**
     * Get regex pattern for matching InjectView/Bind fields.
     *
     * @return The pattern
     */
    Pattern getFieldAnnotationPattern();

    /**
     * Simple class name of the field annotation for a view
     *
     * @return Simple name of the field annotation class
     */
    String getFieldAnnotationSimpleName();

    /**
     * Canonical class name of the field annotation for a view
     *
     * @return Canonical name of the field annotation class
     */
    String getFieldAnnotationCanonicalName();

    /**
     * Canonical class name of the @OnClick method annotation
     *
     * @return Canonical name of the @OnClick annotation class
     */
    String getOnClickAnnotationCanonicalName();

    /**
     * Package name of the ButterKnife version
     *
     * @return Package name
     */
    String getPackageName();

    /**
     * Statement to bind/inject view instances to the fields
     *
     * @return Bind/inject statement
     */
    String getSimpleBindStatement();

    /**
     * Statement (including package name) to bind/inject view instances to the fields
     *
     * @return Bind/inject statement
     */
    String getCanonicalBindStatement();

    /**
     * Statement to unbind/reset view instances from the fields
     *
     * @return Unbind/reset statement
     */
    String getSimpleUnbindStatement();

    /**
     * Statement (including package name) to unbind/reset view instances from the fields
     *
     * @return Unbind/reset statement
     */
    String getCanonicalUnbindStatement();
}
