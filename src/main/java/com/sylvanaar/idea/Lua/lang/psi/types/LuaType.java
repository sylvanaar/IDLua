/*
 * Copyright 2012 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.lang.psi.types;

import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 7/2/12
 * Time: 7:08 PM
 */
public interface LuaType extends Serializable {

//    public static final LuaType USERDATA      = new LuaTypeImpl("USERDATA", "U");
//    public static final LuaType LIGHTUSERDATA = new LuaTypeImpl("LIGHTUSERDATA", "L");
//    public static final LuaType THREAD        = new LuaTypeImpl("THREAD", "X");
//    public static final LuaType ERROR         = new LuaTypeImpl("ERROR");
//
//    public static final LuaType STUB = new LuaTypeImpl("STUB", "STUB");


    String getEncodedAsString();

    String encode(Map<LuaType, String> encodingContext);

    String encodingResult(Map<LuaType, String> encodingContext, String encoded);

    LuaType getFromEncodedString(byte... input);

    LuaType combineTypes(LuaType type1, LuaType type2);
}
