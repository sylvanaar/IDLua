package com.sylvanaar.idea.Lua.intentions.stdlib.impl;

public class StdTableType extends BaseStdLibraryType {
    private final String[] staticInstanceMethods = {
            "concat", "foreach", "foreachi", "getn", "insert", "maxn", "remove", "sort"
    };

    /**
     * Constructor
     */
    public StdTableType() {
        super("table");
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
