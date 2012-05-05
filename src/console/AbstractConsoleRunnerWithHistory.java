/*
 * Copyright 2012 Jon S Akhtar (Sylvanaar)
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
package com.sylvanaar.idea.Lua.console;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.lookup.*;
import com.intellij.execution.*;
import com.intellij.execution.console.*;
import com.intellij.execution.executors.*;
import com.intellij.execution.process.*;
import com.intellij.execution.runners.*;
import com.intellij.execution.ui.*;
import com.intellij.execution.ui.actions.*;
import com.intellij.ide.*;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.ex.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.util.*;
import com.intellij.openapi.wm.*;
import com.intellij.ui.*;
import com.intellij.util.*;
import com.intellij.util.ui.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author oleg
 *         This class provides basic functionality for running consoles.
 *         It launches external process and handles line input with history
 */
public abstract class AbstractConsoleRunnerWithHistory<T extends LanguageConsoleViewImpl> {
  private final Project myProject;
  private final String myConsoleTitle;

  private ProcessHandler myProcessHandler;
  private final String myWorkingDir;

  private T myConsoleView;

  private ConsoleExecuteActionHandler myConsoleExecuteActionHandler;

  public AbstractConsoleRunnerWithHistory(@NotNull final Project project, @NotNull final String consoleTitle,
                                          @Nullable final String workingDir) {
    myProject = project;
    myConsoleTitle = consoleTitle;
    myWorkingDir = workingDir;
  }

  /**
   * Launch process, setup history, actions etc.
   *
   * @throws com.intellij.execution.ExecutionException
   */
  public void initAndRun() throws ExecutionException {
    // Create Server process
    myProcessHandler = createProcess();
    ProcessTerminatedListener.attach(myProcessHandler);

    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        initConsoleUI();
      }
    });
  }

  private void initConsoleUI() {
    // Init console view
    myConsoleView = createConsoleView();
    myConsoleView.setBorder(new SideBorder(UIUtil.getBorderColor(), SideBorder.LEFT));

    myConsoleExecuteActionHandler = createConsoleExecuteActionHandler();

    myProcessHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void processTerminated(ProcessEvent event) {
        finishConsole();
      }
    });

// Attach to process
    myConsoleView.attachToProcess(myProcessHandler);

// Runner creating
    final Executor defaultExecutor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID);
    final DefaultActionGroup toolbarActions = new DefaultActionGroup();
    final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, toolbarActions, false);

// Runner creating
    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(actionToolbar.getComponent(), BorderLayout.WEST);
    panel.add(myConsoleView.getComponent(), BorderLayout.CENTER);

    actionToolbar.setTargetComponent(panel);

    final RunContentDescriptor contentDescriptor =
      new RunContentDescriptor(myConsoleView, myProcessHandler, panel, constructConsoleTitle(myConsoleTitle));

// tool bar actions
    final List<AnAction> actions = fillToolBarActions(toolbarActions, defaultExecutor, contentDescriptor);
    registerActionShortcuts(actions, getLanguageConsole().getConsoleEditor().getComponent());
    registerActionShortcuts(actions, panel);
    panel.updateUI();
    showConsole(defaultExecutor, contentDescriptor);

