package com.sylvanaar.idea.Lua.lang.psi.resolve;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.FileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.impl.VirtualDirectoryImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.PathUtil;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.resolve.processors.ResolveProcessor;
import com.sylvanaar.idea.Lua.lang.psi.resolve.processors.SymbolResolveProcessor;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaLocal;
import org.jetbrains.annotations.Nullable;

public class LuaResolver implements ResolveCache.PolyVariantResolver<LuaReferenceElement> {

        @Nullable
        public LuaResolveResult[] resolve(LuaReferenceElement reference, boolean incompleteCode) {
            if (reference.getText() == null) return LuaResolveResult.EMPTY_ARRAY;
            final LuaResolveResult[] results = _resolve(reference, reference.getManager(), incompleteCode);
            return results;
        }

        private static LuaResolveResult[] _resolve(LuaReferenceElement ref,
                                                   PsiManager manager, boolean incompleteCode) {
            final String refName = ref.getText();
            if (refName == null) {
                return LuaResolveResult.EMPTY_ARRAY;
            }
            ResolveProcessor processor = new SymbolResolveProcessor(refName, ref, incompleteCode);

            if (ref.getElement() instanceof LuaLocal) {
                ResolveUtil.treeWalkUp(ref, processor);
                return processor.getCandidates();
            }

            // Search the Project Files
            final Project project = manager.getProject();
            final PsiScopeProcessor scopeProcessor = processor;
            final PsiElement filePlace = ref;
            final GlobalSearchScope sc = filePlace.getResolveScope();
            final LuaPsiFile currentFile = (LuaPsiFile) filePlace.getContainingFile();

            FileIndex fi = ProjectRootManager.getInstance(project).getFileIndex();

            fi.iterateContent(new ContentIterator() {
                @Override
                public boolean processFile(VirtualFile fileOrDir) {
                    try {

                        if (fileOrDir.getFileType() == LuaFileType.LUA_FILE_TYPE) {
                            PsiFile f = PsiManagerEx.getInstance(project).findFile(fileOrDir);

                            if (!sc.contains(fileOrDir)) {
                                return true;
                            }

                            assert f instanceof LuaPsiFile;

                            f.processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);

//                            for(LuaFunctionDefinitionStatement func : ((LuaPsiFile) f).getFunctionDefs())
//                                if (!func.processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace))
//                                    return false;

//                            for(LuaSymbol symbol : ((LuaPsiFile)f).getSymbolDefs())
//                                symbol.processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
//                                if (symbol instanceof LuaGlobal && !symbol.processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace))
//                                    return false;
                        }
                    } catch (Throwable unused) {
                        unused.printStackTrace();
                    }
                    return true;  // keep going

                }
            });


            // Search Our 'Library Includes'
//            if (!ref.getResolveScope().isSearchInLibraries())
//                return candidates;

            String url = VfsUtil.pathToUrl(PathUtil.getJarPathForClass(LuaPsiFile.class));
            VirtualFile sdkFile = VirtualFileManager.getInstance().findFileByUrl(url);
            if (sdkFile != null)
            {
              VirtualFile jarFile = JarFileSystem.getInstance().getJarRootForLocalFile(sdkFile);
              if (jarFile != null)
              {
                getStdFile(project, jarFile).processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
              }
              else if (sdkFile instanceof VirtualDirectoryImpl)
              {
                getStdFile(project, sdkFile).processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
              }
            }

            if (processor.hasCandidates())
                return processor.getCandidates();


            return LuaResolveResult.EMPTY_ARRAY;
        }



    public static LuaPsiFile getStdFile(Project project, VirtualFile virtualFile)
    {
      VirtualFile r5rsFile = virtualFile.findFileByRelativePath("stdfuncs.lua");
      if (r5rsFile != null)
      {
        PsiFile file = PsiManager.getInstance(project).findFile(r5rsFile);
        if (file != null)
        {
          if (file instanceof LuaPsiFile)
          {
            return (LuaPsiFile) file;
          }
        }
      }
      return null;
    }    }