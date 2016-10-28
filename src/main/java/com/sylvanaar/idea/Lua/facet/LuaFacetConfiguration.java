/*
 * Copyright 2009 Joachim Ansorg, mail@ansorg-it.com
 * File: LuaFacetConfiguration.java, Class: LuaFacetConfiguration
 * Last modified: 2010-02-17
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sylvanaar.idea.Lua.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.sylvanaar.idea.Lua.sdk.LuaSdkType;
import org.jdom.Element;

@State(name = "LuaFacet", storages = {@Storage(StoragePathMacros.MODULE_FILE)})
public class LuaFacetConfiguration implements FacetConfiguration, PersistentStateComponent<LuaFacetProperties> {
    private Logger LOG = Logger.getInstance("Lua.LuaFacetConfiguration");

    LuaFacetProperties properties = new LuaFacetProperties();
    private Sdk sdk;

    LuaFacetConfiguration() {
    }

    public FacetEditorTab[] createEditorTabs(FacetEditorContext facetEditorContext, FacetValidatorsManager
            facetValidatorsManager) {
        return new FacetEditorTab[]{new LuaSdkEditorTab(facetEditorContext)};
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
    }

    @Override
    public LuaFacetProperties getState() {
        return properties;
    }

    @Override
    public void loadState(LuaFacetProperties state) {
        XmlSerializerUtil.copyBean(state, properties);

        Sdk sdk = StringUtil.isEmpty(properties.SDK_NAME) ? null : ProjectJdkTable.getInstance().findJdk(properties.SDK_NAME, LuaSdkType
                .getInstance().getName());

        setSdk(sdk);
    }

    public Sdk getSdk() {
        return sdk;
    }

    public void setSdk(Sdk sdk) {
        this.sdk = sdk;
        properties.SDK_NAME = sdk.getName();
    }
}