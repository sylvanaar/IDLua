package com.sylvanaar.idea.Lua.intentions.stdlib.impl;

import java.util.Map;
import com.sylvanaar.idea.Lua.intentions.stdlib.StdLibraryType;

public abstract class BaseStdLibraryType implements StdLibraryType {
    /**
     * Internally
     */
    private final String typeName;

    /**
     * Constructor with parameter for setting the name.
     *
     * @param name The identifier used for this type. (Type name)
     */
    protected BaseStdLibraryType(String name) {
        typeName = name;
    }

    /**
     * toString override
     *
     * @return getName
     */
    @Override
    public String toString() {
        return this.getName();
    }

    /**
     * Getter for property 'name'.
     *
     * @return Value for property 'name'.
     */
    @Override
    public String getName() {
        return typeName;
    }

    /**
     * Tests whether the instance method specified can be accessed statically.
     *
     * @param methodName Method name to check
     * @return True if conditions are met. False otherwise.
     */
    @Override
    public boolean hasStaticInstanceMethod(String methodName) {
        if (methodName == null) return false;
        String[] methodNames = getStaticInstanceMethods();
        for (String methodName1 : methodNames) {
            if (methodName.equals(methodName1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * When overriden in inheriting classes, this method should return a list
     * of all instance method names for this type that can be accessed statically.
     *
     * @return Names of instance methods that can be accessed statically
     */
    protected abstract String[] getStaticInstanceMethods();

    /**
     * Adds this instance to a typeMap
     *
     * @param typeMap see StdLibraryTypes for info
     */
    public void addToMap(Map<String, StdLibraryType> typeMap) {
        typeMap.put(getName(), this);
    }
}
