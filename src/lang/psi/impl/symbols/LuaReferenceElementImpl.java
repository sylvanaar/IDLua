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
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PathUtil;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.lang.psi.LuaNamedElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.resolve.LuaResolver;
import com.sylvanaar.idea.Lua.lang.psi.resolve.ResolveUtil;
import com.sylvanaar.idea.Lua.lang.psi.resolve.completion.CompletionProcessor;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * TODO: implement all reference stuff...
 */
public abstract class LuaReferenceElementImpl extends LuaSymbolImpl implements LuaReferenceElement {
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


    public PsiElement getResolvedElement() {
        return resolve();
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

    private static final LuaResolver RESOLVER = new LuaResolver();

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

                    for(LuaFunctionDefinitionStatement func : ((LuaPsiFile) f).getFunctionDefs())
                        func.processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);

                    for(LuaSymbol symbol : ((LuaPsiFile)f).getSymbolDefs())
                        symbol.processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
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
            LuaResolver.getStdFile(project, jarFile).processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
          }
          else if (sdkFile instanceof VirtualDirectoryImpl)
          {
            LuaResolver.getStdFile(project, sdkFile).processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
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