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

package com.sylvanaar.idea.Lua.kahlua;

import jsyntaxpane.lexers.LuaLexer;
import se.krka.kahlua.converter.KahluaConverterManager;
import se.krka.kahlua.integration.LuaCaller;
import se.krka.kahlua.integration.LuaReturn;
import se.krka.kahlua.integration.expose.LuaJavaClassExposer;

import se.krka.kahlua.j2se.interpreter.History;
import se.krka.kahlua.j2se.interpreter.InputTerminal;
import se.krka.kahlua.j2se.interpreter.OutputTerminal;
import se.krka.kahlua.j2se.interpreter.autocomplete.AutoComplete;
import se.krka.kahlua.j2se.interpreter.jsyntax.JSyntaxUtil;
import se.krka.kahlua.j2se.interpreter.jsyntax.KahluaKit;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 19, 2010
 * Time: 4:29:54 PM
 */
public class KahluaInterpreter extends JPanel {

    private final KahluaThread thread;
    private final OutputTerminal terminal;
    private final JLabel status = new JLabel("");

    private final History history = new History();
    private final ExecutorService executors = Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
    private Future<?> future;

    final KahluaConverterManager manager = new KahluaConverterManager();
    final LuaCaller caller = new LuaCaller(manager);
    final LuaJavaClassExposer exposer;

    public KahluaInterpreter(Platform platform, KahluaTable env) {
        super(new BorderLayout());

        JSyntaxUtil.setup();

        exposer = new LuaJavaClassExposer(manager, platform, env);
        exposer.exposeGlobalFunctions(this);

        final InputTerminal input = new InputTerminal(Color.BLACK);

        final KahluaKit kit = new KahluaKit(new LuaLexer());
        JSyntaxUtil.installSyntax(input, true, kit);
        new AutoComplete(input, platform, env);


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

        thread = new KahluaThread(terminal.getPrintStream(), platform, env);
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


    public Runnable getRunnableExecution(final String text) {
        return new Runnable() {
            @Override
            public void run() {
                status.setText("[running...]");
                try {
                    LuaClosure luaClosure = smartCompile(text);
                    LuaReturn result = caller.protectedCall(thread, luaClosure);
                    if (result.isSuccess()) {
                        for (Object o : result) {
                            terminal.appendOutput(KahluaUtil.tostring(o, thread)+"\n");
                        }
                    } else {
                        terminal.appendError(result.getErrorString()+"\n");
                        terminal.appendError(result.getLuaStackTrace()+"\n");
                        result.getJavaException().printStackTrace(System.err);
                    }
                } catch (IOException e) {
                    e.printStackTrace(terminal.getPrintStream());
                } catch (RuntimeException e) {
                    terminal.appendError(e.getMessage()+"\n");
                }
                status.setText("");
            }
        };
    }

    public void execute(final String text) {
        future = executors.submit(getRunnableExecution(text));
    }

    private LuaClosure smartCompile(String text) throws IOException {
        LuaClosure luaClosure;
        try {
            luaClosure = LuaCompiler.loadstring("return " + text, "interpreter", thread.getEnvironment());
        } catch (KahluaException e) {
            // Ignore it and try without "return "
            luaClosure = LuaCompiler.loadstring(text, "interpreter", thread.getEnvironment());
        }
        return luaClosure;
    }

    public boolean isDone() {
        return future == null || future.isDone();
    }

    public KahluaThread getThread() {
        return thread;
    }

    public OutputTerminal getTerminal() {
        return terminal;
    }
}
