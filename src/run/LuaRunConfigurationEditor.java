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
package com.sylvanaar.idea.Lua.run;

import com.intellij.execution.configurations.LogFileOptions;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.RawCommandLineEditor;
import com.sylvanaar.idea.Lua.LuaBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;

public class LuaRunConfigurationEditor extends SettingsEditor<LuaRunConfiguration> {

  private DefaultComboBoxModel myModulesModel = new DefaultComboBoxModel();
 // private final JComboBox myModules = new JComboBox(myModulesModel);
  private final JLabel myModuleLabel = new JLabel(LuaBundle.message("run.configuration.classpath.from.module.choose"));
  private final LabeledComponent<RawCommandLineEditor> myVMParameters = new LabeledComponent<RawCommandLineEditor>();
  private final LabeledComponent<RawCommandLineEditor> myProgramParameters = new LabeledComponent<RawCommandLineEditor>();

  @NonNls private final JCheckBox myShowLogs = new JCheckBox(LuaBundle.message("show.smth", "idea.log"));

  private final LuaRunConfiguration myPRC;
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.Lua.run.LuaRunConfigurationEditor");

  public LuaRunConfigurationEditor(final LuaRunConfiguration prc) {
    myPRC = prc;
    myShowLogs.setSelected(isShow(prc));
    myShowLogs.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        setShow(prc, myShowLogs.isSelected());
      }
    });
//    myModules.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        if (myModules.getSelectedItem() != null){
//          prc.removeAllLogFiles();
//          Sdk jdk = ModuleRootManager.getInstance((Module)myModules.getSelectedItem()).getSdk();
//          jdk = IdeaJdk.findIdeaJdk(jdk);
//          if (jdk != null) {
//            final String sandboxHome = ((Sandbox)jdk.getSdkAdditionalData()).getSandboxHome();
//            if (sandboxHome == null){
//              return;
//            }
//            try {
//              @NonNls final String file = new File(sandboxHome).getCanonicalPath() + File.separator + "system" + File.separator + "log" + File.separator +
//                                  "idea.log";
//              if (new File(file).exists()){
//                prc.addLogFile(file, LuaBundle.message("idea.log.tab.title"), myShowLogs.isSelected());
//              }
//            }
//            catch (IOException e1) {
//              LOG.error(e1);
//            }
//          }
//        }
//      }
//    });
  }

  private static void setShow(LuaRunConfiguration prc, boolean show){
    final ArrayList<LogFileOptions> logFiles = prc.getLogFiles();
    for (LogFileOptions logFile: logFiles) {
      logFile.setEnable(show);
    }
  }

  private static boolean isShow(LuaRunConfiguration prc){
    final ArrayList<LogFileOptions> logFiles = prc.getLogFiles();
    for (LogFileOptions logFile : logFiles) {
      if (logFile.isEnabled()) return true;
    }
    return false;
  }

  public void resetEditorFrom(LuaRunConfiguration prc) {
    //myModules.setSelectedItem(prc.getModule());
    getVMParameters().setText(prc.VM_PARAMETERS);
    getProgramParameters().setText(prc.PROGRAM_PARAMETERS);
  }


  public void applyEditorTo(LuaRunConfiguration prc) throws ConfigurationException {
  //  prc.setModule(((Module)myModules.getSelectedItem()));
    prc.VM_PARAMETERS = getVMParameters().getText();
    prc.PROGRAM_PARAMETERS = getProgramParameters().getText();
  }

  @NotNull
  public JComponent createEditor() {
//    myModulesModel = new DefaultComboBoxModel(myPRC.getModules());
//    myModules.setModel(myModulesModel);
//    myModules.setRenderer(new DefaultListCellRenderer() {
//      public Component getListCellRendererComponent(JList list, final Object value, int index, boolean isSelected, boolean cellHasFocus) {
//        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//        if (value != null) {
//          setText(ApplicationManager.getApplication().runReadAction(new Computable<String >() {
//            public String compute() {
//              return ((Module)value).getName();
//            }
//          }));
//          setIcon(((Module)value).getModuleType().getNodeIcon(true));
//        }
//        return this;
//      }
//    });
    JPanel wholePanel = new JPanel(new GridBagLayout());
    myVMParameters.setText(LuaBundle.message("vm.parameters"));
    myVMParameters.setComponent(new RawCommandLineEditor());
    myVMParameters.getComponent().setDialogCaption(myVMParameters.getRawText());

    myProgramParameters.setText(LuaBundle.message("program.parameters"));
    myProgramParameters.setComponent(new RawCommandLineEditor());
    myProgramParameters.getComponent().setDialogCaption(myProgramParameters.getRawText());

    GridBagConstraints gc = new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0);
    wholePanel.add(myVMParameters, gc);
    wholePanel.add(myProgramParameters, gc);
    wholePanel.add(myShowLogs, gc);
    wholePanel.add(myModuleLabel, gc);
    gc.weighty = 1;
    ///wholePanel.add(myModules, gc);
    return wholePanel;
  }

  public RawCommandLineEditor getVMParameters() {
    return myVMParameters.getComponent();
  }

  public RawCommandLineEditor getProgramParameters() {
    return myProgramParameters.getComponent();
  }

  public void disposeEditor() {
  }
}