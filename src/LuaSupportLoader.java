/**
 * Created by IntelliJ IDEA.
 * User: jon
 * Date: Feb 28, 2010
 * Time: 11:51:49 PM
 * To change this template use File | Settings | File Templates.
 */
package lua;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.LanguageFileType;

/**
 * Created by IntelliJ IDEA.
 * User: max
 * Date: Jan 27, 2005
 * Time: 6:00:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class LuaSupportLoader implements ApplicationComponent {
    public static final LanguageFileType LUA = new LuaFileType();

    public void initComponent() {
        ApplicationManager.getApplication().runWriteAction(
                new Runnable() {
                    public void run() {
                        FileTypeManager.getInstance().registerFileType(LUA, new String[]{"lua"});
                    }
                }
        );
    }

    public void disposeComponent() {
    }

    public String getComponentName() {
        return "lua support loader";
    }
}


}
