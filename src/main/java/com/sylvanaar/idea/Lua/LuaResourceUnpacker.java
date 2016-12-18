package com.sylvanaar.idea.Lua;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.sylvanaar.idea.Lua.util.LuaFileUtil;

import java.io.IOException;

/**
 * Created by jon on 12/18/2016.
 */
public class LuaResourceUnpacker extends ApplicationComponent.Adapter {

    @Override
    public void initComponent() {
        VirtualFile pluginVirtualDirectory = LuaFileUtil.getPluginVirtualDirectory();

        if (pluginVirtualDirectory != null) {
            VirtualFile lib = pluginVirtualDirectory.findChild("lib");
            if (lib != null) {
                VirtualFile pluginJar = lib.findChild("IDLua.jar");

                JarFileSystem jfs = JarFileSystem.getInstance();

                VirtualFile std = jfs.findLocalVirtualFileByPath("include/stdlibrary");

                try {
                    VfsUtil.copyDirectory(this, std, pluginVirtualDirectory, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
