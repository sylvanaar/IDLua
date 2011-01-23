package com.sylvanaar.idea.Lua.lang.psi.resolve;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scheme.psi.api.SchemePsiElement;
import static org.jetbrains.plugins.scheme.psi.impl.SchemePsiElementBase.isWrongElement;
import org.jetbrains.plugins.scheme.psi.impl.symbols.SchemeIdentifier;

import java.util.List;
import java.util.ListIterator;


public abstract class ResolveUtil
{
  public static PsiElement resolve(SchemeIdentifier place)
  {
    PsiElement element = place;
    ResolveResult result = ResolveResult.CONTINUE;
    while ((element != null) && !result.isDone())
    {
      if (element instanceof SchemePsiElement)
      {
        SchemePsiElement schemePsiElement = (SchemePsiElement) element;
        result = schemePsiElement.resolve(place);
      }

      element = element.getContext(); //getParent
    }

    return result.getResult();
  }

  public static int getQuotingLevel(PsiElement place)
  {
    int ret = 0;
    PsiElement element = place;
    while (element != null)
    {
      if (element instanceof SchemePsiElement)
      {
        SchemePsiElement schemeElement = (SchemePsiElement) element;
        ret += schemeElement.getQuotingLevel();
      }
      element = element.getContext();
    }
    return ret;
  }

  public static PsiElement[] mapToElements(SchemeResolveResult[] candidates)
  {
    PsiElement[] elements = new PsiElement[candidates.length];
    for (int i = 0; i < elements.length; i++)
    {
      elements[i] = candidates[i].getElement();
    }

    return elements;
  }

  public static PsiElement getNextNonLeafElement(@NotNull PsiElement element)
  {
    PsiElement next = element.getNextSibling();
    while ((next != null) && isWrongElement(next))
    {
      next = next.getNextSibling();
    }
    return next;
  }

  @NotNull
  public static ResolveResult resolveFrom(@NotNull SchemeIdentifier place, @NotNull List<SchemeIdentifier> matches)
  {
    ListIterator<SchemeIdentifier> iterator = matches.listIterator(matches.size());
    while (iterator.hasPrevious())
    {
      SchemeIdentifier identifier = iterator.previous();
      if (place.couldReference(identifier))
      {
        return ResolveResult.of(identifier);
      }
    }

    return ResolveResult.CONTINUE;
  }
}
