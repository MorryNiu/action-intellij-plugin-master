package cn.com.nio.fellow.ideaplugin.action;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.*;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.*;

public class ActionLineMarkerProvider implements LineMarkerProvider {
    // load icon
    public static final Icon ICON = IconLoader.getIcon("/icons/icon.png");
    public static final Icon ICON_R = IconLoader.getIcon("/icons/icon_r.png");

    public static final int MAX_USAGES = 100;

    // the place where action has been created
    public static Set<PsiElement> createdPlace = new HashSet<>();
    // the place where action has been built
    public static Set<PsiElement> builtPlace = new HashSet<>();

    public static Integer passed = 0;

    private static GutterIconNavigationHandler<PsiElement> SHOW_BUILD =
            (e, psiElement) -> {
                if (psiElement instanceof PsiClass) {
                    ArrayList<String> diagnose = new ArrayList<>();

                    // get the project to which the PSI element belongs to, it contains the basic information of a project
                    // for example, the name and root path of a file or dir.
                    Project project = psiElement.getProject();
                    // define the scope we are currently working in
                    JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
                    PsiClass actionClass = javaPsiFacade.findClass("cn.com.nio.fellow.module.core.launcher.ModuleRouter", GlobalSearchScope.allScope(project));
                    String path = PsiUtils.getAnnotationValue(psiElement, "path").replaceAll("[^a-zA-Z0-9._]+", "");



                    if(actionClass != null) {
                        PsiMethod buildMethod = actionClass.findMethodsByName("buildAction", false)[0];
                        Filter buildFilter = new BuildFilter(path);

                        //search reference
                        Query<PsiReference> search = ReferencesSearch.search(buildMethod);
                        Collection<PsiReference> psiReferences = search.findAll();
                        ArrayList<Usage> usages = new ArrayList<>();

                        for(PsiReference r:psiReferences){
                            UsageInfo usageInfo = new UsageInfo(r);
                            Usage usage = new UsageInfo2UsageAdapter(usageInfo);
                            // show or not
                            if(buildFilter.shouldShow(usage)) {
                                usages.add(usage);
                            }
                        }


                        PsiParameter par = buildMethod.getParameterList().getParameters()[0];
                        diagnose.add("Method name: " + buildMethod.getName());
                        diagnose.add("annotation number: " + buildMethod.getAnnotations().length);
                        diagnose.add("post method parameterList: " + buildMethod.getParameterList().getText());
                        diagnose.add("pars: ");
                        diagnose.add("passed " + passed + " times.");
                        diagnose.add("usages found: " + usages.size());
                        diagnose.add("path: " + path);

                        UsageViewManager.getInstance(project).showUsages(UsageTarget.EMPTY_ARRAY, usages.toArray(new Usage[usages.size()]), new UsageViewPresentation());

                        new ShowUsagesAction(new SenderFilter(par.getText())).startFindUsages(buildMethod, new RelativePoint(e), PsiUtilBase.findEditor(psiElement), MAX_USAGES, diagnolise(diagnose));
                    }
                }
            };

