package com.sylvanaar.idea.Lua.intentions.stdlib.impl;

public class StdStringType extends BaseStdLibraryType {
    private final String[] staticInstanceMethods = {
            "byte", "find", "format", "gfind", "gmatch", "gsub", "len", "lower",
            "match", "rep", "reverse", "sub", "upper"
    };

    /**
     * Constructor
     */
    public StdStringType() {
        super("string");
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
