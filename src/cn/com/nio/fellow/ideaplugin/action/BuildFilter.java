package cn.com.nio.fellow.ideaplugin.action;

import com.intellij.CommonBundle;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;

/**
 * This class can be used to select the info that we actually want to send
 */
public class BuildFilter implements Filter {

    public final String actionStr;

    public BuildFilter(String actionStr) {
        this.actionStr = actionStr;
    }

    @Override
    public boolean shouldShow(Usage usage) {
        PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();

        if (element instanceof PsiReferenceExpression) {
            if ((element = element.getParent()) instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression callExpression = (PsiMethodCallExpression) element;

                PsiExpression[] exprs = callExpression.getArgumentList().getExpressions();

/*                String popUp = exprs[0].getText();

                Messages.showMessageDialog(element.getProject(), popUp,
                        CommonBundle.getErrorTitle(), Messages.getErrorIcon());*/

                for (PsiExpression pe : exprs) {
                    if (ActionLineMarkerProvider.processOutput(pe.getText()).equals(actionStr)) {
                        return true;
                    }
                }
            }

            if ((element = element.getParent()) instanceof PsiExpressionStatement) {
                if ((element = element.getParent()) instanceof PsiCodeBlock) {
                    PsiCodeBlock codeBlock = (PsiCodeBlock) element;
                    PsiStatement[] statements = codeBlock.getStatements();
                    for (PsiStatement statement : statements) {
                        if (statement instanceof PsiDeclarationStatement) {
                            PsiDeclarationStatement declarationStatement = (PsiDeclarationStatement) statement;
                            PsiElement[] elements = declarationStatement.getDeclaredElements();
                            for (PsiElement variable : elements) {
                                if (variable instanceof PsiLocalVariable) {
                                    PsiLocalVariable localVariable = (PsiLocalVariable) variable;
                                    PsiClass psiClass = PsiUtils.getClass(localVariable.getTypeElement().getType());
                                    if (psiClass.getName().equals(actionStr)) {
                                        // pattern :
                                        //   Event event = new Event();
                                        //   EventBus.getDefault().post(event);
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
}

