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

package com.sylvanaar.idea.Lua.editor.highlighter;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.sylvanaar.idea.Lua.LuaBundle;
import com.sylvanaar.idea.Lua.LuaIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: jon
 * Date: Apr 3, 2010
 * Time: 1:52:31 AM
 */
public class LuaColorsPage  implements ColorSettingsPage {
    final String DEMO_TEXT = "    a = { foo.bar, foo.bar(), fx(), f = a, 1, FOO } -- url http://www.url.com \n" +
            "     local x,y = 20,nil\n" +
            "     for i=1,10 do\n" +
            "       local y = 0\n" +
            "       a[i] = function () y=y+1; return x+y end\n" +
            "     end\n" +

            "--- External Documentation URL (shift-F1)\n" + "-- This is called by shift-F1 on the symbol, or by the\n" + "-- external documentation button on the quick help panel\n" + "-- @param name The name to get documentation for.\n" + "-- @return the URL of the external documentation\n" + "function getDocumentationUrl(name) \n" + "\tlocal p1, p2 = string.match(name, \"(%a+)\\.?(%a*)\")\n" + "\tlocal url = BASE_URL .. \"/docs/api/\" .. p1\n" + "\n" + "\tif p2 then url = url .. p2 end\n" + "\n" + "\treturn url\n" + "end"+

            "\thidden = {\n" +
            "\t    function()\n" +
            "\t        local t = base[k].args\n" +
            "\t        local hide = true\n" +
            "\t        for k,v in pairs(t) do\n" +
            "\t            hide = false\n" +
            "\t        end\n" +
            "\t        return hide\n" +
            "\t    end\n" +
            "\t }\n" +
            "\n" +
            " a:b(function(a,b)for i,v do end end, function(a,b) end)\n" +
            "--[[\n" +
            "-- http://www.lua.com\n" +
            "]] \n" +
            "\n" +
            "--[===[\n" +
            "-- http://www.lua.com\n" +
            "]===] \n" +
            "\n" +
            "--[[ test ]] x=y --[=[ test2 ]==] x=y ]=] x=y --[[ ntest1 ]]\n" +
            "--[==[ multiline and nesting --[[ http://www.lua.com\n" +
            "dd]] ]]\n" +
            "]===] \n" +
            "]==] --[[ good \n" +
            "]]\n" +
            "\n" +
            "f = [======[ hi\n" +
            "2u ]======] --note count won't be checked ]==]\n" +
            "\n" +
            "\"bad string" +
            "\n" +
            "a = { [f(1)] = g; \"x\", \"y\"; dox = 1, f(x), [30] = 23; 45}\n" +
            "\n" +
            "     endx = 10                -- global variable\n" +
            "     do                    -- new block\n" +
            "       local dox = x         -- new 'x', with value 10\n" +
            "       print(dox)            --> 10\n" +
            "       x = x+1+endVar.endVar\n" +
            "       do                  -- another block\n" +
            "         local x = x+1     -- another 'x'\n" +
            "         print(x)          --> 12\n" +
            "       end\n" +
            "\n" +
            "       print(x[\"foo\"])            --> 11\n" +
            "     end\n" +
            "     print(x)              --> 10  (the global one)\n" +
            "     type()\n" +
            "     f.type()\n" +
            "     f()                -- adjusted to 0 results\t\n" +
            "     g(f(), x)          -- f() is adjusted to 1 result\n" +
            "     g(x, f())          -- g gets x plus all results from f()\n" +
            "     a,b,c = f(), x     -- f() is adjusted to 1 result (c gets nil)\n" +
            "     a,b = ...          -- a gets the first vararg parameter, b gets\n" +
            "                        -- the second (both a and b may get nil if there\n" +
            "                        -- is no corresponding vararg parameter)\n" +
            "     \n" +
            "     a,b,c = x, f()     -- f() is adjusted to 2 results\n" +
            "     a,b,c = f()        -- f() is adjusted to 3 results\n" +
            "     return f()         -- returns all results from f()\n" +
            "     return ...         -- returns all received vararg parameters\n" +
            "     return x,y,f()     -- returns x, y, and all results from f()\n" +
            "     {f()}              -- creates a list with all results from f()\n" +
            "     {...}              -- creates a list with all vararg parameters\n" +
            "     {f(), nil}         -- f() is adjusted to 1 result";

    private static final AttributesDescriptor[] ATTRS = new AttributesDescriptor[]{
            new AttributesDescriptor(LuaBundle.message("color.settings.keyword"), LuaHighlightingData.KEYWORD),
            new AttributesDescriptor(LuaBundle.message("color.settings.constant.keywords"), LuaHighlightingData.DEFINED_CONSTANTS),
            new AttributesDescriptor(LuaBundle.message("color.settings.globals"), LuaHighlightingData.GLOBAL_VAR),
            new AttributesDescriptor(LuaBundle.message("color.settings.locals"), LuaHighlightingData.LOCAL_VAR),
            new AttributesDescriptor(LuaBundle.message("color.settings.field"), LuaHighlightingData.FIELD),
            new AttributesDescriptor(LuaBundle.message("color.settings.parameter"), LuaHighlightingData.PARAMETER),
            new AttributesDescriptor(LuaBundle.message("color.settings.comment"), LuaHighlightingData.COMMENT),
            new AttributesDescriptor(LuaBundle.message("color.settings.longcomment"), LuaHighlightingData.LONGCOMMENT),
            new AttributesDescriptor(LuaBundle.message("color.settings.longcomment.braces"), LuaHighlightingData.LONGCOMMENT_BRACES),
            new AttributesDescriptor(LuaBundle.message("color.settings.luadoc"), LuaHighlightingData.LUADOC),
            new AttributesDescriptor(LuaBundle.message("color.settings.number"), LuaHighlightingData.NUMBER),
            new AttributesDescriptor(LuaBundle.message("color.settings.string"), LuaHighlightingData.STRING),
            new AttributesDescriptor(LuaBundle.message("color.settings.longstring"), LuaHighlightingData.LONGSTRING),
            new AttributesDescriptor(LuaBundle.message("color.settings.longstring.braces"), LuaHighlightingData.LONGSTRING_BRACES),
            new AttributesDescriptor(LuaBundle.message("color.settings.brackets"), LuaHighlightingData.BRACKETS),
            new AttributesDescriptor(LuaBundle.message("color.settings.parenths"), LuaHighlightingData.PARENTHS),
            new AttributesDescriptor(LuaBundle.message("color.settings.braces"), LuaHighlightingData.BRACES),
            new AttributesDescriptor(LuaBundle.message("color.settings.comma"), LuaHighlightingData.COMMA),
            new AttributesDescriptor(LuaBundle.message("color.settings.bad_character"), LuaHighlightingData.BAD_CHARACTER),
    };

    @NotNull
	public String getDisplayName() {
		return LuaBundle.message("color.settings.name");
	}

	@Nullable
	public Icon getIcon() {
		return LuaIcons.LUA_ICON;
	}

	@NotNull
	public AttributesDescriptor[] getAttributeDescriptors() {
		return ATTRS;
	}

	@NotNull
	public ColorDescriptor[] getColorDescriptors() {
		return new ColorDescriptor[0];
	}

	@NotNull
	public SyntaxHighlighter getHighlighter() {
		return new LuaSyntaxHighlighter();
	}

	@NonNls
	@NotNull
	public String getDemoText() {
		return DEMO_TEXT;
	}

	@Nullable
	public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
		return null;
	}

}
