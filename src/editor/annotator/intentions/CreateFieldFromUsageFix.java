///*
// * Copyright 2010 Jon S Akhtar (Sylvanaar)
// *
// *   Licensed under the Apache License, Version 2.0 (the "License");
// *   you may not use this file except in compliance with the License.
// *   You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// *   Unless required by applicable law or agreed to in writing, software
// *   distributed under the License is distributed on an "AS IS" BASIS,
// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *   See the License for the specific language governing permissions and
// *   limitations under the License.
// */
//package com.sylvanaar.idea.Lua.editor.annotator.intentions;
//
//import com.intellij.openapi.editor.Editor;
//import com.intellij.openapi.project.Project;
//import com.intellij.psi.PsiFile;
//import com.intellij.psi.PsiModifier;
//import com.intellij.util.ArrayUtil;
//import com.sylvanaar.idea.Lua.LuaBundle;
//import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaReferenceExpression;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//
///**
// * @author ven
// */
//public class CreateFieldFromUsageFix extends CreateFieldFix {
//  private final LuaReferenceExpression myRefExpression;
//
//  public CreateFieldFromUsageFix(LuaReferenceExpression refExpression, GrMemberOwner targetClass) {
//    super(targetClass);
//    myRefExpression = refExpression;
//  }
//
//  @Nullable
//  protected String getFieldName() {
//    return myRefExpression.getReferenceName();
//  }
//
//  @NotNull
//  public String getFamilyName() {
//    return LuaBundle.message("create.from.usage.family.name");
//  }
//
//  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
//    return super.isAvailable(project, editor, file) && myRefExpression.isValid();
//  }
//
//  protected String[] generateModifiers() {
//    if (myRefExpression != null && PsiUtil.isInStaticContext(myRefExpression, getTargetClass())) {
//      return new String[]{PsiModifier.STATIC};
//    }
//    return ArrayUtil.EMPTY_STRING_ARRAY;
//  }
//
//  protected TypeConstraint[] calculateTypeConstrains() {
//    return GroovyExpectedTypesProvider.calculateTypeConstraints(myRefExpression);
//
//  }
//}
