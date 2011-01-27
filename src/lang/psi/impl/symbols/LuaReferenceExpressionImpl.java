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
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PathUtil;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiType;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaReferenceExpression;
import com.sylvanaar.idea.Lua.lang.psi.impl.expressions.LuaExpressionImpl;
import com.sylvanaar.idea.Lua.lang.psi.resolve.LuaResolveResult;
import com.sylvanaar.idea.Lua.lang.psi.resolve.ResolveUtil;
import com.sylvanaar.idea.Lua.lang.psi.resolve.completion.CompletionProcessor;
import com.sylvanaar.idea.Lua.lang.psi.resolve.processors.ResolveProcessor;
import com.sylvanaar.idea.Lua.lang.psi.resolve.processors.SymbolResolveProcessor;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * TODO: implement all reference stuff...
 */
public class LuaReferenceExpressionImpl extends LuaExpressionImpl implements LuaReferenceExpression {
    public LuaReferenceExpressionImpl(ASTNode node) {
        super(node);
    }


    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitReferenceExpression(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitReferenceExpression(this);
        } else {
            visitor.visitElement(this);
        }
    }


    @Nullable
    public LuaExpression getQualifier() {
        final ASTNode[] nodes = getNode().getChildren(LuaElementTypes.EXPRESSION_SET);
        return (LuaExpression) (nodes.length == 1 ? nodes[0].getPsi() : null);
    }

    @Nullable
    public String getReferencedName() {
        final ASTNode nameElement = getNameElement();
        return nameElement != null ? nameElement.getText() : null;
    }

    public PsiElement getElement() {
        return findChildByClass(LuaIdentifier.class);
    }

    public PsiReference getReference() {
        return this;
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        return super.getReferences();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public TextRange getRangeInElement() {
        final ASTNode nameElement = getNameElement();
        final int startOffset = nameElement != null ? nameElement.getStartOffset() : getNode().getTextRange().getEndOffset();
        return new TextRange(startOffset - getNode().getStartOffset(), getTextLength());
    }

    public ASTNode getNameElement() {
        PsiElement e = findChildByClass(LuaIdentifier.class);

        if (e != null)
            return e.getNode();

        return null;
    }

//    @Override
//    public boolean isDeclaration() {
//        LuaIdentifier id = findChildByClass(LuaIdentifier.class);
//
//
//        return id != null && id.isDeclaration();
//    }

    @Nullable
    public PsiElement resolve() {
      ResolveResult[] results = getManager().getResolveCache().resolveWithCaching(this, RESOLVER, true, false);
      return results.length == 1 ? results[0].getElement() : null;
    }

    @NotNull
    public ResolveResult[] multiResolve(final boolean incompleteCode) {
        return getManager().getResolveCache().resolveWithCaching(this, RESOLVER, true, incompleteCode);
    }

//    @Override
//    public boolean isSameKind(LuaSymbol symbol) {
//        return symbol.isSameKind((LuaSymbol) getElement());
//    }


    private static class OurResolver implements ResolveCache.PolyVariantResolver<LuaReferenceExpression> {

        @Nullable
        public LuaResolveResult[] resolve(LuaReferenceExpression reference, boolean incompleteCode) {
            if (reference.getNameElement() == null) return LuaResolveResult.EMPTY_ARRAY;
            final LuaResolveResult[] results = _resolve(reference, reference.getManager(), incompleteCode);
            return results;
        }

        private static LuaResolveResult[] _resolve(LuaReferenceExpression ref,
                                                   PsiManager manager, boolean incompleteCode) {
            final String refName = ref.getNameElement().getText();
            if (refName == null) {
                return LuaResolveResult.EMPTY_ARRAY;
            }


            ResolveProcessor processor = new SymbolResolveProcessor(refName, ref, incompleteCode);
            ResolveUtil.treeWalkUp(ref, processor);
            LuaResolveResult[] candidates = processor.getCandidates();

            if (candidates.length > 0)
                return candidates;

            final Project project = manager.getProject();
            final PsiScopeProcessor scopeProcessor = processor;
            final PsiElement filePlace = ref;

            FileIndex fi = ProjectRootManager.getInstance(project).getFileIndex();


            fi.iterateContent(new ContentIterator() {
                @Override
                public boolean processFile(VirtualFile fileOrDir) {
                    try {
                    if (fileOrDir.getFileType() == LuaFileType.LUA_FILE_TYPE) {
                        PsiFile f = PsiManagerEx.getInstance(project).findFile(fileOrDir);

                        assert f instanceof LuaPsiFile;

                        f.processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
                    }
                    } catch (Throwable unused) { unused.printStackTrace(); }
                    return true;  // keep going

                }
            });

            candidates = processor.getCandidates();

            if (candidates.length > 0)
                return candidates;

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
//        final ASTNode nameElement = LuaPsiElementFactoryImpl.getInstance(getProject()).createLocalNameIdentifier(newElementName).getNode();
//        getNode().replaceChild(getNameElement(), nameElement);

        ((PsiNamedElement)getElement()).setName(newElementName);
        return this;
    }

    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        findChildByClass(LuaIdentifier.class).replace(element);
        return this;
    }

    public boolean isReferenceTo(PsiElement element) {
        if (element instanceof PsiNamedElement) {
            if (Comparing.equal(getReferencedName(), ((PsiNamedElement) element).getName()))
                return resolve() == element;
        }
        return false;
    }

    @NotNull
    public Object[] getVariants() {
//        if (getQualifier() != null) {
//            return new Object[0]; // TODO?
//        }
        ResolveProcessor variantsProcessor = new CompletionProcessor(this);
        ResolveUtil.treeWalkUp(this, variantsProcessor);

//
//        final Project project = getProject();
//        final PsiScopeProcessor scopeProcessor = variantsProcessor;
//        final PsiElement filePlace = this;
//
//        FileIndex fi = ProjectRootManager.getInstance(project).getFileIndex();
//
//
//        fi.iterateContent(new ContentIterator() {
//            @Override
//            public boolean processFile(VirtualFile fileOrDir) {
//                try {
//                if (fileOrDir.getFileType() == LuaFileType.LUA_FILE_TYPE) {
//                    PsiFile f = PsiManagerEx.getInstance(project).findFile(fileOrDir);
//
//                    assert f instanceof LuaPsiFile;
//
//                    f.processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
//                }
//                } catch (Throwable unused) { unused.printStackTrace(); }
//                return true;  // keep going
//
//            }
//        });
//
//        String url = VfsUtil.pathToUrl(PathUtil.getJarPathForClass(LuaPsiFile.class));
//        VirtualFile sdkFile = VirtualFileManager.getInstance().findFileByUrl(url);
//        if (sdkFile != null)
//        {
//          VirtualFile jarFile = JarFileSystem.getInstance().getJarRootForLocalFile(sdkFile);
//          if (jarFile != null)
//          {
//            getStdFile(project, jarFile).processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
//          }
//          else if (sdkFile instanceof VirtualDirectoryImpl)
//          {
//            getStdFile(project, sdkFile).processDeclarations(scopeProcessor, ResolveState.initial(), filePlace, filePlace);
//          }
//        }

        return variantsProcessor.getCandidates();
    }

    public boolean isSoft() {
        return false;
    }


    public String toString() {
        return "LuaReferenceExpression ("+getReferencedName()+")";
    }

    @Override
    public String getName() {
        return getText();
    }


    @Override
    public PsiElement replaceWithExpression(LuaExpression newCall, boolean b) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LuaPsiType getType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

//    @Override
//    public PsiElement setName(@NotNull String s) {
//        ((PsiNamedElement)getElement()).setName(s);
//
//        resolve();
//
//        return this;
//    }


    @NotNull
    @Override
    public GlobalSearchScope getResolveScope() {
        return getElement().getResolveScope();
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        return getElement().getUseScope();
    }
}