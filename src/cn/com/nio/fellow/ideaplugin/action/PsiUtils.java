package cn.com.nio.fellow.ideaplugin.action;

import com.intellij.psi.*;

public class PsiUtils {

    public static PsiClass getClass(PsiType psiType) {
        if (psiType instanceof PsiClassType) {
            return ((PsiClassType) psiType).resolve();
        }
        return null;
    }

    /**
     * To determine if the PsiElement is the place that action got created
     * @param psiElement the psiElement that will be tested
     * @return if the PsiElement is the place that action got created
     */
    public static boolean isActionCreated(PsiElement psiElement) {

        if (psiElement instanceof PsiClass) {
            PsiClass pclass = (PsiClass) psiElement;
            PsiModifierList modifierList = pclass.getModifierList();
                // retrieve the annotation that we are interested in
                for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
                    if (psiAnnotation.getQualifiedName().equals("cn.com.nio.fellow.module.base.annotation.Action")) {
                        return true;
                }
            }
        }

        return false;
    }

    /**
     * @param psiElement The method with annotation
     * @param str The target attribute
     * @return the value of the path in a annotation
     */
    public static String getAnnotationValue(PsiElement psiElement, String str) {
        if (psiElement instanceof PsiClass) {
            PsiClass pclass = (PsiClass) psiElement;

            PsiModifierList modifierList = pclass.getModifierList();

            for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
                if (psiAnnotation.getQualifiedName().equals("cn.com.nio.fellow.module.base.annotation.Action")) {
                    PsiAnnotation[] psiAnnotations=   psiAnnotation.getOwner().getAnnotations();
                    if (psiAnnotations!=null){
                        for (PsiAnnotation psi:psiAnnotations){
                           return psi.findAttributeValue(str).getText();
                        }
                    }

                }
            }
        }
        return "@131313*&*(";
    }


    public static String getParameterValue(PsiElement psi){
        if(psi instanceof PsiMethod){
            PsiParameter par = ((PsiMethod) psi).getParameterList().getParameters()[0];

            return par.getText();
        }

        return null;
    }

    /**
     * To determine if the PsiElement is the place that action got built up
     * @param psiElement the psiElement that will be tested
     * @return if the PsiElement is the place that action got built up
     */
    public static boolean isActionBuilt(PsiElement psiElement) {

        if (psiElement instanceof PsiCallExpression) {

            PsiCallExpression callExpression = (PsiCallExpression) psiElement;
            PsiMethod method = callExpression.resolveMethod();

            if (method != null) {
                String name = method.getName();
                PsiElement parent = method.getParent();

                if (name != null && name.equals("buildAction") && parent instanceof PsiClass) {
                    PsiClass implClass = (PsiClass) parent;

                    if (isActionClass(implClass) || isSuperClassAction(implClass)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isActionClass(PsiClass psiClass) {
        if (psiClass.getName().equals("ModuleRouter")) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isSuperClassAction(PsiClass psiClass) {
        PsiClass[] supers = psiClass.getSupers();

        if (supers.length == 0) {
            return false;
        }

        for (PsiClass superClass : supers) {

            if (isActionClass(superClass)) {
                return true;
            }
        }
        return false;
    }

}
