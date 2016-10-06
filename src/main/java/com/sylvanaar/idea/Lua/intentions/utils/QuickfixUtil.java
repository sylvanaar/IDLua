/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.sylvanaar.idea.Lua.intentions.utils;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.SuggestedNameInfo;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * User: Dmitry.Krasilschikov
 * Date: 20.12.2007
 */
public class QuickfixUtil {
//  @Nullable
//  public static PsiClass findTargetClass(LuaReferenceExpression refExpr) {
//    final PsiClass psiClass;
//    if (refExpr.isQualified()) {
//      LuaExpression qualifier = refExpr.getQualifierExpression();
//      PsiType type = qualifier.getType();
//      if (!(type instanceof PsiClassType)) return null;
//
//      psiClass = ((PsiClassType)type).resolve();
//    } else {
//      LuaPsiElement context = PsiTreeUtil.getParentOfType(refExpr, LuaTypeDefinition.class, LuaPsiFileBase.class);
//      if (context instanceof LuaTypeDefinition) {
//        return (PsiClass)context;
//      } else if (context instanceof LuaPsiFileBase) return ((LuaPsiFileBase)context).getScriptClass();
//      return null;
//    }
//    return psiClass;
//  }

//  public static boolean isStaticCall(LuaReferenceExpression refExpr) {
//
//    //todo: look more carefully
//    LuaExpression qualifierExpression = refExpr.getQualifierExpression();
//
//    if (!(qualifierExpression instanceof LuaReferenceExpression)) return false;
//
//    LuaReferenceExpression referenceExpression = (LuaReferenceExpression)qualifierExpression;
//    LuaPsiElement resolvedElement = ResolveUtil.resolveProperty(referenceExpression, referenceExpression.getName());
//
//    if (resolvedElement == null) return false;
//    if (resolvedElement instanceof PsiClass) return true;
//
//    return false;
//  }


  public static boolean ensureFileWritable(Project project, PsiFile file) {
    final VirtualFile virtualFile = file.getVirtualFile();
    final ReadonlyStatusHandler readonlyStatusHandler = ReadonlyStatusHandler.getInstance(project);
    final ReadonlyStatusHandler.OperationStatus operationStatus = readonlyStatusHandler.ensureFilesWritable(virtualFile);
    return !operationStatus.hasReadonlyFiles();
  }

  public static Editor positionCursor(@NotNull Project project, @NotNull PsiFile targetFile, @NotNull PsiElement element) {
    TextRange range = element.getTextRange();
    int textOffset = range.getStartOffset();

    VirtualFile vFile = targetFile.getVirtualFile();
    assert vFile != null;
    OpenFileDescriptor descriptor = new OpenFileDescriptor(project, vFile, textOffset);
    return FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
  }

//  public static String[] getMethodArgumentsNames(Project project, PsiType[] types) {
//    Set<String> uniqNames = new LinkedHashSet<String>();
//    Set<String> nonUniqNames = new THashSet<String>();
//    for (PsiType type : types) {
//      final SuggestedNameInfo nameInfo =
//        JavaCodeStyleManager.getInstance(project).suggestVariableName(VariableKind.PARAMETER, null, null, type);
//
//      final String name = nameInfo.names[0];
//      if (uniqNames.contains(name)) {
//        int i = 2;
//        while (uniqNames.contains(name + i)) i++;
//        uniqNames.add(name + i);
//        nonUniqNames.add(name);
//      } else {
//        uniqNames.add(name);
//      }
//    }
//
//    final String[] result = new String[uniqNames.size()];
//    int i = 0;
//    for (String name : uniqNames) {
//      result[i] = nonUniqNames.contains(name) ? name + 1 : name;
//      i++;
//    }
//    return result;
//  }

//  public static List<MyPair> swapArgumentsAndTypes(String[] names, PsiType[] types) {
//    List<MyPair> result = new ArrayList<MyPair>();
//
//    if (names.length != types.length) return Collections.emptyList();
//
//    for (int i = 0; i < names.length; i++) {
//      String name = names[i];
//      final PsiType type = types[i];
//
//      result.add(new MyPair(name, type.getCanonicalText()));
//    }
//
//    return result;
//  }
//
//  public static boolean isCall(LuaReferenceExpression referenceExpression) {
//    return referenceExpression.getParent() instanceof LuaFunctionCallStatement;
//  }
//
//  public static String[] getArgumentsTypes(List<MyPair> listOfPairs) {
//    final List<String> result = new ArrayList<String>();
//
//    if (listOfPairs == null) return ArrayUtil.EMPTY_STRING_ARRAY;
//    for (MyPair listOfPair : listOfPairs) {
//      String type = PsiTypesUtil.unboxIfPossible(listOfPair.second);
//      result.add(type);
//    }
//
//    return ArrayUtil.toStringArray(result);
//  }
//
//  public static String[] getArgumentsNames(List<MyPair> listOfPairs) {
//    final ArrayList<String> result = new ArrayList<String>();
//    for (MyPair listOfPair : listOfPairs) {
//      String name = listOfPair.first;
//      result.add(name);
//    }
//
//    return ArrayUtil.toStringArray(result);
//  }

  public static String shortenType(String typeText) {
    if (typeText == null) return "";
    final int i = typeText.lastIndexOf(".");
    if (i != -1) {
      return typeText.substring(i + 1);
    }
    return typeText;
  }

  public static Module getModuleByPsiFile(PsiFile containingFile) {
    VirtualFile file;
    if (containingFile != null) {
      file = containingFile.getVirtualFile();
      if (file == null) return null;
    } else {
      return null;
    }

    return ProjectRootManager.getInstance(containingFile.getProject()).getFileIndex().getModuleForFile(file);
  }


//  public static DynamicElementSettings createSettings(LuaReferenceExpression referenceExpression) {
//    DynamicElementSettings settings = new DynamicElementSettings();
//    final PsiClass containingClass = findTargetClass(referenceExpression);
//
//    assert containingClass != null;
//    String className = containingClass.getQualifiedName();
//    className = className == null ? containingClass.getContainingFile().getName() : className;
//
//    if (isStaticCall(referenceExpression)) {
//      settings.setStatic(true);
//    }
//
//    settings.setContainingClassName(className);
//    settings.setName(referenceExpression.getName());
//
//    if (isCall(referenceExpression)) {
//      List<PsiType> unboxedTypes = new ArrayList<PsiType>();
//      for (PsiType type : PsiUtil.getArgumentTypes(referenceExpression, false)) {
//        unboxedTypes.add(TypesUtil.unboxPrimitiveTypeWraperAndEraseGenerics(type));
//      }
//      final PsiType[] types = unboxedTypes.toArray(new PsiType[unboxedTypes.size()]);
//      final String[] names = getMethodArgumentsNames(referenceExpression.getProject(), types);
//      final List<MyPair> pairs = swapArgumentsAndTypes(names, types);
//
//      settings.setMethod(true);
//      settings.setPairs(pairs);
//    } else {
//      settings.setMethod(false);
//    }
//    return settings;
//  }
//
//  public static DynamicElementSettings createSettings(LuaArgumentLabel label, PsiClass targetClass) {
//    DynamicElementSettings settings = new DynamicElementSettings();
//
//    assert targetClass != null;
//    String className = targetClass.getQualifiedName();
//    className = className == null ? targetClass.getContainingFile().getName() : className;
//
//    settings.setContainingClassName(className);
//    settings.setName(label.getName());
//
//    return settings;
//  }
}