package com.avast.android.butterknifezelezny.navigation;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Predicate;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.AnnotatedMembersSearch;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.avast.android.butterknifezelezny.navigation.PsiHelper.getAnnotation;
import static com.avast.android.butterknifezelezny.navigation.PsiHelper.hasAnnotationWithValue;

public class NavigationMarkerProvider implements LineMarkerProvider {

    private static final String ON_CLICK_ANNOTATION = "butterknife.OnClick";
    private static final String INJECT_VIEW_ANNOTATION = "butterknife.InjectView";

    private static final Predicate<PsiElement> IS_FIELD_IDENTIFIER = new Predicate<PsiElement>() {
        @Override
        public boolean apply(@Nullable PsiElement element) {
            return element != null && element instanceof PsiIdentifier && element.getParent() instanceof PsiField;
        }
    };

    private static final Predicate<PsiElement> IS_METHOD_IDENTIFIER = new Predicate<PsiElement>() {
        @Override
        public boolean apply(@Nullable PsiElement element) {
            return element != null && element instanceof PsiIdentifier && element.getParent() instanceof PsiMethod;
        }
    };

    private enum AnnotationLink {
        FIELD(INJECT_VIEW_ANNOTATION, ON_CLICK_ANNOTATION, PsiMethod.class),
        METHOD(ON_CLICK_ANNOTATION, INJECT_VIEW_ANNOTATION, PsiField.class);

        private final String srcAnnotation;
        private final String dstAnnotation;
        private final Class<? extends PsiMember> dstClassMember;

        AnnotationLink(String srcAnnotation, String dstAnnotation, Class<? extends PsiMember> dstClassMember) {
            this.srcAnnotation = srcAnnotation;
            this.dstAnnotation = dstAnnotation;
            this.dstClassMember = dstClassMember;
        }
    }

    /**
     * Check if element is a method annotated with <em>@OnClick</em> or a field annotated with
     * <em>@InjectView</em> and create corresponding navigation link.
     *
     * @return a {@link com.intellij.codeInsight.daemon.GutterIconNavigationHandler} for the
     * appropriate type, or null if we don't care about it.
     */
    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull final PsiElement element) {
        if (IS_FIELD_IDENTIFIER.apply(element)) {
            return getNavigationLineMarker((PsiIdentifier)element, AnnotationLink.FIELD);
        } else if (IS_METHOD_IDENTIFIER.apply(element)) {
            return getNavigationLineMarker((PsiIdentifier)element, AnnotationLink.METHOD);
        }

        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> psiElements, @NotNull Collection<LineMarkerInfo> lineMarkerInfos) {
        // empty
    }

    @Nullable
    private LineMarkerInfo getNavigationLineMarker(@NotNull final PsiIdentifier element, @NotNull AnnotationLink link) {

        final PsiAnnotation srcAnnotation = getAnnotation(element.getParent(), link.srcAnnotation);
        if (srcAnnotation != null) {
            final PsiAnnotationParameterList annotationParameters = srcAnnotation.getParameterList();
            if (annotationParameters.getAttributes().length > 0) {
                final String resourceId = annotationParameters.getAttributes()[0].getValue().getText();

                final PsiClass dstAnnotationClass = JavaPsiFacade.getInstance(element.getProject()).findClass(link.dstAnnotation,
                    ProjectScope.getLibrariesScope(element.getProject()));

                final ClassMemberProcessor processor = new ClassMemberProcessor(resourceId, link);

                AnnotatedMembersSearch.search(dstAnnotationClass, GlobalSearchScope.fileScope
                    (element.getContainingFile())).forEach(processor);
                final PsiMember dstMember = processor.getResultMember();
                if (dstMember != null) {
                    return new NavigationMarker.Builder().from(element).to(dstMember).build();
                }
            }
        }

        return null;
    }

    private class ClassMemberProcessor implements Processor<PsiMember> {
        private final String resourceId;
        private final AnnotationLink link;
        private PsiMember resultMember;

        public ClassMemberProcessor(@NotNull final String resourceId, @NotNull final AnnotationLink link) {
            this.resourceId = resourceId;
            this.link = link;
        }

        @Override
        public boolean process(PsiMember psiMember) {
            if (link.dstClassMember.isInstance(psiMember) &&
                hasAnnotationWithValue(psiMember, link.dstAnnotation, resourceId)) {
                resultMember = psiMember;
                return false;
            }
            return true;
        }

        @Nullable
        public PsiMember getResultMember() {
            return resultMember;
        }
    }
}
