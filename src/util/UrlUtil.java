/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.util;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.io.UrlConnectionUtil;
import com.intellij.util.net.HttpConfigurable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 2/27/11
 * Time: 8:40 PM
 */
public final class UrlUtil {
    private UrlUtil() {
    }

    private static
    @NonNls
    final String JAR_PROTOCOL = "jar:";

    @Nullable
    public static Reader getReaderByUrl(final String surl, final HttpConfigurable httpConfigurable, final ProgressIndicator pi) throws IOException {
        if (surl.startsWith(JAR_PROTOCOL)) {
            VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(BrowserUtil.getDocURL(surl));

            if (file == null) {
                return null;
            }

            return new StringReader(VfsUtil.loadText(file));
        }

        URL url = BrowserUtil.getURL(surl);
        if (url == null) {
            return null;
        }
        httpConfigurable.prepareURL(url.toString());
        final URLConnection urlConnection = url.openConnection();
        final String contentEncoding = urlConnection.getContentEncoding();
        final InputStream inputStream =
                pi != null ? UrlConnectionUtil.getConnectionInputStreamWithException(urlConnection, pi) : urlConnection.getInputStream();
        //noinspection IOResourceOpenedButNotSafelyClosed
        return contentEncoding != null ? new InputStreamReader(inputStream, contentEncoding) : new InputStreamReader(inputStream);
    }


    public interface FetchedUrlBuilder {
        void buildFromStream(String surl, Reader input, StringBuffer result) throws IOException;
    }

    public static FetchedUrlBuilder IDENTITY_BUILDER = new FetchedUrlBuilder() {

        @Override
        public void buildFromStream(String surl, Reader input, StringBuffer result) throws IOException {
            BufferedReader reader = new BufferedReader(input);

            for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                result.append(line);
                result.append("\r\n");
            }
        }
    };


    public static class UrlFetcher implements Runnable {
        private static boolean ourFree = true;
        private final StringBuffer data = new StringBuffer();
        private final String surl;
        private final UrlUtil.FetchedUrlBuilder myBuilder;
        private final Exception[] myExceptions = new Exception[1];
        private final HttpConfigurable myHttpConfigurable;

        public UrlFetcher(final String surl, FetchedUrlBuilder builder) {
            this.surl = surl;
            myBuilder = builder;
            ourFree = false;
            myHttpConfigurable = HttpConfigurable.getInstance();
        }

        public UrlFetcher(final String surl) {
            this(surl, IDENTITY_BUILDER);
        }

        public static boolean isFree() {
            return ourFree;
        }

        public String getData() {
            return data.toString();
        }

        public void run() {
            try {
                if (surl == null) {
                    return;
                }

                Reader stream = null;
                try {
                    stream = getReaderByUrl(surl, myHttpConfigurable, new ProgressIndicatorBase());
                } catch (ProcessCanceledException e) {
                    return;
                } catch (IOException e) {
                    myExceptions[0] = e;
                }

                if (stream == null) {
                    return;
                }

                try {
                    myBuilder.buildFromStream(surl, stream, data);
                } catch (final IOException e) {
                    myExceptions[0] = e;
                } finally {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        myExceptions[0] = e;
                    }
                }
            } finally {
                ourFree = true;
            }
        }

        public Exception getException() {
            return myExceptions[0];
        }

        public void cleanup() {
            myExceptions[0] = null;
        }
    }
}
