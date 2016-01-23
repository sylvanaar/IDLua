package com.sylvanaar.idea.Lua.intentions.stdlib;

import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaFunctionCallExpression;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.LuaGlobalUsageImpl;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaGlobalIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaPrimitiveType;

public class StdTypeStaticInstanceMethod {
    private final String typeName;
    private final String methodName;
    private final StdLibraryType stdType;

    /**
     * Method
     *
     * @param typeName   Type name
     * @param methodName Method name
     */
    private StdTypeStaticInstanceMethod(String typeName, String methodName, StdLibraryType stdType) {
        this.stdType = stdType;
        this.typeName = typeName;
        this.methodName = methodName;
    }

    /**
     * Factory for type
     *
     * @param call Call element
     * @return new instance or null
     */
    public static StdTypeStaticInstanceMethod create(LuaFunctionCallExpression call) {
        StdTypeStaticInstanceMethod oResult = null;
        LuaReferenceElement methodRef, typeRef = call.getFunctionNameElement();
        if (typeRef != null) {
            PsiElement typeElement = typeRef.getFirstChild();
            if(typeElement instanceof LuaGlobalIdentifier) {
                //LuaGlobalIdentifier globalId = (LuaGlobalIdentifier) typeRef.getFirstChild();
                //LuaPrimitiveType luaPrimitiveType = (LuaPrimitiveType)globalId.getLuaType();
                return null;
            } else if(typeElement instanceof LuaCompoundIdentifier) {
                LuaCompoundIdentifier compoundId = (LuaCompoundIdentifier) typeRef.getFirstChild();
                String operator = compoundId.getOperator();
                if(operator == null || !operator.equals(".")) return null;
                typeRef = (LuaReferenceElement) compoundId.getLeftSymbol();
                methodRef = (LuaReferenceElement) compoundId.getLeftSymbol();
                String typeName = typeRef != null ? typeRef.getName() : null,
                       methodName = methodRef != null ? methodRef.getName() : null;
                if (typeName != null && methodName != null) {
                    StdLibraryType stdType = StdLibraryTypes.getInstance().getStdType(typeName);
                    if (stdType != null && stdType.hasStaticInstanceMethod(methodName)) {
                        oResult = new StdTypeStaticInstanceMethod(typeName, methodName, stdType);
                    }
                }
            }
        }
        return oResult;
    }

    /**
     * @return StdType instance
     */
    public final StdLibraryType getStdType() {
        return stdType;
    }

    /**
     * @return Name of type
     */
    public final String getTypeName() {
        return typeName;
    }

    /**
     * @return Name of method
     */
    public final String getMethodName() {
        return methodName;
    }
}
