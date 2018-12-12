package cn.com.nio.fellow.ideaplugin.action;

import com.intellij.psi.*;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;

/**
 * This class can be used to filter out some unnecessary received info
 */
public class ReceiverFilter implements Filter {
    @Override
    public boolean shouldShow(Usage usage) {
        PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();

        // find which method does this method belong to recursively
        // although the implementation is shit, but we cannot linearise it (from my point of view).
        if (element instanceof PsiJavaCodeReferenceElement) {
            if ((element = element.getParent()) instanceof PsiTypeElement) {
                if ((element = element.getParent()) instanceof PsiParameter) {
                    if ((element = element.getParent()) instanceof PsiParameterList) {
                        if ((element = element.getParent()) instanceof PsiMethod) {
                            PsiMethod method = (PsiMethod) element;
                            if (PsiUtils.isActionCreated(method)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
}
