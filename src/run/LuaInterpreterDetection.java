/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.run;


import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class to detect if there's is a Lua installation in one of the most common places.
 * <p/>
 * User: jansorg
 * Date: Oct 31, 2009
 * Time: 12:48:42 PM
 */
public class LuaInterpreterDetection {
    private static final List<String> guessLocations = Arrays.asList("/bin/Lua", "/usr/bin/Lua",
            "/usr/local/bin/Lua");

    public LuaInterpreterDetection() {
    }

    public String findBestLocation() {
        for (String guessLocation : guessLocations) {
            File f = new File(guessLocation);

            if (f.isFile() && f.canRead()) {
                return guessLocation;
            }
        }

        return "";
    }
}