    private static GutterIconNavigationHandler<PsiElement> SHOW_CREATE =
            (e, psiElement) -> {
                if (psiElement instanceof PsiMethodCallExpression) {
                    PsiMethodCallExpression expression = (PsiMethodCallExpression) psiElement;
                    PsiType[] expressionTypes = expression.getArgumentList().getExpressionTypes();

                    if (expressionTypes.length > 0) {
                        PsiClass eventClass = PsiUtils.getClass(expressionTypes[0]);
                        if (eventClass != null) {

                            String an = "";
                            PsiElement temp = null;

                            for (PsiElement p : createdPlace) {
                                an += PsiUtils.getAnnotationValue(p, "path") + "\n";
                                temp = p;
                                if(processOutput(expression.getArgumentList().getText()).equals(PsiUtils.getAnnotationValue(p, "path"))){
                                    an = p.getText() + "&*^%";
                                    break;
                                }
                            }

                            //trying to find the target class
                            Project project = psiElement.getProject();
                            // define the scope we are currently working in
                            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
                            String target = "cn.com.nio.fellow." + expression.getArgumentList().getText().substring(2, expression.getArgumentList().getText().length()-2).split("/")[0] + ".action";

                            // package we are interested in
                            PsiPackage targetClasses = javaPsiFacade.findPackage(target);

                            //PsiUtils.getAnnotationValue(targetClass, "path");
/*
                            if(targetClasses.length > 0){
                                for(PsiClass p:targetClasses){
                                    if(PsiUtils.getAnnotationValue(p, "path").equals(processOutput(expression.getArgumentList().getText()))){
                                        temp = p;
                                        break;
                                    }
                                }
                            }*/

                            Boolean navi = false;
                            int run = 0;
                            String clses = "";

                            for(PsiClass c: targetClasses.getClasses()){
                                run++;
                                clses += PsiUtils.getAnnotationValue(c,"path") + "\n";
                                if(processOutput(PsiUtils.getAnnotationValue(c,"path")).equals(processOutput(expression.getArgumentList().getText()))){
                                    temp = c;
                                    navi = true;
                                    break;
                                }
                            }



                            new OpenFileDescriptor(temp.getProject(), temp.getContainingFile().getVirtualFile(), 1,0).navigate(true);

    /*                        new ShowUsagesAction(new ReceiverFilter()).startFindUsages(temp, new RelativePoint(e), PsiUtilBase.findEditor(psiElement), MAX_USAGES,
                                    "Arglist: " + expression.getArgumentList().getText().substring(2, expression.getArgumentList().getText().length()-2) + "\n"
                                    + "processed: " + processOutput(expression.getArgumentList().getText()) + "\n"
                                    + "ExpressionName:" + expression.getText() + "\n"
                                    //+ "CreatedPlace[0] " + createdPlace.get(0).getText() + "\n"
                                    + "size: " + createdPlace.size() + "\n"
                                    + "annotation: " + an
                                    + "target class: " + target + "\n"
                                    + "navi? " + navi + " \n"
                                    + "run: " + run + "\n"
                                    + "clses: " + clses + "\n"
                                    //+ "hardcode path: " + (targetPackage == null?"null":targetPackage.getName()) + "\n"
                                    //+ "find class: " + "cn.com.nio.fellow." + expression.getArgumentList().getText().substring(2, expression.getArgumentList().getText().length()-2).split("/")[0] + ".action" + "\n"
                                    //+ "hard code result: " + (targetClass == null ? "null": targetClass.getQualifiedName()) + "\n"
                                    //+ "true result: " + (targetClasses.getClasses().length > 0 ? targetClasses.getClasses()[0].getText() : "null" + "\n")
                                    //+ "random shit: " + targetClass.getText() + "\n"
                                    //+ "random shit2: " + targetClass.getImplementsList().getReferencedTypes()[0].resolve().getText()
                                    //+ "target package: " + (targetPackage == null? "null":targetPackage.getClasses().length)

                            );*/
                        }
                    }
                }
            };


    /**
     * remove all unwanted character from a path string
     * @param input the path string that we want to process
     * @return a processed string
     */
    public static String processOutput(String input){
        if(input.contains("/")) {
            input = input.substring(input.indexOf("/"));
        }
        return input.replaceAll("[^a-zA-Z0-9._]+", "");
    }

    /**
     * make the diagnose text from a list of string
     * @param input the string need to be concatenated
     * @return output string
     */
    public static String diagnolise(ArrayList<String> input){
        String res = "";

        for (String s:input) {
            res += s + "\n";
        }
        return res;
    }


    /**
     * When icon is clicked, this method will be called (i guess)
     * @param psiElement The element that will be connect to the event
     * @return LineMarkerInfo for event creation
     */
    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {

        if (PsiUtils.isActionBuilt(psiElement)) {
            // psiMethod
            builtPlace.add(psiElement);

            return new LineMarkerInfo<>(psiElement, psiElement.getTextRange(), ICON,
                    Pass.UPDATE_ALL, null, SHOW_CREATE,
                    GutterIconRenderer.Alignment.LEFT);

        } else if (PsiUtils.isActionCreated(psiElement)) {
            // psiClass
            createdPlace.add(psiElement);

            return new LineMarkerInfo<>(psiElement, psiElement.getTextRange(), ICON_R,
                    Pass.UPDATE_ALL, null, SHOW_BUILD,
                    GutterIconRenderer.Alignment.LEFT);
        }

        passed++;
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> list, @NotNull Collection<LineMarkerInfo> collection) {
    }
}
