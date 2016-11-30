/*
 * Copyright 2016 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.options;

import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.ui.AnActionButton;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.LocalPathCellEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.List;

public class LuaInterpretersTable extends ListTableWithButtons<LuaInterpreter> {
    protected LuaInterpretersTable() {
        super();
    }

    @Override
    protected ListTableModel createListModel() {
        ColumnInfo isDefault = new BooleanColumnInfoBase<LuaInterpreter>("Default") {
            @Override
            public Boolean valueOf(LuaInterpreter interpreter) {
                return interpreter.isDefault;
            }

            @Override
            public boolean isCellEditable(LuaInterpreter interpreter) {
                return canDeleteElement(interpreter);
            }

            @Override
            public void setValue(LuaInterpreter interpreter, Boolean b) {
                if (b.equals(valueOf(interpreter))) {
                    return;
                }
                interpreter.isDefault = b;
                setModified();
            }
        };
        ColumnInfo name = new ElementsColumnInfoBase<LuaInterpreter>("Name") {
            @Override
            public String valueOf(LuaInterpreter interpreter) {
                return interpreter.name;
            }

            @Override
            public boolean isCellEditable(LuaInterpreter interpreter) {
                return canDeleteElement(interpreter);
            }

            @Override
            public void setValue(LuaInterpreter interpreter, String s) {
                if (s.equals(valueOf(interpreter))) {
                    return;
                }
                interpreter.name = s;
                setModified();
            }

            @Override
            protected String getDescription(LuaInterpreter interpreter) {
                return valueOf(interpreter);
            }
        };
        ColumnInfo path = new ElementsColumnInfoBase<LuaInterpreter>("Executable") {
            @Override
            public String valueOf(LuaInterpreter interpreter) {
                return interpreter.path;
            }

            @Override
            public boolean isCellEditable(LuaInterpreter interpreter) {
                return canDeleteElement(interpreter);
            }

            @Override
            public void setValue(LuaInterpreter interpreter, String s) {
                if (s.equals(valueOf(interpreter))) {
                    return;
                }
                interpreter.path = s;

                LuaInterpreterFinder.INSTANCE.describe(interpreter);

                setModified();
            }

            @Override
            protected String getDescription(LuaInterpreter interpreter) {
                return valueOf(interpreter);
            }

            @Nullable
            @Override
            public TableCellEditor getEditor(LuaInterpreter luaInterpreter) {
                FileChooserDescriptor chooserDescriptor = new FileChooserDescriptor(
                        true, false, false, false, false, false
                );
                LocalPathCellEditor cellEditor = new LocalPathCellEditor(null);
                cellEditor.fileChooserDescriptor(chooserDescriptor);
                return cellEditor;
            }
        };
        ColumnInfo type = new ElementsColumnInfoBase<LuaInterpreter>("Family") {
            @Override
            public String valueOf(LuaInterpreter interpreter) {
                if (interpreter.family == null)
                    return "<Not Found>";
                return interpreter.family.interpreterName;
            }

            @Override
            public boolean isCellEditable(LuaInterpreter interpreter) {
                return false;
            }

            @Override
            public void setValue(LuaInterpreter interpreter, String s) {
                if (s.equals(valueOf(interpreter))) {
                    return;
                }
                interpreter.family = LuaInterpreterFamily.findByName(s);
                setModified();
            }

            @Override
            protected String getDescription(LuaInterpreter interpreter) {
                return valueOf(interpreter);
            }
        };
        ColumnInfo version = new ElementsColumnInfoBase<LuaInterpreter>("Version") {
            @Override
            public String valueOf(LuaInterpreter interpreter) {
                return interpreter.version;
            }

            @Override
            public boolean isCellEditable(LuaInterpreter interpreter) {
                return false;
            }

            @Override
            public void setValue(LuaInterpreter interpreter, String s) {
                if (s.equals(valueOf(interpreter))) {
                    return;
                }
                interpreter.version = s;
                setModified();
            }

            @Override
            protected String getDescription(LuaInterpreter interpreter) {
                return valueOf(interpreter);
            }
        };

        return new ListTableModel((new ColumnInfo[]{
                isDefault, name, path, type, version
        })) {
            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                super.setValueAt(aValue, rowIndex, columnIndex);

                if (columnIndex == 0) {
                    if (aValue != null && Boolean.TRUE.equals(aValue)) {
                        int i = 0;
                        for (LuaInterpreter interpreter : getTableView().getItems()) {
                            if (i != rowIndex && interpreter.isDefault) {
                                interpreter.isDefault = false;
                                this.fireTableCellUpdated(i, 0);
                            }
                            i++;
                        }
                    }
                }
                else if (columnIndex == 2) {
                    // Manually redraw the derived values
                    this.fireTableCellUpdated(rowIndex, 3);
                    this.fireTableCellUpdated(rowIndex, 4);
                }

                setModified();
            }
        };
    }

    @Override
    protected LuaInterpreter createElement() {
        return new LuaInterpreter();
    }

    @Override
    protected boolean isEmpty(LuaInterpreter element) {
        return element==null || element.name == null || element.name.isEmpty()
                || element.path == null || element.path.isEmpty();
    }

    @Override
    protected LuaInterpreter cloneElement(LuaInterpreter interpreter) {
        return new LuaInterpreter(interpreter);
    }

    @Override
    protected boolean canDeleteElement(LuaInterpreter selection) {
        return true;
    }

    @NotNull
    protected AnActionButton[] createExtraActions() {
        return new AnActionButton[] {
                new AnActionButton("Re-scan", AllIcons.Actions.ForceRefresh) {

                    protected LuaInterpreter findByPath(String path) {
                        for (LuaInterpreter element : getElements())
                            if (element.path.equals(path))
                                return element;
                        return null;
                    }

                    protected void update(LuaInterpreter target, LuaInterpreter source) {
                        target.family = source.family;
                        target.path = source.path;
                        target.version = source.version;
                    }

                    @Override
                    public void actionPerformed(AnActionEvent e) {
                        LuaInterpreterFinder finder = new LuaInterpreterFinder();
                        List<LuaInterpreter> interpreters = finder.findInterpreters();

                        for (LuaInterpreter interpreter : interpreters) {
                            LuaInterpreter original = findByPath(interpreter.path);
                            if (original == null) {
                                List<LuaInterpreter> elements = getElements();
                                if (0 == elements.size())
                                    interpreter.isDefault = Boolean.TRUE;
                                elements.add(interpreter);
                            } else
                                update(original, interpreter);
                        }

                        if (interpreters.size() != 0) {
                            getTableView().getTableViewModel().setItems(getElements());
                            setModified();
                        }
                    }
                }
        };
    }

    protected static abstract class BooleanColumnInfoBase<T> extends ColumnInfo<T, Boolean> {
        protected BooleanColumnInfoBase(String name) {
            super(name);
        }

        public Class<?> getColumnClass() {
            return Boolean.class;
        }

        @Override
        public TableCellRenderer getRenderer(T element) {
            return null;
        }
    }
}
