/*
 * Copyright 2009 Max Ishchenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sylvanaar.idea.Lua.lang;

import com.intellij.lang.Commenter;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 04.07.2009
 * Time: 14:12:45
 */
public class LuaCommenter implements Commenter {

    public String getLineCommentPrefix() {
        return "--";
    }

    public String getBlockCommentPrefix() {
        return "--[[";
    }

    public String getBlockCommentSuffix() {
        return "]]";

    }

    @Override
    public String getCommentedBlockCommentPrefix() {
        return null;
    }

    @Override
    public String getCommentedBlockCommentSuffix() {
        return null;
    }
}
