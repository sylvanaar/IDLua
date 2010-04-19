/*
 * Lua 2000-2009 JetBrains s.r.o.
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

package com.sylvanaar.idea.Lua.configurable.ui;

import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.packageDependencies.DependencyValidationManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.search.scope.packageSet.PackageSet;
import com.intellij.util.Options;
import com.intellij.util.containers.HashMap;
import com.intellij.util.text.UniqueNameGenerator;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@State(
  name = "LuaManager",
  storages = {@Storage(
    id = "default",
    file = "$PROJECT_FILE$"), @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/Lua/", scheme = StorageScheme.DIRECTORY_BASED,
                                       stateSplitter = LuaManager.LuaStateSplitter.class)})
public class LuaManager extends AbstractProjectComponent implements JDOMExternalizable, PersistentStateComponent<Element> {
  private static final Logger LOG = Logger.getInstance("#" + LuaManager.class.getName());
  @Nullable
  private LuaProfile myDefaultLua = null;

  private final LinkedHashMap<String, String> myModule2Luas = new LinkedHashMap<String, String>();

  private final Map<String, LuaProfile> myLuas = new HashMap<String, LuaProfile>();

  private final Options myOptions = new Options();

  public LuaManager(Project project) {
    super(project);
  }

  @NonNls
  private static final String Lua = "Lua";
  @NonNls
  private static final String MODULE2Lua = "module2Lua";
  @NonNls
  private static final String ELEMENT = "element";
  @NonNls
  private static final String MODULE = "module";
  @NonNls
  private static final String DEFAULT = "default";

  public static LuaManager getInstance(Project project) {
    return project.getComponent(LuaManager.class);
  }


//  public void projectOpened() {
//    if (myProject != null) {
//      FileEditorManagerListener listener = new FileEditorManagerAdapter() {
//        public void fileOpened(FileEditorManager fileEditorManager, VirtualFile virtualFile) {
//          if (virtualFile.isWritable() && NewFileTracker.getInstance().contains(virtualFile)) {
//            NewFileTracker.getInstance().remove(virtualFile);
//            if (FileTypeUtil.getInstance().isSupportedFile(virtualFile)) {
//              final Module module = ProjectRootManager.getInstance(myProject).getFileIndex().getModuleForFile(virtualFile);
//              if (module != null) {
//                final PsiFile file = PsiManager.getInstance(myProject).findFile(virtualFile);
//                if (file != null) {
//                  ApplicationManager.getApplication().invokeLater(new Runnable() {
//                    public void run() {
//                      if (file.isValid() && file.isWritable()) {
//                        new UpdateLuaProcessor(myProject, module, file).run();
//                      }
//                    }
//                  });
//                }
//              }
//            }
//          }
//        }
//      };
//
//      FileEditorManager.getInstance(myProject).addFileEditorManagerListener(listener, myProject);
//    }
//  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return "LuaManager";
  }

  public void readExternal(Element element) throws InvalidDataException {
    clearLuas();
    final Element module2Lua = element.getChild(MODULE2Lua);
    if (module2Lua != null) {
      for (Object o : module2Lua.getChildren(ELEMENT)) {
        final Element el = (Element)o;
        final String moduleName = el.getAttributeValue(MODULE);
        final String LuaName = el.getAttributeValue(Lua);
        myModule2Luas.put(moduleName, LuaName);
      }
    }
    for (Object o : element.getChildren(Lua)) {
      final LuaProfile LuaProfile = new LuaProfile();
      LuaProfile.readExternal((Element)o);
      myLuas.put(LuaProfile.getName(), LuaProfile);
    }
    myDefaultLua = myLuas.get(element.getAttributeValue(DEFAULT));
    myOptions.readExternal(element);
  }

  public void writeExternal(Element element) throws WriteExternalException {
    for (LuaProfile Lua : myLuas.values()) {
      final Element LuaElement = new Element(Lua);
      Lua.writeExternal(LuaElement);
      element.addContent(LuaElement);
    }
    final Element map = new Element(MODULE2Lua);
    for (String moduleName : myModule2Luas.keySet()) {
      final Element setting = new Element(ELEMENT);
      setting.setAttribute(MODULE, moduleName);
      setting.setAttribute(Lua, myModule2Luas.get(moduleName));
      map.addContent(setting);
    }
    element.addContent(map);
    element.setAttribute(DEFAULT, myDefaultLua != null ? myDefaultLua.getName() : "");
    myOptions.writeExternal(element);
  }


  public Element getState() {
    try {
      final Element e = new Element("settings");
      writeExternal(e);
      return e;
    }
    catch (WriteExternalException e1) {
      LOG.error(e1);
      return null;
    }
  }

  public void loadState(Element state) {
    try {
      readExternal(state);
    }
    catch (InvalidDataException e) {
      LOG.error(e);
    }
  }

  public Map<String, String> getLuasMapping() {
    return myModule2Luas;
  }

  public void setDefaultLua(@Nullable LuaProfile Lua) {
    myDefaultLua = Lua;
  }

  @Nullable
  public LuaProfile getDefaultLua() {
    return myDefaultLua;
  }

  public void addLua(LuaProfile LuaProfile) {
    myLuas.put(LuaProfile.getName(), LuaProfile);
  }

  public void removeLua(LuaProfile LuaProfile) {
    myLuas.values().remove(LuaProfile);
    for (Iterator<String> it = myModule2Luas.keySet().iterator(); it.hasNext();) {
      final String profileName = myModule2Luas.get(it.next());
      if (profileName.equals(LuaProfile.getName())) {
        it.remove();
      }
    }
  }

  public void clearLuas() {
    myDefaultLua = null;
    myLuas.clear();
    myModule2Luas.clear();
  }

  public void mapLua(String scopeName, String LuaProfileName) {
    myModule2Luas.put(scopeName, LuaProfileName);
  }

  public void unmapLua(String scopeName) {
    myModule2Luas.remove(scopeName);
  }

  public Collection<LuaProfile> getLuas() {
    return myLuas.values();
  }

  @Nullable
  public LuaProfile getLuaOptions(@NotNull PsiFile file) {
    if (myOptions.getOptions(file.getFileType().getName()).getFileTypeOverride() == LanguageOptions.NO_Lua) return null;
    final DependencyValidationManager validationManager = DependencyValidationManager.getInstance(myProject);
    for (String scopeName : myModule2Luas.keySet()) {
      final NamedScope namedScope = validationManager.getScope(scopeName);
      if (namedScope != null) {
        final PackageSet packageSet = namedScope.getValue();
        if (packageSet != null) {
          if (packageSet.contains(file, validationManager)) {
            final LuaProfile profile = myLuas.get(myModule2Luas.get(scopeName));
            if (profile != null) {
              return profile;
            }
          }
        }
      }
    }
    return myDefaultLua != null ? myDefaultLua : null;
  }

  public Options getOptions() {
    return myOptions;
  }

  public static class LuaStateSplitter implements StateSplitter {
    public List<Pair<Element, String>> splitState(Element e) {
      final UniqueNameGenerator generator = new UniqueNameGenerator();
      final List<Pair<Element, String>> result = new ArrayList<Pair<Element, String>>();

      final Element[] elements = JDOMUtil.getElements(e);
      for (Element element : elements) {
        if (element.getName().equals("Lua")) {
          element.detach();

          String profileName = null;
          final Element[] options = JDOMUtil.getElements(element);
          for (Element option : options) {
            if (option.getName().equals("option") && option.getAttributeValue("name").equals("myName")) {
              profileName = option.getAttributeValue("value");
            }
          }

          assert profileName != null;

          final String name = generator.generateUniqueName(FileUtil.sanitizeFileName(profileName)) + ".xml";
          result.add(new Pair<Element, String>(element, name));
        }
      }
      result.add(new Pair<Element, String>(e, generator.generateUniqueName("profiles_settings") + ".xml"));
      return result;
    }

    public void mergeStatesInto(Element target, Element[] elements) {
      for (Element element : elements) {
        if (element.getName().equals("Lua")) {
          element.detach();
          target.addContent(element);
        }
        else {
          final Element[] states = JDOMUtil.getElements(element);
          for (Element state : states) {
            state.detach();
            target.addContent(state);
          }
        }
      }
    }
  }
}
