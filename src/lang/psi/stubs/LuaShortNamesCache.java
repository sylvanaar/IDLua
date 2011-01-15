///*
// * Copyright 2011 Jon S Akhtar (Sylvanaar)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.sylvanaar.idea.Lua.lang.psi.stubs;
//
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.util.Condition;
//import com.intellij.psi.PsiClass;
//import com.intellij.psi.PsiField;
//import com.intellij.psi.PsiFile;
//import com.intellij.psi.PsiMethod;
//import com.intellij.psi.search.FilenameIndex;
//import com.intellij.psi.search.GlobalSearchScope;
//import com.intellij.psi.search.PsiShortNamesCache;
//import com.intellij.psi.stubs.StubIndex;
//import com.intellij.util.Function;
//import com.intellij.util.containers.ContainerUtil;
//import com.intellij.util.containers.HashSet;
//import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
//import com.sylvanaar.idea.Lua.lang.psi.stubs.index.LuaFullScriptNameIndex;
//import org.jetbrains.annotations.NonNls;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//import org.jetbrains.plugins.Lua.psi.stubs.index.LuaClassNameIndex;
//
//import java.util.ArrayList;
//import java.util.Collection;
//
///**
// * @author ilyas
// */
//public class LuaShortNamesCache extends PsiShortNamesCache {
//
//  Project myProject;
//
//  public LuaShortNamesCache(Project project) {
//    myProject = project;
//  }
//
//
//  public void runStartupActivity() {
//  }
//
//  @NotNull
//  public PsiFile[] getFilesByName(@NotNull String name) {
//    return new PsiFile[0];
//  }
//
//  @NotNull
//  public String[] getAllFileNames() {
//    return FilenameIndex.getAllFilenames(myProject);
//  }
//
//  private boolean areClassesCompiled() {
//    return true;
//  }
//
//  @NotNull
//  public PsiClass[] getClassesByName(@NotNull String name, @NotNull GlobalSearchScope scope) {
//    if (!areClassesCompiled()) return PsiClass.EMPTY_ARRAY;
//
//    Collection<PsiClass> allClasses = getAllScriptClasses(name, scope);
//    if (allClasses.isEmpty()) return PsiClass.EMPTY_ARRAY;
//    return allClasses.toArray(new PsiClass[allClasses.size()]);
//  }
//
//  private Collection<PsiClass> getAllScriptClasses(String name, GlobalSearchScope scope) {
//    if (!areClassesCompiled()) return new ArrayList<PsiClass>();
//
//    Collection<LuaPsiFile> files = StubIndex.getInstance().get(LuaClassNameIndex.KEY, name, myProject, scope);
//    files = ContainerUtil.findAll(files, new Condition<LuaPsiFile>() {
//      public boolean value(LuaPsiFile LuaPsiFile) {
//        return LuaPsiFile.isClassDefiningFile();
//      }
//    });
//    return ContainerUtil.map(files, new Function<LuaPsiFile, PsiClass>() {
//      public PsiClass fun(LuaPsiFile LuaPsiFile) {
//        assert LuaPsiFile.isClassDefiningFile();
//        return LuaPsiFile.getDefinedClass();
//      }
//    });
//  }
//
//  private Collection<PsiClass> getScriptClassesByFQName(final String name, final GlobalSearchScope scope) {
//    Collection<LuaPsiFile> scripts = StubIndex.getInstance().get(LuaFullScriptNameIndex.KEY, name.hashCode(), myProject, scope);
//
//    scripts = ContainerUtil.findAll(scripts, new Condition<LuaPsiFile>() {
//      public boolean value(final LuaPsiFile LuaPsiFile) {
//        final PsiClass clazz = LuaPsiFile.getDefinedClass();
//        return LuaPsiFile.isClassDefiningFile() && clazz != null && name.equals(clazz.getQualifiedName());
//      }
//    });
//    return ContainerUtil.map(scripts, new Function<LuaPsiFile, PsiClass>() {
//      public PsiClass fun(final LuaPsiFile LuaPsiFile) {
//        return LuaPsiFile.getDefinedClass();
//      }
//    });
//  }
//
///*
//  public Collection<ClNs> getAllNameSpaces(final GlobalSearchScope scope) {
//    final Collection<String> nsNames = StubIndex.getInstance().getAllKeys(LuaNsNameIndex.KEY);
//
//    StubIndex.getInstance().get(LuaNsNameIndex.KEY, name.hashCode(), myProject, scope)
//  }
//*/
//
//
//  @NotNull
//  public String[] getAllClassNames() {
//    if (!areClassesCompiled()) return new String[0];
//
//    final Collection<String> classNames = StubIndex.getInstance().getAllKeys(LuaClassNameIndex.KEY, myProject);
//    return classNames.toArray(new String[classNames.size()]);
//  }
//
//  public void getAllClassNames(@NotNull HashSet<String> dest) {
//    if (!areClassesCompiled()) return;
//
//    final Collection<String> classNames = StubIndex.getInstance().getAllKeys(LuaClassNameIndex.KEY, myProject);
//    dest.addAll(classNames);
//  }
//
//  @Nullable
//  public PsiClass getClassByFQName(@NotNull @NonNls String name, @NotNull GlobalSearchScope scope) {
//    if (!areClassesCompiled()) return null;
//
//    final Collection<PsiClass> scriptClasses = getScriptClassesByFQName(name, scope);
//    for (PsiClass clazz : scriptClasses) {
//      if (name.equals(clazz.getQualifiedName())) return clazz;
//    }
//    return null;
//  }
//
//  @NotNull
//  public PsiClass[] getClassesByFQName(@NotNull @NonNls String fqn, @NotNull GlobalSearchScope scope) {
//    if (!areClassesCompiled()) return PsiClass.EMPTY_ARRAY;
//
//    final Collection<PsiClass> result = getScriptClassesByFQName(fqn, scope);
//    ArrayList<PsiClass> filtered = new ArrayList<PsiClass>();
//    for (PsiClass clazz : result) {
//      if (fqn.equals(clazz.getQualifiedName())) {
//        filtered.add(clazz);
//      }
//    }
//    return filtered.isEmpty() ? PsiClass.EMPTY_ARRAY : filtered.toArray(new PsiClass[filtered.size()]);
//  }
//
//
//  @NotNull
//  public PsiMethod[] getMethodsByName(@NonNls String name, @NotNull GlobalSearchScope scope) {
//    return new PsiMethod[0];
//  }
//
//  @NotNull
//  public PsiMethod[] getMethodsByNameIfNotMoreThan(@NonNls String name, @NotNull GlobalSearchScope scope, int maxCount) {
//    return new PsiMethod[0];
//  }
//
//  @NotNull
//  public String[] getAllMethodNames() {
//    return new String[0];
//  }
//
//  public void getAllMethodNames(@NotNull HashSet<String> set) {
//  }
//
//  @NotNull
//  public PsiField[] getFieldsByName(@NotNull String name, @NotNull GlobalSearchScope scope) {
//    return new PsiField[0];
//  }
//
//  @NotNull
//  public String[] getAllFieldNames() {
//    return new String[0];
//  }
//
//  public void getAllFieldNames(@NotNull HashSet<String> set) {
//  }
//
//}
