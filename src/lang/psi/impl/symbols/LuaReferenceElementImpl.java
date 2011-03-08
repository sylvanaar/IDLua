package com.sylvanaar.idea.Lua.lang.psi.impl.symbols;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaNamedElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.resolve.LuaResolveResult;
import com.sylvanaar.idea.Lua.lang.psi.resolve.LuaResolver;
import com.sylvanaar.idea.Lua.lang.psi.resolve.ResolveUtil;
import com.sylvanaar.idea.Lua.lang.psi.resolve.completion.CompletionProcessor;
import com.sylvanaar.idea.Lua.lang.psi.stubs.index.LuaGlobalDeclarationIndex;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import com.sylvanaar.idea.Lua.options.LuaApplicationSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;


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

     public PsiElement setName(@NotNull String s) {
        ((PsiNamedElement)getElement()).setName(s);

        resolve();

        return this;
     }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        ((PsiNamedElement)getElement()).setName(newElementName);
        resolve();
        return this;
    }

    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        findChildByClass(LuaIdentifier.class).replace(element);
        return this;
    }

    public boolean isReferenceTo(PsiElement element) {
        if (LuaApplicationSettings.getInstance().RESOLVE_ALIASED_IDENTIFIERS) {
            return resolve() == element;
        } else {
            if (element instanceof LuaNamedElement) {
                if (Comparing.equal(((PsiNamedElement) getElement()).getName(), ((PsiNamedElement) element).getName()))
                    return resolve() == element;
            }
        }
        return false;
    }

    @NotNull
    public Object[] getVariants() {
        CompletionProcessor variantsProcessor = new CompletionProcessor(this);
        ResolveUtil.treeWalkUp(this, variantsProcessor);

//        final Project project = getProject();
//        final PsiScopeProcessor scopeProcessor = variantsProcessor;
//        final PsiElement filePlace = this;

        LuaGlobalDeclarationIndex index = LuaGlobalDeclarationIndex.getInstance();
        Collection<String> names = index.getAllKeys(getProject());


//        ModuleManager mm = ModuleManager.getInstance(project);
//        ProjectRootManager prm = ProjectRootManager.getInstance(project);

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

        for (PsiElement e : variantsProcessor.getResultElements())
            names.add(e.getText());

        names.remove(this.getName());

        return names.toArray();
    }


//    @NotNull
//    @Override
//    public Object[] getVariants() {
//        return ArrayUtil.EMPTY_OBJECT_ARRAY;
//    }

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
    public String getName() {
        return getText();
    }

    public PsiNamedElement getReferenceNameElement() {
        return (PsiNamedElement) findChildByType(LuaTokenTypes.NAME);
    }

    @Override
    public PsiElement resolveWithoutCaching(boolean ingnoreAlias) {

        boolean save = RESOLVER.getIgnoreAliasing();
        RESOLVER.setIgnoreAliasing(ingnoreAlias);
        LuaResolveResult[] results = RESOLVER.resolve(this, false);
        RESOLVER.setIgnoreAliasing(save);

        if (results != null && results.length > 0)
            return results[0].getElement();

        return null;
    }
}