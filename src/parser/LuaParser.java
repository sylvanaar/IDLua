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

package com.sylvanaar.idea.Lua.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IElementType;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 07.07.2009
 * Time: 17:37:49
 */
public class LuaParser implements PsiParser {
    static Logger log = Logger.getLogger(LuaParser.class);
    Project project;

    public LuaParser(Project project) {
        this.project = project;

    }

 	@NotNull
     public ASTNode parse(IElementType root, PsiBuilder builder) {
       final LuaPsiBuilder psiBuilder = new LuaPsiBuilder(builder);
       final PsiBuilder.Marker rootMarker = psiBuilder.mark();
       psiBuilder.debug();
       while (!psiBuilder.eof()) {
        
         parseOne(psiBuilder);
       }
       rootMarker.done(root);
       return builder.getTreeBuilt();
     }

    class Scope {
       PsiBuilder.Marker mark;
       IElementType type;

        Scope(PsiBuilder.Marker mark, IElementType type) {
            this.mark = mark;
            this.type = type;
        }

        void done() {
            mark.done(type);
        }
    }
    
    Deque<Scope> blockStack = new ArrayDeque<Scope>();
    private ASTNode parseOne(LuaPsiBuilder builder) {
        
        int last = 0;
        while (!builder.eof()) {
          if (builder.compare(LuaElementTypes.BLOCK_BEGIN_SET)) {
            blockStack.addFirst(new Scope(builder.mark(), LuaElementTypes.BLOCK));
            builder.advanceLexer();
          }
          else if (builder.compareAndEat((LuaElementTypes.BLOCK_END_SET))) {
              Scope scope = blockStack.removeFirst();
              if (scope != null) scope.done();
          }
          else {
               builder.advanceLexer();
          }

        }

        return null;
    }

}


