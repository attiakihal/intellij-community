package com.jetbrains.python.findUsages;

import com.intellij.find.findUsages.AbstractFindUsagesDialog;
import com.intellij.find.findUsages.CommonFindUsagesDialog;
import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyUtil;
import com.jetbrains.python.psi.impl.PyImportedModule;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author yole
 */
public class PyModuleFindUsagesHandler extends FindUsagesHandler {
  private final PsiFileSystemItem myElement;

  protected PyModuleFindUsagesHandler(@NotNull PsiFileSystemItem file) {
    super(file);
    final PsiElement e = PyUtil.turnInitIntoDir(file);
    myElement = e instanceof PsiFileSystemItem ? (PsiFileSystemItem)e : file;
  }

  @NotNull
  @Override
  public PsiElement[] getPrimaryElements() {
    return new PsiElement[] {myElement};
  }

  @NotNull
  @Override
  public AbstractFindUsagesDialog getFindUsagesDialog(boolean isSingleFile, boolean toShowInNewTab, boolean mustOpenInNewTab) {
    return new CommonFindUsagesDialog(myElement,
                                      getProject(),
                                      getFindUsagesOptions(),
                                      toShowInNewTab,
                                      mustOpenInNewTab,
                                      isSingleFile,
                                      this) {
      @Override
      public void configureLabelComponent(@NotNull final SimpleColoredComponent coloredComponent) {
        coloredComponent.append(myElement instanceof PsiDirectory ? "Package " : "Module ");
        coloredComponent.append(myElement.getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
      }
    };
  }

  @Override
  public Collection<PsiReference> findReferencesToHighlight(@NotNull PsiElement target, @NotNull SearchScope searchScope) {
    if (target instanceof PyImportedModule) {
      target = ((PyImportedModule) target).resolve();
    }
    if (target instanceof PyFile && PyNames.INIT_DOT_PY.equals(((PyFile)target).getName())) {
      List<PsiReference> result = new ArrayList<PsiReference>();
      result.addAll(super.findReferencesToHighlight(target, searchScope));
      result.addAll(ReferencesSearch.search(PyUtil.turnInitIntoDir(target), searchScope, false).findAll());
      return result;
    }
    return super.findReferencesToHighlight(target, searchScope);
  }
}
