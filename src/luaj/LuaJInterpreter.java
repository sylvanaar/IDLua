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

import com.intellij.openapi.application.*;
import jsyntaxpane.lexers.*;
import org.luaj.vm2.script.*;
import se.krka.kahlua.j2se.interpreter.*;
import se.krka.kahlua.j2se.interpreter.jsyntax.*;

import javax.script.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;

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
    private Future<?> future;
    LuaScriptEngine engine;

    public LuaJInterpreter() {
        super(new BorderLayout());

        JSyntaxUtil.setup();

        // create a Lua engine
        engine = new LuaScriptEngine();

        final InputTerminal input = new InputTerminal(Color.BLACK);

        final KahluaKit kit = new KahluaKit(new LuaLexer());
        JSyntaxUtil.installSyntax(input, true, kit);
//        new AutoComplete(input, platform, env);

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

        this.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
            }

            @Override
            public void componentMoved(ComponentEvent componentEvent) {
            }

            @Override
            public void componentShown(ComponentEvent componentEvent) {
                input.requestFocus();
            }

            @Override
            public void componentHidden(ComponentEvent componentEvent) {
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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                status.setText(text);
            }
        });
    }



    public Runnable getRunnableExecution(final String text) {
        return new Runnable() {
            @Override
            public void run() {
                setStatus("[running...]");
                try {
                    // evaluate Lua code from String
                    engine.eval(text);
                } catch (ScriptException e) {
                    terminal.appendError(e.getMessage() + "\n");
                }
                setStatus("");
            }
        };
    }

    public void execute(final String text) {
        ApplicationManager.getApplication().executeOnPooledThread((getRunnableExecution(text)));
    }

//    private LuaClosure smartCompile(String text) throws IOException {
//        LuaClosure luaClosure;
//        try {
//            luaClosure = LuaCompiler.loadstring("return " + text, "interpreter", thread.getEnvironment());
//        } catch (KahluaException e) {
//            // Ignore it and try without "return "
//            luaClosure = LuaCompiler.loadstring(text, "interpreter", thread.getEnvironment());
//        }
//        return luaClosure;
//    }

    public boolean isDone() {
        return future == null || future.isDone();
    }

//    public KahluaThread getThread() {
//        return thread;
//    }

    public OutputTerminal getTerminal() {
        return terminal;
    }
}
