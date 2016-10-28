/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
package com.sylvanaar.idea.Lua.facet;

import com.intellij.facet.FacetManager;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportConfigurable;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.ui.LabeledComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class LuaFrameworkSupportConfigurable extends FrameworkSupportConfigurable {
  private final JComponent myMainPanel;
  private final LuaSdkComboBox mySdkComboBox;

  public LuaFrameworkSupportConfigurable(FrameworkSupportModel model) {
    mySdkComboBox = new LuaSdkComboBox();
    mySdkComboBox.setProject(model.getProject());
    myMainPanel = LabeledComponent.create(mySdkComboBox, "Lua SDK:");
    ((LabeledComponent)myMainPanel).setLabelLocation(BorderLayout.WEST);
  }

  public JComponent getComponent() {
    return myMainPanel;
  }

  public void addSupport(@NotNull Module module, @NotNull ModifiableRootModel model, @Nullable Library library) {
    final FacetManager facetManager = FacetManager.getInstance(module);
    ModifiableFacetModel facetModel = facetManager.createModifiableModel();
    LuaFacet facet = facetManager.createFacet(LuaFacetType.getInstance(), "Lua", null);
    facet.getConfiguration().setSdk(mySdkComboBox.getSelectedSdk());
    facetModel.addFacet(facet);
    facetModel.commit();
  }
}
