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
package com.sylvanaar.idea.errorreporting;

import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NonNls;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 23, 2010
 * Time: 1:20:24 PM
 */
public class BugzReport extends ErrorReportSubmitter {
    @NonNls
    private static final String SERVER_URL = "https://sylvanaar.fogbugz.com/scoutSubmit.asp";

    private String userName="autosubmit";
    private String project = "Lua for IDEA";
    private String area = "Main";
    private String description = null;
    private String extraInformation = null;

    public String submit() {
        if (this.description == null || this.description.length() == 0) throw new RuntimeException("Description");
        if (this.project == null || this.project.length() == 0) throw new RuntimeException("Project");
        if (this.area == null || this.area.length() == 0) throw new RuntimeException("Area");

        String response = "";
        
        //Create Post String
        String data;
        try {
            data = URLEncoder.encode("Description", "UTF-8") + "=" + URLEncoder.encode(description, "UTF-8");
            data += "&" + URLEncoder.encode("ScoutProject", "UTF-8") + "=" + URLEncoder.encode(project, "UTF-8");
            data += "&" + URLEncoder.encode("ScoutArea", "UTF-8") + "=" + URLEncoder.encode(area, "UTF-8");
            data += "&" + URLEncoder.encode("ScoutUserName", "UTF-8") + "=" + URLEncoder.encode(userName, "UTF-8");
            if (extraInformation != null)
                data += "&" + URLEncoder.encode("Extra", "UTF-8") + "=" + URLEncoder.encode(extraInformation, "UTF-8");

            // Send Data To Page
            URL url = new URL(SERVER_URL);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // Get The Response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                response += line;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    
    @Override
    public String getReportActionText() {
        return "Action Text";
    }

    @Override
    public SubmittedReportInfo submit(IdeaLoggingEvent[] ideaLoggingEvents, Component component) {
        // show modal error submission dialog
        PluginErrorSubmitDialog dialog = new PluginErrorSubmitDialog(component);
        dialog.prepare();
        dialog.show();

        // submit error to server if user pressed SEND
        int code = dialog.getExitCode();
        if (code == DialogWrapper.OK_EXIT_CODE) {
            dialog.persist();
            String description = dialog.getDescription();
            String user = dialog.getUser();
            return submit(ideaLoggingEvents, description, user, component);
        }

        // otherwise do nothing
        return null;
    }

    private SubmittedReportInfo submit(IdeaLoggingEvent[] ideaLoggingEvents, String description, String user, Component component) {
         this.description = ideaLoggingEvents[0].getThrowable().getMessage();
         //this.userName = user;

         if (user == null) user = "<none>";
         if (description == null) description = "<none>";

         this.extraInformation = "\n\nDescription: " + description + "\n\n" + "User: " + user;

         for (IdeaLoggingEvent e : ideaLoggingEvents)
                 this.extraInformation += "\n\n" + e.toString();

         submit();

         return new SubmittedReportInfo("", "", SubmittedReportInfo.SubmissionStatus.NEW_ISSUE);
    }
}
