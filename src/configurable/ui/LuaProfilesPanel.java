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

import com.intellij.ide.actions.OpenProjectFileChooserDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.MasterDetailsStateService;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Icons;
import com.intellij.util.containers.HashMap;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class LuaProfilesPanel extends MasterDetailsComponent {
    private static final Icon COPY_ICON = IconLoader.getIcon("/actions/copy.png");

    private final Project myProject;
    private final LuaManager myManager;
  private final AtomicBoolean myInitialized = new AtomicBoolean(false);

  public LuaProfilesPanel(Project project) {
      MasterDetailsStateService.getInstance(project).register("Lua.UI", this);
        myProject = project;
        myManager = LuaManager.getInstance(project);
        initTree();
    }

    protected void processRemovedItems() {
        Map<String, LuaProfile> profiles = getAllProfiles();
        final List<LuaProfile> deleted = new ArrayList<LuaProfile>();
        for (LuaProfile profile : myManager.getLuas()) {
            if (!profiles.containsValue(profile)) {
                deleted.add(profile);
            }
        }
        for (LuaProfile profile : deleted) {
            myManager.removeLua(profile);
        }
    }

    protected boolean wasObjectStored(Object o) {
        return myManager.getLuas().contains((LuaProfile) o);
    }

    @Nls
    public String getDisplayName() {
        return "Lua Profiles";
    }

    @Nullable
    public Icon getIcon() {
        return null;
    }

    @Nullable
    @NonNls
    public String getHelpTopic() {
        return "Lua.profiles";
    }

    public void apply() throws ConfigurationException {
        final Set<String> profiles = new HashSet<String>();
        for (int i = 0; i < myRoot.getChildCount(); i++) {
            MyNode node = (MyNode) myRoot.getChildAt(i);
            final String profileName = ((LuaConfigurable) node.getConfigurable()).getEditableObject().getName();
            if (profiles.contains(profileName)) {
                selectNodeInTree(profileName);
                throw new ConfigurationException("Duplicate Lua profile name: \'" + profileName + "\'");
            }
            profiles.add(profileName);
        }
        super.apply();
    }

    public Map<String, LuaProfile> getAllProfiles() {
      final Map<String, LuaProfile> profiles = new HashMap<String, LuaProfile>();
        if (!myInitialized.get()) {
          for (LuaProfile profile : myManager.getLuas()) {
            profiles.put(profile.getName(), profile);
          }
        } else {
          for (int i = 0; i < myRoot.getChildCount(); i++) {
            MyNode node = (MyNode) myRoot.getChildAt(i);
            final LuaProfile LuaProfile = ((LuaConfigurable) node.getConfigurable()).getEditableObject();
            profiles.put(LuaProfile.getName(), LuaProfile);
          }
        }
        return profiles;
    }

  @Override
  public void disposeUIResources() {
    super.disposeUIResources();
    myInitialized.set(false);
  }

  @Nullable
    protected ArrayList<AnAction> createActions(boolean fromPopup) {
        ArrayList<AnAction> result = new ArrayList<AnAction>();
        result.add(new AnAction("Add", "Add", Icons.ADD_ICON) {
            {
                registerCustomShortcutSet(CommonShortcuts.INSERT, myTree);
            }
            public void actionPerformed(AnActionEvent event) {
                final String name = askForProfileName("Create new Lua profile", "");
                if (name == null) return;
                final LuaProfile LuaProfile = new LuaProfile(name);
                addProfileNode(LuaProfile);
            }


        });
        result.add(new MyDeleteAction(Conditions.alwaysTrue()));
        result.add(new AnAction("Copy", "Copy", COPY_ICON) {
            {
                registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK)), myTree);
            }
            public void actionPerformed(AnActionEvent event) {
                final String profileName = askForProfileName("Copy Lua profile", "");
                if (profileName == null) return;
                final LuaProfile clone = new LuaProfile();
                clone.copyFrom((LuaProfile) getSelectedObject());
                clone.setName(profileName);
                addProfileNode(clone);
            }

            public void update(AnActionEvent event) {
                super.update(event);
                event.getPresentation().setEnabled(getSelectedObject() != null);
            }
        });
        result.add(new AnAction("Import", "Import", Icons.ADVICE_ICON) {
          public void actionPerformed(AnActionEvent event) {
            final OpenProjectFileChooserDescriptor descriptor = new OpenProjectFileChooserDescriptor(true) {
              @Override
              public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                return super.isFileVisible(file, showHiddenFiles) || canContainLua(file);
              }

              @Override
              public boolean isFileSelectable(VirtualFile file) {
                return super.isFileSelectable(file) || canContainLua(file);
              }

              private boolean canContainLua(VirtualFile file) {
                return !file.isDirectory() && (file.getFileType() == StdFileTypes.IDEA_MODULE || file.getFileType() == StdFileTypes.XML);
              }
            };
            descriptor.setTitle("Choose file containing Lua notice");
            final VirtualFile[] files = FileChooser.chooseFiles(myProject, descriptor);
            if (files.length != 1) return;

            final List<LuaProfile> LuaProfiles = ExternalOptionHelper.loadOptions(VfsUtil.virtualToIoFile(files[0]));
            if (LuaProfiles != null) {
              if (LuaProfiles.size() == 1) {
                importProfile(LuaProfiles.get(0));
              } else {
                JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<LuaProfile>("Choose profile to import", LuaProfiles) {
                  @Override
                  public PopupStep onChosen(final LuaProfile selectedValue, boolean finalChoice) {
                    SwingUtilities.invokeLater(new Runnable(){
                      public void run() {
                        importProfile(selectedValue);
                      }
                    });
                    return FINAL_CHOICE;
                  }

                  @NotNull
                  @Override
                  public String getTextFor(LuaProfile value) {
                    return value.getName();
                  }
                }).showUnderneathOf(myNorthPanel);
              }
            }
            else {
              Messages.showWarningDialog(myProject, "The selected file did not contain any Lua settings.", "Import Failure");
            }
          }

          private void importProfile(LuaProfile LuaProfile) {
            final String profileName = askForProfileName("Import Lua profile", LuaProfile.getName());
            if (profileName == null) return;
            LuaProfile.setName(profileName);
            addProfileNode(LuaProfile);
            Messages.showInfoMessage(myProject, "The Lua settings have been successfully imported.", "Import Complete");
          }
        });
        return result;
    }



  @Nullable
  private String askForProfileName(String title, String initialName) {
        return Messages.showInputDialog("New Lua profile name:", title, Messages.getQuestionIcon(), initialName, new InputValidator() {
            public boolean checkInput(String s) {
                return !getAllProfiles().containsKey(s) && s.length() > 0;
            }

            public boolean canClose(String s) {
                return checkInput(s);
            }
        });
    }

    private void addProfileNode(LuaProfile LuaProfile) {
        final LuaConfigurable LuaConfigurable = new LuaConfigurable(myProject, LuaProfile, TREE_UPDATER);
        LuaConfigurable.setModified(true);
        final MyNode node = new MyNode(LuaConfigurable);
        addNode(node, myRoot);
        selectNodeInTree(node);
    }

    private void reloadTree() {
        myRoot.removeAllChildren();
      Collection<LuaProfile> collection = myManager.getLuas();
      for (LuaProfile profile : collection) {
        LuaProfile clone = new LuaProfile();
        clone.copyFrom(profile);
        addNode(new MyNode(new LuaConfigurable(myProject, clone, TREE_UPDATER)), myRoot);
      }
      myInitialized.set(true);
    }

    public void reset() {
        reloadTree();
        super.reset();
    }

  @Override
  protected String getEmptySelectionString() {
    return "Select a profile to view or edit its details here";
  }

  public void addItemsChangeListener(final Runnable runnable) {
    addItemsChangeListener(new ItemsChangeListener() {
      public void itemChanged(@Nullable Object deletedItem) {
        SwingUtilities.invokeLater(runnable);
      }

      public void itemsExternallyChanged() {
        SwingUtilities.invokeLater(runnable);
      }
    });
  }
}
