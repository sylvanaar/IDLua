/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.sylvanaar.idea.Lua.debugger;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 4/3/11
 * Time: 11:48 AM
 */
public class LuaPosition {
    private final String myPath;
    private final int myLine;

    public LuaPosition(String path, int line)
    {
        myPath = path;
        myLine = line;
    }

    public String getPath()
    {
        return myPath;
    }

    public int getLine()
    {
        return myLine;
    }

    public boolean equals(Object o)
    {
        if(this == o)
            return true;
        if(!(o instanceof LuaPosition))
            return false;
        LuaPosition that = (LuaPosition)o;
        if(myLine != that.myLine)
            return false;
        return myPath.equals(that.myPath);
    }

    public int hashCode()
    {
        int result = myPath.hashCode();
        result = 31 * result + myLine;
        return result;
    }

}
