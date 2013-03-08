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

package com.sylvanaar.idea.Lua.luaj;

import com.google.common.base.Charsets;
import com.intellij.openapi.application.ApplicationManager;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.script.LuaScriptEngine;
import se.krka.kahlua.j2se.interpreter.History;
import se.krka.kahlua.j2se.interpreter.InputTerminal;
import se.krka.kahlua.j2se.interpreter.OutputTerminal;
import se.krka.kahlua.j2se.interpreter.jsyntax.JSyntaxUtil;

import javax.script.ScriptContext;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Future;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 19, 2010
 * Time: 4:29:54 PM
 */
public class LuaJInterpreter extends JPanel {

    private final OutputTerminal terminal;
    private final JLabel status = new JLabel("");

    private final History history = new History();
    private ScriptContext         myContext;
    private Future<?>             future;
    private LuaScriptEngine       engine;
    private ByteArrayOutputStream myOutput;
    private ByteArrayOutputStream myErrors;

    private Globals _G;

    public LuaJInterpreter() {
        super(new BorderLayout());

        JSyntaxUtil.setup();

        // create a Lua engine
        _G = JsePlatform.debugGlobals();


        final InputTerminal input = new InputTerminal(Color.BLACK);

//        final KahluaKit kit = new KahluaKit(new LuaLexer());
//        JSyntaxUtil.installSyntax(input, true, kit);
        //new AutoComplete(input, platform, env);

        terminal = new OutputTerminal(Color.BLACK, input.getFont(), input);
        terminal.setPreferredSize(new Dimension(800, 400));
        terminal.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() != 0) {
                    input.requestFocus();
                    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(e);
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        add(status, BorderLayout.SOUTH);
        add(terminal, BorderLayout.CENTER);

        input.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (isControl(keyEvent)) {
                    if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (isDone()) {
                            String text = input.getText();
                            history.add(text);
                            terminal.appendLua(withNewline(text));
                            input.setText("");
                            execute(text);
                        }
                        keyEvent.consume();
                    }
                    if (keyEvent.getKeyCode() == KeyEvent.VK_UP) {
                        history.moveBack(input);
                        keyEvent.consume();
                    }
                    if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN) {
                        history.moveForward(input);
                        keyEvent.consume();
                    }
                }
            }
        });

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent componentEvent) {
                input.requestFocus();
            }
        });

//        thread = new KahluaThread(terminal.getPrintStream());
    }

    private String withNewline(String text) {
        if (text.endsWith("\n")) {
            return text;
        }
        return text + "\n";
    }

    private boolean isControl(KeyEvent keyEvent) {
        return (keyEvent.getModifiers() & KeyEvent.CTRL_MASK) != 0;
    }

    private void setStatus(final String text) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    status.setText(text);
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public Runnable getRunnableExecution(final String text) {
        return new Runnable() {
            @Override
            public void run() {
                setStatus("[running...]");
                try {
                    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                    // evaluate Lua code from String
                    final PrintStream stdout = _G.STDOUT;
                    _G.STDOUT = new PrintStream(outputStream);
                    _G.get("load").call(LuaValue.valueOf(text)).call();

                    print(new String(outputStream.toByteArray(), Charsets.UTF_8));
                    _G.STDOUT = stdout;

//                } catch (ScriptException e) {
//                    printError(e);
                } catch (LuaError e) {
                    printError(e);
                }
                setStatus("");
            }
        };
    }

    private void print(final String text) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    terminal.appendOutput(text + "\n");
                }
            });
        } catch (InterruptedException e1) {
            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void printError(final Exception e) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    terminal.appendError(e.getMessage() + "\n");
                }
            });
        } catch (InterruptedException e1) {
            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void execute(final String text) {
        ApplicationManager.getApplication().executeOnPooledThread((getRunnableExecution(text)));
    }

    public boolean isDone() {
        return future == null || future.isDone();
    }

    public OutputTerminal getTerminal() {
        return terminal;
    }

    public LuaScriptEngine getEngine() {
        return engine;
    }
}
