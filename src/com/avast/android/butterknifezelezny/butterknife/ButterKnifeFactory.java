package com.avast.android.butterknifezelezny.butterknife;

import com.avast.android.butterknifezelezny.common.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.EverythingGlobalScope;
import org.jetbrains.annotations.NotNull;

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
    private static IButterKnife[] sSupportedButterKnives = new IButterKnife[] {
            new ButterKnife7(),
            new ButterKnife6()
    };

    private ButterKnifeFactory() {
        // no construction
    }

    /**
     * Find ButterKnife that is available for given {@link PsiElement} in the {@link Project}.
     * Note that it check if ButterKnife is available in the module.
     *
     * @param project Project
     * @param psiElement Element for which we are searching for ButterKnife
     * @return ButterKnife
     */
    public static IButterKnife findButterKnifeForPsiElement(@NotNull Project project, @NotNull PsiElement psiElement) {
        for (IButterKnife butterKnife : sSupportedButterKnives) {
            if (Utils.isClassAvailableForPsiFile(project, psiElement, butterKnife.getDistinctClassName())) {
                return butterKnife;
            }
        }
        return null;
    }

    public static IButterKnife[] getSupportedButterKnives() {
        return sSupportedButterKnives;
    }
}
