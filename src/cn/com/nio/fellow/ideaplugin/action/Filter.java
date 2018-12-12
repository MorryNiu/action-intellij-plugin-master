package cn.com.nio.fellow.ideaplugin.action;

import com.intellij.psi.PsiElementFactory;
import com.intellij.usages.Usage;

/**
 * Created by kgmyshin on 2015/06/07.
 */
public interface Filter {
    boolean shouldShow(Usage usage);
}
