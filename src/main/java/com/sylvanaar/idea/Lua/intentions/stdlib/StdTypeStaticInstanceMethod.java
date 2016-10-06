package com.sylvanaar.idea.Lua.intentions.stdlib;

import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaFieldIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaFunctionCallExpression;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;

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
        LuaReferenceElement typeRef = call.getFunctionNameElement();
        if (typeRef != null) {
            PsiElement compoundElement = typeRef.getFirstChild();
            if(compoundElement instanceof LuaCompoundIdentifier) {
                LuaCompoundIdentifier compoundId = (LuaCompoundIdentifier) compoundElement;
                String operator = compoundId.getOperator();
                if(operator == null || !operator.equals(".")) return null;
                if (! (compoundId.getLeftSymbol() instanceof LuaReferenceElement))
                    return null;
                typeRef = (LuaReferenceElement) compoundId.getLeftSymbol();
                LuaExpression fieldExpression = compoundId.getRightSymbol();
                if(fieldExpression == null || !(fieldExpression instanceof LuaFieldIdentifier))
                    return null;

                //LuaFieldIdentifier fieldId = (LuaFieldIdentifier)fieldExpression;
                String typeName = typeRef != null ? typeRef.getName() : null,
                       fieldName = fieldExpression.getName();
                if (typeName != null && fieldName != null) {
                    StdLibraryType stdType = StdLibraryTypes.getInstance().getStdType(typeName);
                    if (stdType != null && stdType.hasStaticInstanceMethod(fieldName)) {
                        oResult = new StdTypeStaticInstanceMethod(typeName, fieldName, stdType);
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
