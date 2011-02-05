package com.sylvanaar.idea.Lua.lang.psi.impl.symbols;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.FileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.impl.VirtualDirectoryImpl;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PathUtil;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.lang.psi.LuaNamedElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaPsiElementImpl;
import com.sylvanaar.idea.Lua.lang.psi.resolve.LuaResolveResult;
import com.sylvanaar.idea.Lua.lang.psi.resolve.ResolveUtil;
import com.sylvanaar.idea.Lua.lang.psi.resolve.completion.CompletionProcessor;
import com.sylvanaar.idea.Lua.lang.psi.resolve.processors.ResolveProcessor;
import com.sylvanaar.idea.Lua.lang.psi.resolve.processors.SymbolResolveProcessor;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * TODO: implement all reference stuff...
 */
public abstract class LuaReferenceElementImpl extends LuaPsiElementImpl implements LuaReferenceElement {
    public LuaReferenceElementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitReferenceElement(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitReferenceElement(this);
        } else {
            visitor.visitElement(this);
        }
    }

    public PsiType getType() {
        return PsiType.VOID;
    }

    public PsiElement getElement() {
        return this;
    }

    public PsiReference getReference() {
        return this;
    }

    public TextRange getRangeInElement() {
        final PsiElement nameElement = getElement();
        return new TextRange(getTextOffset() - nameElement.getTextOffset(), nameElement.getTextLength());
    }

    @Nullable
    public PsiElement resolve() {
      ResolveResult[] results = getManager().getResolveCache().resolveWithCaching(this, RESOLVER, true, false);
      return results.length == 1 ? results[0].getElement() : null;
    }

    @NotNull
    public ResolveResult[] multiResolve(final boolean incompleteCode) {
        return getManager().getResolveCache().resolveWithCaching(this, RESOLVER, true, incompleteCode);
    }

    private static class OurResolver implements ResolveCache.PolyVariantResolver<LuaReferenceElement> {

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
            LuaResolveResult[] candidates = processor.getCandidates();

            if (candidates.length > 0)
                return candidates;


            // Search the Project Files


            final Project project = manager.getProject();
            final PsiScopeProcessor scopeProcessor = processor;
            final PsiElement filePlace = ref;
            final GlobalSearchScope sc = filePlace.getResolveScope();
            FileIndex fi = ProjectRootManager.getInstance(project).getFileIndex();


            fi.iterateContent(new ContentIterator() {
                @Override
                public boolean processFile(VirtualFile fileOrDir) {
                    try {

                        if (fileOrDir.getFileType() == LuaFileType.LUA_FILE_TYPE) {
                            PsiFile f = PsiManagerEx.getInstance(project).findFile(fileOrDir);

//                            if (!sc.contains(fileOrDir))
//                                return true;

                            assert f instanceof LuaPsiFile;

                            for(LuaFunctionDefinitionStatement func : ((LuaPsiFile) f).getFunctionDefs())
                                func.processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);

                            for(LuaSymbol symbol : ((LuaPsiFile)f).getSymbolDefs())
                                symbol.processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
                        }
                    } catch (Throwable unused) {
                        unused.printStackTrace();
                    }
                    return true;  // keep going

                }
            });

            candidates = processor.getCandidates();

            if (candidates.length > 0)
                return candidates;



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

            candidates = processor.getCandidates();

            if (candidates.length > 0)
                return candidates;


            return LuaResolveResult.EMPTY_ARRAY;
        }
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
    }


    private static final OurResolver RESOLVER = new OurResolver();


    @NotNull
    public String getCanonicalText() {
        return getText();
    }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        ((PsiNamedElement)getElement()).setName(newElementName);
        return this;
    }

    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        replace(element);
        return this;
    }

    public boolean isReferenceTo(PsiElement element) {
        if (element instanceof LuaNamedElement) {
            if (Comparing.equal(((PsiNamedElement) getElement()).getName(), ((PsiNamedElement) element).getName()))
                return resolve() == element;
        }
        return false;
    }

    @NotNull
    public Object[] getVariants() {
        CompletionProcessor variantsProcessor = new CompletionProcessor(this);
        ResolveUtil.treeWalkUp(this, variantsProcessor);


        final Project project = getProject();
        final PsiScopeProcessor scopeProcessor = variantsProcessor;
        final PsiElement filePlace = this;

        FileIndex fi = ProjectRootManager.getInstance(project).getFileIndex();


        fi.iterateContent(new ContentIterator() {
            @Override
            public boolean processFile(VirtualFile fileOrDir) {
                try {
                if (fileOrDir.getFileType() == LuaFileType.LUA_FILE_TYPE) {
                    PsiFile f = PsiManagerEx.getInstance(project).findFile(fileOrDir);

                    assert f instanceof LuaPsiFile;

                    f.processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);

//                    for(LuaFunctionDefinitionStatement func : ((LuaPsiFile) f).getFunctionDefs())
//                        func.processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
//
//                    for(LuaSymbol symbol : ((LuaPsiFile)f).getSymbolDefs())
//                        symbol.processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
                }
                } catch (Throwable unused) { unused.printStackTrace(); }
                return true;  // keep going

            }
        });

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

        return variantsProcessor.getResultElements();
    }

    public boolean isSoft() {
        return false;
    }

    public boolean isAssignedTo() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PsiElement replaceWithExpression(LuaExpression newCall, boolean b) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName() {
        return getText();
    }
}