package com.sylvanaar.idea.Lua.lang.psi.resolve;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.search.GlobalSearchScope;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.impl.statements.LuaFunctionDefinitionStatementImpl;
import com.sylvanaar.idea.Lua.lang.psi.resolve.processors.ResolveProcessor;
import com.sylvanaar.idea.Lua.lang.psi.resolve.processors.SymbolResolveProcessor;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaLocalDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.psi.stubs.index.LuaGlobalDeclarationIndex;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaLocal;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaAssignmentUtil;
import com.sylvanaar.idea.Lua.options.LuaApplicationSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class LuaResolver implements ResolveCache.PolyVariantResolver<LuaReferenceElement> {
    public static final Logger log = Logger.getInstance("Lua.LuaResolver");

    boolean ignoreAliasing = false;
    public void setIgnoreAliasing(boolean b) { ignoreAliasing=b; }
    public boolean getIgnoreAliasing() { return ignoreAliasing; }

    static Collection<LuaDeclarationExpression> filteredGlobalsCache = null;

    @Nullable
    public LuaResolveResult[] resolve(LuaReferenceElement reference, boolean incompleteCode) {
        if (reference.getText() == null) return LuaResolveResult.EMPTY_ARRAY;
        final LuaResolveResult[] results = _resolve(reference, reference.getManager(), incompleteCode, ignoreAliasing);
        if (results.length == 1) {
            final LuaSymbol element = (LuaSymbol) results[0].getElement();
            final LuaSymbol referenceElement = (LuaSymbol) reference.getElement();

            LuaAssignmentUtil.transferSingleType(element, referenceElement, element.getLuaType(), referenceElement.getLuaType());
        }
        return results;
    }

    private static LuaResolveResult[] _resolve(LuaReferenceElement ref,
                                               PsiManager manager, boolean incompleteCode, boolean ignoreAliasing) {

        if (ref.getName() == null) {
            return LuaResolveResult.EMPTY_ARRAY;
        }
        
        ResolveProcessor processor = new SymbolResolveProcessor(ref, incompleteCode);

        ResolveUtil.treeWalkUp(ref, processor);

        if (/*processor.hasCandidates() || */ref.getElement() instanceof LuaLocal) {
            if (!processor.hasCandidates())
                return LuaResolveResult.EMPTY_ARRAY;

            return new LuaResolveResult[]{processor.getCandidates()[0]};
        }

        // Search the Project Files
        final Project project = manager.getProject();
        final GlobalSearchScope sc = ref.getResolveScope();
//        final LuaPsiFile currentFile = (LuaPsiFile) ref.getContainingFile();
        final String globalRefName = ref.getCanonicalText();

        LuaGlobalDeclarationIndex index = LuaGlobalDeclarationIndex.getInstance();
        Collection<LuaDeclarationExpression> names = index.get(globalRefName, project, sc);
        for (LuaDeclarationExpression name : names) {
//            log.debug(name + " --> ");
            name.processDeclarations(processor, ResolveState.initial(), ref, ref);
        }

        if (processor.hasCandidates()) {
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

        String prefix = symbol.getText().substring(0, idx + 1);
        return prefix;
    }

    public static LuaReferenceElement resolveAlias(LuaReferenceElement ref, @NotNull PsiElement resolved) {

        if (!LuaApplicationSettings.getInstance().RESOLVE_ALIASED_IDENTIFIERS)
            return null;

        if (resolved instanceof LuaLocal && resolved.getContext().getContext() instanceof LuaLocalDefinitionStatement) {
            LuaLocalDefinitionStatement stat = (LuaLocalDefinitionStatement) resolved.getContext().getContext();

            LuaDeclarationExpression[] decls = stat.getDeclarations();
            LuaExpression[] exprs = stat.getExprs();

            if (exprs != null && exprs.length > 0) {
                LuaExpression aliasedExpression = null;
                for (int i = 0; i < decls.length; i++) {
                    if (decls[i] == resolved && exprs.length > i)
                        aliasedExpression = exprs[i];
                }

                if (aliasedExpression instanceof LuaReferenceElement) {
                    return (LuaReferenceElement) aliasedExpression;
                }
            }
        }

        return null;
    }
}