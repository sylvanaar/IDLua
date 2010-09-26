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
import com.intellij.openapi.diagnostic.Logger;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.intellij.openapi.diagnostic.SubmittedReportInfo.SubmissionStatus.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 23, 2010
 * Time: 1:20:24 PM
 */
public class BugzReport extends ErrorReportSubmitter {
    private static final Logger log = Logger.getInstance(BugzReport.class.getName());
    @NonNls
    private static final String SERVER_URL = "https://sylvanaar.fogbugz.com/scoutSubmit.asp";

    private String userName = "autosubmit";
    private String project = "Lua for IDEA";
    private String area = "Main";
    private String description = null;
    private String extraInformation = null;
    private String email = null;
    private static final String DEFAULT_RESPONSE = "Thank you for your report.";

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
//            data += "&" + URLEncoder.encode("ScoutDefaultMessage", "UTF-8") + "=" + URLEncoder.encode(DEFAULT_RESPONSE, "UTF-8");
            if (extraInformation != null)
                data += "&" + URLEncoder.encode("Extra", "UTF-8") + "=" + URLEncoder.encode(extraInformation, "UTF-8");
            if (email != null)
                data += "&" + URLEncoder.encode("Email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");


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
        this.description = ideaLoggingEvents[0].getThrowableText();
        this.email = user;

        if (user == null) user = "<none>";
        if (description == null) description = "<none>";

        this.extraInformation = "\n\nDescription: " + description + "\n\n" + "User: " + user;

        for (IdeaLoggingEvent e : ideaLoggingEvents)
            this.extraInformation += "\n\n" + e.toString();

        String result = submit();
        log.error("Error submitted, response: " + result);


        String resultType = null;
        String resultText = null;
        try {
            Pattern regex = Pattern.compile("<([A-Z][A-Z0-9]*)[^>]*>(.*?)</\\1>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            Matcher regexMatcher = regex.matcher(result);
            if (regexMatcher.find()) {
                resultType = regexMatcher.group(1);
                resultText = regexMatcher.group(2);
            }
        } catch (PatternSyntaxException ex) {
            // Syntax error in the regular expression
        }


        SubmittedReportInfo.SubmissionStatus status = NEW_ISSUE;

        if (resultType.equals("Error"))
            status = FAILED;
        else {
            if (resultText.trim().length() > 0)
                status = DUPLICATE;
        }

        return new SubmittedReportInfo(SERVER_URL, resultText, status);
    }
}
