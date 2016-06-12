package com.sylvanaar.idea.Lua.intentions.stdlib;

public interface StdLibraryType {
    /**
     * Getter for property 'name'.
     *
     * @return Value for property 'name'.
     */
    public String getName();

    /**
     * Tests whether the instance method specified can be accessed statically.
     *
     * @param methodName Method name to check
     * @return True if conditions are met. False otherwise.
     */
    public boolean hasStaticInstanceMethod(String methodName);
}
