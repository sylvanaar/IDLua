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

package com.sylvanaar.idea.Lua.platform;

import com.intellij.openapi.vfs.VirtualFile;
import com.sylvanaar.idea.Lua.configurator.LuaServerDescriptor;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 27.07.2009
 * Time: 1:30:36
 */
public interface PlatformDependentTools {

    boolean checkExecutable(VirtualFile file);

    boolean checkExecutable(String path);

    String[] getStartCommand(LuaServerDescriptor descriptor);

    String[] getStopCommand(LuaServerDescriptor descriptor);

    String[] getReloadCommand(LuaServerDescriptor descriptor);

    String[] getTestCommand(LuaServerDescriptor descriptor);

    /**
     * @param file Lua executable
     * @return new LuaServerDescriptor respecting -V output
     * @throws ThisIsNotLuaExecutableException
     *          - when file.getPath() -V can not be executed, or can not be parsed
     */
    LuaServerDescriptor createDescriptorFromFile(VirtualFile file) throws ThisIsNotLuaExecutableException;

    /**
     * @param virtualFile Lua executable
     * @return new LuaServerDescriptor instance ignoring -V output
     */
    LuaServerDescriptor getDefaultDescriptorFromFile(VirtualFile virtualFile);

    static class ThisIsNotLuaExecutableException extends Exception {

        public ThisIsNotLuaExecutableException(String message) {
            super(message);
        }

        public ThisIsNotLuaExecutableException(Throwable e) {
            super(e);
        }
    }
}
