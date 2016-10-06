/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.editor;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.xml.model.gotosymbol.GoToSymbolProvider;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiManager;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.resolve.ResolveUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 11/23/10
 * Time: 3:38 PM
 */
public class LuaGotoSymbolContributor implements ChooseByNameContributor {
    @Override
    public String[] getNames(Project project, boolean b) {
        final Collection<String> names = new ArrayList<String>();
        final Collection<LuaDeclarationExpression> globals = LuaPsiManager.getInstance(project).getFilteredGlobalsCache();

        for (LuaDeclarationExpression global : globals) {
            names.add(global.getDefinedName());
        }
        return names.toArray(new String[names.size()]);
    }


    @Override 
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        GlobalSearchScope scope = includeNonProjectItems ? GlobalSearchScope.allScope(project) : GlobalSearchScope.projectScope(project);

        final Collection<LuaDeclarationExpression> globals = ResolveUtil.getFilteredGlobals(project, scope);
        List<NavigationItem> symbols = new ArrayList<NavigationItem>();

        for (LuaDeclarationExpression global : globals) {
            if (!includeNonProjectItems && !scope.contains(global.getContainingFile().getVirtualFile()))
                continue;

            if (global.getDefinedName().startsWith(pattern))
                symbols.add(new GoToSymbolProvider.BaseNavigationItem(global, global.getDefinedName(), global.getIcon(0)));
        }

        //symbols.addAll(StubIndex.getInstance().get(LuaGlobalDeclarationIndex.KEY, name, project, scope));

        return symbols.toArray(new NavigationItem[symbols.size()]);
    }
}
