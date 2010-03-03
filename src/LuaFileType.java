package lua;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: jon
 * Date: Mar 1, 2010
 * Time: 12:00:55 AM
 * To change this template use File | Settings | File Templates.
 */

public class LuaFileType extends LanguageFileType {
    public LuaFileType() {
        super(new LuaLanguage());
    }

    public String getName() {
        return "Lua";
    }

    public String getDescription() {
        return "Lua";
    }

    public String getDefaultExtension() {
        return "lua";
    }

    public Icon getIcon() {
        return IconLoader.getIcon("/fileTypes/Lua.png");
    }
}

