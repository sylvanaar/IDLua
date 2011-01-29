///*
// * Copyright 2010 Jon S Akhtar (Sylvanaar)
// *
// *   Licensed under the Apache License, Version 2.0 (the "License");
// *   you may not use this file except in compliance with the License.
// *   You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// *   Unless required by applicable law or agreed to in writing, software
// *   distributed under the License is distributed on an "AS IS" BASIS,
// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *   See the License for the specific language governing permissions and
// *   limitations under the License.
// */
//
//package com.sylvanaar.idea.Lua.util;
//
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author Maxim.Manuylov
// *         Date: 11.04.2010
// */
//public class TreeNode<T> {
//    @NotNull private final T myData;
//    @Nullable private TreeNode<T> myParent = null;
//    @NotNull final private List<TreeNode<T>> myChildren = new ArrayList<TreeNode<T>>();
//
//    public TreeNode(@NotNull final T data) {
//        myData = data;
//    }
//
//    @NotNull
//    public T getData() {
//        return myData;
//    }
//
//    @NotNull
//    public List<TreeNode<T>> getChildren() {
//        return myChildren;
//    }
//
//    @Nullable
//    public TreeNode<T> getParent() {
//        return myParent;
//    }
//
//    public void addChild(@NotNull final TreeNode<T> child) {
//        child.myParent = this;
//        myChildren.add(child);
//    }
//}
