/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.lang.psi.impl.symbols;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.NamedStub;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/24/11
 * Time: 12:26 AM
 */
public class LuaGlobalUsageImpl<T extends NamedStub> extends LuaGlobalIdentifierImpl<T> {
   // implements StubBasedPsiElement<LuaGlobalReferenceStub> {
    public LuaGlobalUsageImpl(ASTNode node) {
        super(node);
    }

    @Override
    public boolean isDeclaration() {
        return false;
    }
}
