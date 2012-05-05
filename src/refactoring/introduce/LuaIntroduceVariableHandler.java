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

package com.sylvanaar.idea.Lua.refactoring.introduce;

/**
 * @author ilyas
 */
public class LuaIntroduceVariableHandler extends LuaIntroduceVariableBase {
  @Override
  protected String getRefactoringName() {
    return REFACTORING_NAME;
  }

  @Override
  protected String getHelpID() {
    return null;
  }

  protected LuaIntroduceVariableDialog getDialog(final LuaIntroduceContext context) {
    final Validator validator = new Validator() {
        @Override
        public boolean isOK(LuaIntroduceDialog dialog) {
            return true;
        }
    };
    String[] possibleNames = new String[]{};
    return new LuaIntroduceVariableDialog(context, validator, possibleNames);
  }
}
