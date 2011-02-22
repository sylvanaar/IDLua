package com.sylvanaar.idea.Lua.lang.psi.resolve;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.impl.statements.LuaFunctionDefinitionStatementImpl;
import com.sylvanaar.idea.Lua.lang.psi.resolve.processors.ResolveProcessor;
import com.sylvanaar.idea.Lua.lang.psi.resolve.processors.SymbolResolveProcessor;
import com.sylvanaar.idea.Lua.lang.psi.stubs.index.LuaGlobalDeclarationIndex;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaLocal;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
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
        PsiElement element = ref.getElement();
        String prefix = "", postfix = "";
        if (element.getText().startsWith("self.") || element.getText().startsWith("self:")) {
            postfix = element.getText().substring(5);
            prefix = findSelfPrefix(element);
        }

        final String refName = prefix!=null?prefix+postfix:ref.getText();
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
        Collection<LuaDeclarationExpression> names = index.get(refName, project, sc);
        for(LuaDeclarationExpression name : names) {
//            System.out.println("Resolve: got <" + name + "> from index");
            name.processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
        }



//        ModuleManager mm = ModuleManager.getInstance(project);
//        ProjectRootManager prm = ProjectRootManager.getInstance(project);
//
//        for (final Module module : mm.getModules()) {
//            ModuleRootManager mrm = ModuleRootManager.getInstance(module);
//            Sdk sdk = mrm.getSdk();
//
//            if (sdk != null) {
//                VirtualFile[] vf = sdk.getRootProvider().getFiles(OrderRootType.CLASSES);
//
//                for (VirtualFile libraryFile : vf)
//                    LuaFileUtil.iterateRecursively(libraryFile, new ContentIterator() {
//                        @Override
//                        public boolean processFile(VirtualFile fileOrDir) {
//                            if (fileOrDir.getFileType() == LuaFileType.LUA_FILE_TYPE) {
//                                PsiFile f = PsiManagerEx.getInstance(project).findFile(fileOrDir);
//
//                                assert f instanceof LuaPsiFile;
//
//                                f.processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
//                            }
//                            return true;
//                        }
//                    });
//            }
//        }
//
//        String url = VfsUtil.pathToUrl(PathUtil.getJarPathForClass(LuaPsiFile.class));
//        VirtualFile sdkFile = VirtualFileManager.getInstance().findFileByUrl(url);
//        if (sdkFile != null) {
//            VirtualFile jarFile = JarFileSystem.getInstance().getJarRootForLocalFile(sdkFile);
//            if (jarFile != null) {
//                StdLibrary.getStdFile(project, jarFile).processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
//            } else if (sdkFile instanceof VirtualDirectoryImpl) {
//                StdLibrary.getStdFile(project, sdkFile).processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
//            }
//        }

        if (processor.hasCandidates()) {
          //  if (prefix != null) System.out.println("Resolved: " + ref.getText() + " to " + processor.getCandidates()[0].getElement());
            return processor.getCandidates();
        }

        return LuaResolveResult.EMPTY_ARRAY;
    }

    private static String findSelfPrefix(PsiElement element) {
        while (!(element instanceof LuaFunctionDefinitionStatementImpl) && element != null)
            element = element.getContext();

        // Must be inside a function
        if (element == null) return null;

        LuaFunctionDefinitionStatementImpl func = (LuaFunctionDefinitionStatementImpl) element;

        LuaSymbol symbol = func.getIdentifier();

        int colonIdx = symbol.getText().lastIndexOf(':');
        int dotIdx = symbol.getText().lastIndexOf('.');
        if (colonIdx < 0 && dotIdx < 0) return null;

        int idx = Math.max(colonIdx, dotIdx);

        String prefix = symbol.getText().substring(0, idx+1);
        return prefix;
    }


}