// Run
    myProcessHandler.startNotify();
  }

  private String constructConsoleTitle(final @NotNull String consoleTitle) {
    if (shouldAddNumberToTitle()) {
      List<RunContentDescriptor> consoles = ExecutionHelper.collectConsolesByDisplayName(myProject, new NotNullFunction<String, Boolean>() {
        @NotNull
        @Override
        public Boolean fun(String dom) {
          return dom.contains(consoleTitle);
        }
      });
      int max = 0;
      for (RunContentDescriptor dsc : consoles) {
        ProcessHandler handler = dsc.getProcessHandler();
        if (handler != null && !handler.isProcessTerminated()) {
          if (max == 0) {
            max = 1;
          }
          try {
            int num = Integer.parseInt(dsc.getDisplayName().substring(consoleTitle.length() + 1, dsc.getDisplayName().length() - 1));
            if (num > max) {
              max = num;
            }
          }
          catch (Exception e) {
            //skip
          }
        }
      }
      if (max >= 1) {
        return consoleTitle + "(" + (max + 1) + ")";
      }
    }

    return consoleTitle;
  }

  protected boolean shouldAddNumberToTitle() {
    return false;
  }

  protected void showConsole(Executor defaultExecutor, RunContentDescriptor myDescriptor) {
    // Show in run toolwindow
    ExecutionManager.getInstance(myProject).getContentManager().showRunContent(defaultExecutor, myDescriptor);

// Request focus
    final ToolWindow window = ToolWindowManager.getInstance(myProject).getToolWindow(defaultExecutor.getId());
    window.activate(new Runnable() {
      public void run() {
        IdeFocusManager.getInstance(myProject).requestFocus(getLanguageConsole().getCurrentEditor().getContentComponent(), true);
      }
    });
  }

  protected void finishConsole() {
    myConsoleView.getConsole().setEditable(false);
  }

  protected abstract T createConsoleView();

  @Nullable
  protected abstract OSProcessHandler createProcess() throws ExecutionException;

  public static void registerActionShortcuts(final List<AnAction> actions, final JComponent component) {
    for (AnAction action : actions) {
      if (action.getShortcutSet() != null) {
        action.registerCustomShortcutSet(action.getShortcutSet(), component);
      }
    }
  }

  protected List<AnAction> fillToolBarActions(final DefaultActionGroup toolbarActions,
                                              final Executor defaultExecutor,
                                              final RunContentDescriptor contentDescriptor) {

    List<AnAction> actionList = Lists.newArrayList();

//stop
    final AnAction stopAction = createStopAction();
    actionList.add(stopAction);

//close
    final AnAction closeAction = createCloseAction(defaultExecutor, contentDescriptor);
    actionList.add(closeAction);

// run action
    actionList.add(createConsoleExecAction(getLanguageConsole(), myProcessHandler, myConsoleExecuteActionHandler));

// Help
    actionList.add(CommonActionsManager.getInstance().createHelpAction("interactive_console"));

    toolbarActions.addAll(actionList);

    return actionList;
  }

  protected AnAction createCloseAction(final Executor defaultExecutor, final RunContentDescriptor myDescriptor) {
    return new CloseAction(defaultExecutor, myDescriptor, myProject);
  }

  protected AnAction createStopAction() {
    return ActionManager.getInstance().getAction(IdeActions.ACTION_STOP_PROGRAM);
  }

  public LanguageConsoleImpl getLanguageConsole() {
    return myConsoleView.getConsole();
  }

  public static AnAction createConsoleExecAction(final LanguageConsoleImpl languageConsole,
                                                 final ProcessHandler processHandler,
                                                 final ConsoleExecuteActionHandler consoleExecuteActionHandler) {
    return new ConsoleExecuteAction(languageConsole, processHandler, consoleExecuteActionHandler);
  }

  @NotNull
  protected abstract ConsoleExecuteActionHandler createConsoleExecuteActionHandler();


  private static class ConsoleExecuteAction extends DumbAwareAction {
    public static final String ACTIONS_EXECUTE_ICON = "/actions/execute.png";

    private final LanguageConsoleImpl myLanguageConsole;
    private final ProcessHandler myProcessHandler;

    private final ConsoleExecuteActionHandler myConsoleExecuteActionHandler;


    public ConsoleExecuteAction(LanguageConsoleImpl languageConsole,
                                ProcessHandler processHandler,
                                ConsoleExecuteActionHandler consoleExecuteActionHandler) {
      super(null, null, IconLoader.getIcon(ACTIONS_EXECUTE_ICON));
      myLanguageConsole = languageConsole;
      myProcessHandler = processHandler;
      myConsoleExecuteActionHandler = consoleExecuteActionHandler;
      EmptyAction.setupAction(this, "Console.Execute", null);
    }

    public void actionPerformed(final AnActionEvent e) {
      myConsoleExecuteActionHandler.runExecuteAction(myLanguageConsole);
    }

    public void update(final AnActionEvent e) {
      final EditorEx editor = myLanguageConsole.getConsoleEditor();
      final Lookup lookup = LookupManager.getActiveLookup(editor);
      e.getPresentation().setEnabled(!editor.isRendererMode() && !myProcessHandler.isProcessTerminated() &&
                                     (lookup == null || !(lookup.isCompletion() && lookup.isFocused())));
    }
  }

  public T getConsoleView() {
    return myConsoleView;
  }

  public Project getProject() {
    return myProject;
  }

  public String getConsoleTitle() {
    return myConsoleTitle;
  }

  public String getWorkingDir() {
    return myWorkingDir;
  }

  public ProcessHandler getProcessHandler() {
    return myProcessHandler;
  }

  public ConsoleExecuteActionHandler getConsoleExecuteActionHandler() {
    return myConsoleExecuteActionHandler;
  }

}
