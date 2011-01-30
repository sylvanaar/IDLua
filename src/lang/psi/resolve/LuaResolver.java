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
//package com.sylvanaar.idea.Lua.lang.psi.resolve;
//
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.roots.ContentIterator;
//import com.intellij.openapi.roots.FileIndex;
//import com.intellij.openapi.roots.ProjectRootManager;
//import com.intellij.openapi.vfs.JarFileSystem;
//import com.intellij.openapi.vfs.VfsUtil;
//import com.intellij.openapi.vfs.VirtualFile;
//import com.intellij.openapi.vfs.VirtualFileManager;
//import com.intellij.openapi.vfs.newvfs.impl.VirtualDirectoryImpl;
//import com.intellij.psi.PsiElement;
//import com.intellij.psi.PsiFile;
//import com.intellij.psi.PsiManager;
//import com.intellij.psi.ResolveState;
//import com.intellij.psi.impl.PsiManagerEx;
//import com.intellij.psi.impl.source.resolve.ResolveCache;
//import com.intellij.psi.scope.PsiScopeProcessor;
//import com.intellij.psi.search.GlobalSearchScope;
//import com.intellij.util.PathUtil;
//import com.sylvanaar.idea.Lua.LuaFileType;
//import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
//import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaReferenceExpression;
//import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.LuaReferenceElementImpl;
//import com.sylvanaar.idea.Lua.lang.psi.resolve.processors.ResolveProcessor;
//import com.sylvanaar.idea.Lua.lang.psi.resolve.processors.SymbolResolveProcessor;
//import org.jetbrains.annotations.Nullable;
//
//    public class LuaResolver implements ResolveCache.PolyVariantResolver<LuaReferenceExpression> {
//
//        @Nullable
//        public static LuaResolveResult[] resolve(LuaReferenceExpression reference, boolean incompleteCode) {
//            if (reference.getNameElement() == null) return LuaResolveResult.EMPTY_ARRAY;
//            return _resolve(reference, reference.getManager(), incompleteCode);
//        }
//
//        private static LuaResolveResult[] _resolve(LuaReferenceExpression ref,
//                                                   PsiManager manager, boolean incompleteCode) {
//            final String refName = ref.getNameElement().getText();
//            if (refName == null) {
//                return LuaResolveResult.EMPTY_ARRAY;
//            }
//
//
//            ResolveProcessor processor = new SymbolResolveProcessor(refName, ref, incompleteCode);
//            ResolveUtil.treeWalkUp(ref, processor);
//            LuaResolveResult[] candidates = processor.getCandidates();
//
//            if (candidates.length > 0)
//                return candidates;
//
//
//            // Search the Project Files
//
//
//            final Project project = manager.getProject();
//            final PsiScopeProcessor scopeProcessor = processor;
//            final PsiElement filePlace = ref;
//            final GlobalSearchScope sc = filePlace.getResolveScope();
//            FileIndex fi = ProjectRootManager.getInstance(project).getFileIndex();
//
//
//            fi.iterateContent(new ContentIterator() {
//                @Override
//                public boolean processFile(VirtualFile fileOrDir) {
//                    try {
//                        if (fileOrDir.getFileType() == LuaFileType.LUA_FILE_TYPE) {
//                            PsiFile f = PsiManagerEx.getInstance(project).findFile(fileOrDir);
//
//                            if (!sc.contains(fileOrDir))
//                                return true;
//
//                            assert f instanceof LuaPsiFile;
//
//                            f.processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
//                        }
//                    } catch (Throwable unused) {
//                        unused.printStackTrace();
//                    }
//                    return true;  // keep going
//
//                }
//            });
//
//            candidates = processor.getCandidates();
//
//            if (candidates.length > 0)
//                return candidates;
//
//
//            // Search Our 'Library Includes'
//            if (!ref.getResolveScope().isSearchInLibraries())
//                return candidates;
//
//            String url = VfsUtil.pathToUrl(PathUtil.getJarPathForClass(LuaPsiFile.class));
//            VirtualFile sdkFile = VirtualFileManager.getInstance().findFileByUrl(url);
//            if (sdkFile != null) {
//                VirtualFile jarFile = JarFileSystem.getInstance().getJarRootForLocalFile(sdkFile);
//                if (jarFile != null) {
//                    LuaReferenceElementImpl.getStdFile(project, jarFile).processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
//                } else if (sdkFile instanceof VirtualDirectoryImpl) {
//                    LuaReferenceElementImpl.getStdFile(project, sdkFile).processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
//                }
//            }
//
//            candidates = processor.getCandidates();
//
//            if (candidates.length > 0)
//                return candidates;
//
//
//            return LuaResolveResult.EMPTY_ARRAY;
//        }
//
//}