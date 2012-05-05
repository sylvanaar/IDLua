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

import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.help.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.ui.*;
import com.intellij.refactoring.*;
import com.intellij.ui.*;
import com.sylvanaar.idea.Lua.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.refactoring.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;

public class LuaIntroduceVariableDialog extends DialogWrapper implements LuaIntroduceDialog<LuaIntroduceVariableSettings> {

  private final Project myProject;
  private final LuaExpression myExpression;
  private final int myOccurrencesCount;
  private final LuaIntroduceVariableBase.Validator myValidator;
  private final EventListenerList myListenerList = new EventListenerList();

  private static final String REFACTORING_NAME = "Introduce Variable";

  private JPanel contentPane;
  private ComboBox myNameComboBox;
  private JCheckBox myCbIsLocal;
  private JCheckBox myCbReplaceAllOccurences;

  private JLabel myNameLabel;
  private JButton buttonOK;
  public String myEnteredName;

  public LuaIntroduceVariableDialog(LuaIntroduceContext context, LuaIntroduceVariableBase.Validator validator,
                                    String[] possibleNames) {
    super(context.project, true);
    myProject = context.project;
    myExpression = context.expression;
    myOccurrencesCount = context.occurrences.length;
    myValidator = validator;
    setUpNameComboBox(possibleNames);

    setModal(true);
    getRootPane().setDefaultButton(buttonOK);
    setTitle(REFACTORING_NAME);
    init();
    setUpDialog();
    updateOkStatus();
  }

  @Nullable
  protected JComponent createCenterPanel() {
    return contentPane;
  }

  public JComponent getContentPane() {
    return contentPane;
  }

  @Nullable
  protected String getEnteredName() {
    if (myNameComboBox.getEditor().getItem() instanceof String &&
        ((String) myNameComboBox.getEditor().getItem()).length() > 0) {
      return (String) myNameComboBox.getEditor().getItem();
    } else {
      return null;
    }
  }

  protected boolean isReplaceAllOccurrences() {
    return myCbReplaceAllOccurences.isSelected();
  }

  private boolean isDeclareFinal() {
    return myCbIsLocal.isSelected();
  }


  private void setUpDialog() {

    myCbReplaceAllOccurences.setMnemonic(KeyEvent.VK_A);
    myCbReplaceAllOccurences.setFocusable(false);
    myCbIsLocal.setMnemonic(KeyEvent.VK_F);
    myCbIsLocal.setFocusable(false);
    myNameLabel.setLabelFor(myNameComboBox);

    myCbIsLocal.setSelected(true);

    // Replace occurences
    if (myOccurrencesCount > 1) {
      myCbReplaceAllOccurences.setSelected(false);
      myCbReplaceAllOccurences.setEnabled(true);
      myCbReplaceAllOccurences.setText(myCbReplaceAllOccurences.getText() + " (" + myOccurrencesCount + " occurrences)");
    } else {
      myCbReplaceAllOccurences.setSelected(false);
      myCbReplaceAllOccurences.setEnabled(false);
    }


  }

  private void setUpNameComboBox(String[] possibleNames) {

    final EditorComboBoxEditor comboEditor = new StringComboboxEditor(myProject, LuaFileType.LUA_FILE_TYPE, myNameComboBox);

    myNameComboBox.setEditor(comboEditor);
    myNameComboBox.setRenderer(new EditorComboBoxRenderer(comboEditor));

    myNameComboBox.setEditable(true);
    myNameComboBox.setMaximumRowCount(8);
    myListenerList.add(DataChangedListener.class, new DataChangedListener());

    myNameComboBox.addItemListener(
        new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            fireNameDataChanged();
          }
        }
    );

    ((EditorTextField) myNameComboBox.getEditor().getEditorComponent()).addDocumentListener(new DocumentListener() {
      public void beforeDocumentChange(DocumentEvent event) {
      }

      public void documentChanged(DocumentEvent event) {
        fireNameDataChanged();
      }
    }
    );

    contentPane.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        myNameComboBox.requestFocus();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

    for (String possibleName : possibleNames) {
      myNameComboBox.addItem(possibleName);
    }
  }

  public JComponent getPreferredFocusedComponent() {
    return myNameComboBox;
  }

  protected Action[] createActions() {
    return new Action[]{getOKAction(), getCancelAction(), getHelpAction()};
  }

  protected void doOKAction() {
    if (!myValidator.isOK(this)) {
      return;
    }
    super.doOKAction();
  }


  protected void doHelpAction() {
//    HelpManager.getInstance().invokeHelp(HelpID.INTRODUCE_VARIABLE);
  }

  class DataChangedListener implements EventListener {
    void dataChanged() {
      updateOkStatus();
    }
  }

  private void updateOkStatus() {
    String text = getEnteredName();
    setOKActionEnabled(LuaRefactoringUtil.isIdentifier(text));
  }

  private void fireNameDataChanged() {
    Object[] list = myListenerList.getListenerList();
    for (Object aList : list) {
      if (aList instanceof DataChangedListener) {
        ((DataChangedListener) aList).dataChanged();
      }
    }
  }

  public LuaIntroduceVariableSettings getSettings() {
    return new MyLuaIntroduceVariableSettings(this);
  }

  private static class MyLuaIntroduceVariableSettings implements LuaIntroduceVariableSettings {
    String myEnteredName;
    boolean myIsReplaceAllOccurrences;
    boolean myIsDeclareLocal;

    public MyLuaIntroduceVariableSettings(LuaIntroduceVariableDialog dialog) {
      myEnteredName = dialog.getEnteredName();
      myIsReplaceAllOccurrences = dialog.isReplaceAllOccurrences();
      myIsDeclareLocal = dialog.isDeclareFinal();
    }

    public String getName() {
      return myEnteredName;
    }

    public boolean replaceAllOccurrences() {
      return myIsReplaceAllOccurrences;
    }

    public boolean isLocal() {
      return myIsDeclareLocal;
    }

  }
}
