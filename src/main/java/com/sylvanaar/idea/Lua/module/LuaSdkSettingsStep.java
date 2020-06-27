package com.sylvanaar.idea.Lua.module;

// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import com.intellij.CommonBundle;
import com.intellij.ide.JavaUiBundle;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ui.configuration.*;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Condition;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.intellij.openapi.roots.ui.configuration.SdkComboBoxModel.createJdkComboBoxModel;
import static com.intellij.openapi.roots.ui.configuration.SdkComboBoxModel.createSdkComboBoxModel;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.HORIZONTAL;

/**
 * @author Dmitry Avdeev
 */
public class LuaSdkSettingsStep extends ModuleWizardStep {
    protected final SdkComboBox mySdkComboBox;
    protected final WizardContext myWizardContext;
    protected final ProjectSdksModel myModel;
    private final ModuleBuilder myModuleBuilder;
    private final JPanel myJdkPanel;

//    public LuaSdkSettingsStep(@NotNull SettingsStep settingsStep, @NotNull ModuleBuilder moduleBuilder,
//                              @NotNull Condition<? super SdkTypeId> sdkTypeIdFilter) {
//        this(settingsStep, moduleBuilder, sdkTypeIdFilter, null);
//    }
//
//    public LuaSdkSettingsStep(@NotNull SettingsStep settingsStep,
//                              @NotNull ModuleBuilder moduleBuilder,
//                              @NotNull Condition<? super SdkTypeId> sdkTypeIdFilter,
//                              @Nullable Condition<? super Sdk> sdkFilter) {
//        this(settingsStep.getContext(), moduleBuilder, sdkTypeIdFilter, sdkFilter);
//        if (!isEmpty()) {
//            settingsStep.addSettingsField(getSdkFieldLabel(settingsStep.getContext().getProject()), myJdkPanel);
//        }
//    }

    public LuaSdkSettingsStep(@NotNull WizardContext context,
                              @NotNull ModuleBuilder moduleBuilder,
                              @NotNull Condition<? super SdkTypeId> sdkTypeIdFilter,
                              @Nullable Condition<? super Sdk> sdkFilter) {
        myModuleBuilder = moduleBuilder;

        myWizardContext = context;
        myModel = new ProjectSdksModel();

        Project project =  myWizardContext.getProject() == null ? ProjectManager.getInstance().getDefaultProject() : myWizardContext
                .getProject();

        myModel.reset(project);

        mySdkComboBox = new SdkComboBox(createSdkComboBoxModel(project, myModel));
        myJdkPanel = new JPanel(new GridBagLayout());

        final PropertiesComponent component = project == null ? PropertiesComponent.getInstance() : PropertiesComponent.getInstance(project);
        ModuleType moduleType = moduleBuilder.getModuleType();
        final String selectedJdkProperty = "jdk.selected." + (moduleType == null ? "" : moduleType.getId());
        mySdkComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Sdk jdk = mySdkComboBox.getSelectedSdk();
                if (jdk != null) {
                    component.setValue(selectedJdkProperty, jdk.getName());
                }
                onSdkSelected(jdk);
            }
        });

        preselectSdk(project, component.getValue(selectedJdkProperty), sdkTypeIdFilter);
        myJdkPanel.add(mySdkComboBox, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, CENTER, HORIZONTAL, JBUI.emptyInsets(), 0, 0));
    }

    private void preselectSdk(Project project, String lastUsedSdk, Condition<? super SdkTypeId> sdkFilter) {
        mySdkComboBox.reloadModel();

        if (project != null) {
            Sdk sdk = ProjectRootManager.getInstance(project).getProjectSdk();
            if (sdk != null && myModuleBuilder.isSuitableSdkType(sdk.getSdkType())) {
                // use project SDK
                mySdkComboBox.setSelectedItem(mySdkComboBox.showProjectSdkItem());
                return;
            }
        }

        if (lastUsedSdk != null) {
            Sdk sdk = ProjectJdkTable.getInstance().findJdk(lastUsedSdk);
            if (sdk != null && myModuleBuilder.isSuitableSdkType(sdk.getSdkType())) {
                mySdkComboBox.setSelectedSdk(sdk);
                return;
            }
        }

        // set default project SDK
        Project defaultProject = ProjectManager.getInstance().getDefaultProject();
        Sdk selected = ProjectRootManager.getInstance(defaultProject).getProjectSdk();
        if (selected != null && sdkFilter.value(selected.getSdkType())) {
            mySdkComboBox.setSelectedSdk(selected);
            return;
        }

//        Sdk best = null;
//        SdkComboBoxModel model = mySdkComboBox.getModel();
//        for(int i = 0; i < model.getSize(); i++) {
//            SdkListItem item = model.getElementAt(i);
//
//            Sdk jdk = item.getSdk();
//            if (jdk == null) continue;
//
//            SdkTypeId jdkType = jdk.getSdkType();
//            if (!sdkFilter.value(jdkType)) continue;
//
//            if (best == null) {
//                best = jdk;
//                continue;
//            }
//
//            SdkTypeId bestType = best.getSdkType();
//            //it is in theory possible to have several SDK types here, let's just pick the first lucky type for now
//            if (bestType == jdkType && bestType.versionComparator().compare(best, jdk) < 0) {
//                best = jdk;
//            }
//        }
//
//        if (best != null) {
//            mySdkComboBox.setSelectedSdk(best);
//        } else {
            mySdkComboBox.setSelectedItem(mySdkComboBox.showNoneSdkItem());
//        }
    }

    protected void onSdkSelected(Sdk sdk) {}

    public boolean isEmpty() {
        return myJdkPanel.getComponentCount() == 0;
    }

    @NotNull
    protected String getSdkFieldLabel(@Nullable Project project) {
        return (project == null ? "Project" : "Module") + " \u001BSDK:";
    }

    @Override
    public JComponent getComponent() {
        return myJdkPanel;
    }

    @Override
    public void updateDataModel() {
        Project project = myWizardContext.getProject();
        Sdk jdk = mySdkComboBox.getSelectedSdk();
        if (project == null) {
            myWizardContext.setProjectJdk(jdk);
        }
        else {
            myModuleBuilder.setModuleJdk(jdk);
        }
    }

    @Override
    public boolean validate() throws ConfigurationException {
        SdkListItem item = mySdkComboBox.getSelectedItem();
        if (mySdkComboBox.getSelectedSdk() == null) {
            if (Messages.showDialog(getNoSdkMessage(),
                    JavaUiBundle.message("title.no.jdk.specified"),
                    new String[]{CommonBundle.getYesButtonText(), CommonBundle.getNoButtonText()}, 1, Messages.getWarningIcon()) != Messages.YES) {
                return false;
            }
        }
        try {
            myModel.apply(null, true);
        } catch (ConfigurationException e) {
            //IDEA-98382 We should allow Next step if user has wrong SDK
            if (Messages.showDialog(JavaUiBundle.message("dialog.message.0.do.you.want.to.proceed", e.getMessage()),
                    e.getTitle(),
                    new String[]{CommonBundle.getYesButtonText(), CommonBundle.getNoButtonText()}, 1, Messages.getWarningIcon()) != Messages.YES) {
                return false;
            }
        }
        return true;
    }

    protected String getNoSdkMessage() {
        return JavaUiBundle.message("prompt.confirm.project.no.jdk");
    }
}
