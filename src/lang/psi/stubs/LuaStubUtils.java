/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sylvanaar.idea.Lua.lang.psi.stubs;

import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: Dmitry.Krasilschikov
 * Date: 02.06.2009
 */
public class LuaStubUtils {
    public static List<Set<String>> deserializeCollectionsArray(StubInputStream dataStream) throws IOException {
        //named parameters
        final byte namedParametersSetNumber = dataStream.readByte();
        final List<Set<String>> collArray = new ArrayList<Set<String>>();

        for (int i = 0; i < namedParametersSetNumber; i++) {
            final byte curNamedParameterSetSize = dataStream.readByte();
            final String[] namedParameterSetArray = new String[curNamedParameterSetSize];

            for (int j = 0; j < curNamedParameterSetSize; j++) {
                namedParameterSetArray[j] = dataStream.readUTF();
            }
            Set<String> curSet = new HashSet<String>();
            ContainerUtil.addAll(curSet, namedParameterSetArray);
            collArray.add(curSet);
        }
        return collArray;
    }

    public static void serializeCollectionsArray(StubOutputStream dataStream,
                                                 Set<String>[] collArray) throws IOException {
        dataStream.writeByte(collArray.length);
        for (Set<String> namedParameterSet : collArray) {
            dataStream.writeByte(namedParameterSet.size());
            for (String namepParameter : namedParameterSet) {
                dataStream.writeUTF(namepParameter);
            }
        }
    }

    public static void writeStringArray(StubOutputStream dataStream, String[] array) throws IOException {
        dataStream.writeShort(array.length);
        for (String s : array) {
            dataStream.writeName(s);
        }
    }

    public static String[] readStringArray(StubInputStream dataStream) throws IOException {
        final short b = dataStream.readShort();

        final String[] annNames = new String[b > 0 ? b : 0];
        for (int i = 0; i < b; i++) {
            annNames[i] = dataStream.readName().toString();
        }
        return annNames;
    }

    public static void writeNullableString(StubOutputStream dataStream, @Nullable String typeText) throws IOException {
        dataStream.writeBoolean(typeText != null);
        if (typeText != null) {
            dataStream.writeUTFFast(typeText);
        }
    }

    @Nullable
    public static String readNullableString(StubInputStream dataStream) throws IOException {
        final boolean hasTypeText = dataStream.readBoolean();
        return hasTypeText ? dataStream.readUTFFast() : null;
    }
}
