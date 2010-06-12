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

package com.sylvanaar.idea.Lua.lang.formatter;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.FileTypeIndentOptionsProvider;
import com.sylvanaar.idea.Lua.LuaFileType;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 14, 2010
 * Time: 1:25:33 AM
 */
public class LuaIndentOptionsProvider implements FileTypeIndentOptionsProvider {
  public CodeStyleSettings.IndentOptions createIndentOptions() {
    final CodeStyleSettings.IndentOptions indentOptions = new CodeStyleSettings.IndentOptions();
    indentOptions.INDENT_SIZE = 2;
    return indentOptions;
  }

  public FileType getFileType() {
    return LuaFileType.LUA_FILE_TYPE;
  }

  public IndentOptionsEditor createOptionsEditor() {
    return new SmartIndentOptionsEditor();
  }

  public String getPreviewText() {
    return "function fat(x)\n" +
            "  if x <= 1 then return 1\n" +
            "  else return x*loadstring(\"return fat(\" .. x-1 .. \")\")()\n" +
            "  end\n" +
            "end\n" +
            "\n" +
            "assert(loadstring \"loadstring 'assert(fat(6)==720)' () \")()\n" +
            "a = loadstring('return fat(5), 3')\n" +
            "a,b = a()\n" +
            "assert(a == 120 and b == 3)\n" +
            "print('+')\n" +
            "\n" +
            "function err_on_n (n)\n" +
            "  if n==0 then error(); exit(1);\n" +
            "  else err_on_n (n-1); exit(1);\n" +
            "  end\n" +
            "end\n" +
            "\n" +
            "do\n" +
            "  function dummy (n)\n" +
            "    if n > 0 then\n" +
            "      assert(not pcall(err_on_n, n))\n" +
            "      dummy(n-1)\n" +
            "    end\n" +
            "  end\n" +
            "end\n" +
            "\n" +
            "dummy(10)\n" +
            "\n" +
            "function deep (n)\n" +
            "  if n>0 then deep(n-1) end\n" +
            "end\n" +
            "deep(10)";
  }

  public void prepareForReformat(final PsiFile psiFile) {
  }
}
