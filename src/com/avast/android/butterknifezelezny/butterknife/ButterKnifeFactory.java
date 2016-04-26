package com.avast.android.butterknifezelezny.butterknife;

import com.avast.android.butterknifezelezny.common.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Factory for obtaining proper ButterKnife version.
 *
 * @author Tomáš Kypta
 * @since 1.3
 */
public class ButterKnifeFactory {

    /**
     * List of supported ButterKnifes.
     * Note: The ordering corresponds to the preferred ButterKnife versions.
     */
    private static IButterKnife[] sSupportedButterKnives = new IButterKnife[]{
            new ButterKnife7(),
            new ButterKnife6(),
            new ButterKnife8()
    };

    private ButterKnifeFactory() {
        // no construction
    }

    /**
     * Find ButterKnife that is available for given {@link PsiElement} in the {@link Project}.
     * Note that it check if ButterKnife is available in the module.
     *
     * @param project    Project
     * @param psiElement Element for which we are searching for ButterKnife
     * @return ButterKnife
     */
    @Nullable
    public static IButterKnife findButterKnifeForPsiElement(@NotNull Project project, @NotNull PsiElement psiElement) {
        for (IButterKnife butterKnife : sSupportedButterKnives) {
            if (Utils.isClassAvailableForPsiFile(project, psiElement, butterKnife.getDistinctClassName())) {
                return butterKnife;
            }
        }
        // we haven't found any version of ButterKnife in the module, let's fallback to the whole project
        return findButterKnifeForProject(project);
    }

    /**
     * Find ButterKnife that is available in the {@link Project}.
     *
     * @param project Project
     * @return ButterKnife
     * @since 1.3.1
     */
    @Nullable
    private static IButterKnife findButterKnifeForProject(@NotNull Project project) {
        for (IButterKnife butterKnife : sSupportedButterKnives) {
            if (Utils.isClassAvailableForProject(project, butterKnife.getDistinctClassName())) {
                return butterKnife;
            }
        }
        return null;
    }

    public static IButterKnife[] getSupportedButterKnives() {
        return sSupportedButterKnives;
    }
}
