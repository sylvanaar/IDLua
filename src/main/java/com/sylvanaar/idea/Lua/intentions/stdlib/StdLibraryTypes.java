package com.sylvanaar.idea.Lua.intentions.stdlib;

import com.sylvanaar.idea.Lua.intentions.stdlib.impl.StdCoroutineType;
import com.sylvanaar.idea.Lua.intentions.stdlib.impl.StdStringType;
import com.sylvanaar.idea.Lua.intentions.stdlib.impl.StdTableType;

import java.util.HashMap;
import java.util.Map;

public class StdLibraryTypes {
    private static StdLibraryTypes instance = new StdLibraryTypes();
    private Map<String, StdLibraryType> stdTypes;

    private StdLibraryTypes() {
        stdTypes = new HashMap<String, StdLibraryType>(3/*4*/);
        // On second thought, io static methods are not exactly the same
        // as their file instance method counterparts
        //new StdFileType().addToMap(stdTypes);
        new StdCoroutineType().addToMap(stdTypes);
        new StdStringType().addToMap(stdTypes);
        new StdTableType().addToMap(stdTypes);
    }

    public static StdLibraryTypes getInstance() {
        return instance;
    }

    public StdLibraryType getStdType(String typeName) {
        return isStdType(typeName) ? stdTypes.get(typeName) : null;
    }

    public boolean isStdType(String typeName) {
        return stdTypes.containsKey(typeName);
    }
}
