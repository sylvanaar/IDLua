package com.sylvanaar.idea.Lua.intentions.stdlib.impl;

public class StdCoroutineType extends BaseStdLibraryType {
    private final String[] staticInstanceMethods = {
            "resume", "status"
    };

    /**
     * Constructor
     */
    public StdCoroutineType() {
        super("coroutine");
    }

    /**
     * When overriden in inheriting classes, this method should return a list
     * of all instance method names for this type that can be accessed statically.
     *
     * @return Names of instance methods that can be accessed statically
     */
    @Override
    protected String[] getStaticInstanceMethods() {
        return staticInstanceMethods;
    }
}
