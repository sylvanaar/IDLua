package com.sylvanaar.idea.Lua.lang.psi.resolve;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
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
import com.sylvanaar.idea.Lua.lang.psi.stubs.index.LuaGlobalDeclarationIndex;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaGlobalDeclaration;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaLocal;
import com.sylvanaar.idea.Lua.sdk.StdLibrary;
import com.sylvanaar.idea.Lua.util.LuaFileUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

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
        ResolveUtil.treeWalkUp(ref, processor);

        if (/*processor.hasCandidates() || */ref.getElement() instanceof LuaLocal) {
            if (!processor.hasCandidates())
                return LuaResolveResult.EMPTY_ARRAY;

            final LuaResolveResult[] r = {processor.getCandidates()[0]};

            return r;
        }

        // Search the Project Files
        final Project project = manager.getProject();
        final PsiScopeProcessor scopeProcessor = processor;
        final PsiElement filePlace = ref;
        final GlobalSearchScope sc = filePlace.getResolveScope();
        final LuaPsiFile currentFile = (LuaPsiFile) filePlace.getContainingFile();

        LuaGlobalDeclarationIndex index = LuaGlobalDeclarationIndex.getInstance();
//        System.out.println("Resolve: getting indexed values for <" + refName + "> total keys: " + index.getAllKeys(project).size());
        Collection<LuaGlobalDeclaration> names = index.get(refName, project, sc);
        for(LuaGlobalDeclaration name : names) {
//            System.out.println("Resolve: got <" + name + "> from index");
            name.processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
        }

        ModuleManager mm = ModuleManager.getInstance(project);
        ProjectRootManager prm = ProjectRootManager.getInstance(project);

        for (final Module module : mm.getModules()) {
            ModuleRootManager mrm = ModuleRootManager.getInstance(module);
            Sdk sdk = mrm.getSdk();
           
            if (sdk != null) {
                VirtualFile[] vf = sdk.getRootProvider().getFiles(OrderRootType.CLASSES);

                for (VirtualFile libraryFile : vf)
                    LuaFileUtil.iterateRecursively(libraryFile, new ContentIterator() {
                        @Override
                        public boolean processFile(VirtualFile fileOrDir) {
                            if (fileOrDir.getFileType() == LuaFileType.LUA_FILE_TYPE) {
                                PsiFile f = PsiManagerEx.getInstance(project).findFile(fileOrDir);

                                assert f instanceof LuaPsiFile;

                                f.processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
                            }
                            return true;
                        }
                    });
            }
        }
              
        String url = VfsUtil.pathToUrl(PathUtil.getJarPathForClass(LuaPsiFile.class));
        VirtualFile sdkFile = VirtualFileManager.getInstance().findFileByUrl(url);
        if (sdkFile != null) {
            VirtualFile jarFile = JarFileSystem.getInstance().getJarRootForLocalFile(sdkFile);
            if (jarFile != null) {
                StdLibrary.getStdFile(project, jarFile).processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
            } else if (sdkFile instanceof VirtualDirectoryImpl) {
                StdLibrary.getStdFile(project, sdkFile).processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
            }
        }

        if (processor.hasCandidates())
            return processor.getCandidates();


        return LuaResolveResult.EMPTY_ARRAY;
    }


}