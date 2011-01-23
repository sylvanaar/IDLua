package com.sylvanaar.idea.Lua.lang.psi.impl.expressions;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiType;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaReferenceExpression;
import com.sylvanaar.idea.Lua.lang.psi.util.ResolveUtil;
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

    @Override
    public boolean isDeclaration() {
        LuaIdentifier id = findChildByClass(LuaIdentifier.class);


        return id != null && id.isDeclaration();
    }


    public PsiElement resolve() {
        final String referencedName = getReferencedName();
        if (referencedName == null) return null;

        if (getQualifier() != null) {
            return null; // TODO?
        }

        return ResolveUtil.treeWalkUp(new ResolveUtil.ResolveProcessor(referencedName), this, this, this);
    }

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

        final ResolveUtil.VariantsProcessor processor = new ResolveUtil.VariantsProcessor();
        ResolveUtil.treeWalkUp(processor, this, this, this);
        return processor.getResult();
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

    @Override
    public PsiElement setName(@NotNull String s) {        
        ((PsiNamedElement)getElement()).setName(s);

        resolve();
        
        return this;
    }


    @NotNull
    @Override
    public GlobalSearchScope getResolveScope() {
        return getElement().getResolveScope();
    }

    @NotNull
  public ResolveResult[] multiResolve(boolean incomplete) {
    return new ResolveResult[0]; //getManager().getResolveCache().resolveWithCaching(this, RESOLVER, true, incomplete);
  }
}