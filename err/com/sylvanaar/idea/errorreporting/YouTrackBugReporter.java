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

import static com.intellij.openapi.diagnostic.SubmittedReportInfo.SubmissionStatus.FAILED;
import static com.intellij.openapi.diagnostic.SubmittedReportInfo.SubmissionStatus.NEW_ISSUE;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Oct 19, 2010
 * Time: 11:35:35 AM
 */
public class YouTrackBugReporter extends ErrorReportSubmitter {
    private static final String USER = "IDEA";

    private static final Logger log = Logger.getInstance(YouTrackBugReporter.class.getName());
    @NonNls
    private static final String SERVER_URL = "http://192.168.128.199:8082/rest/issue";

    private String userName = "autosubmit";
    private String project = "Lua";
    private String area = "Main";
    private String description = null;
    private String extraInformation = "";
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
            data = URLEncoder.encode("login", "UTF-8") + "=" + URLEncoder.encode("autosubmit", "UTF-8");
            data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode("root", "UTF-8");
            // Send Data To Page
            URL url = new URL("http://192.168.128.199:8082/rest/user/login");
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


            CookieManager cm = new CookieManager();

            // getting cookies:

            // setting cookies
            cm.storeCookies(conn);


// project=TST&assignee=beto&summary=new issue&description=description of new issue #&priority=show-stopper&type=feature&subsystem=UI&state=Reopened&affectsVersion=2.0,2.0.1&fixedVersions=2.0&fixedInBuild=2.0.1
            // POST /rest/issue?{project}&{assignee}&{summary}&{description}&{priority}&{type}&{subsystem}&{state}&{affectsVersion}&{fixedVersions}&{attachments}&{fixedInBuild}

            data = URLEncoder.encode("project", "UTF-8") + "=" + URLEncoder.encode(project, "UTF-8");
            data += "&" + URLEncoder.encode("assignee", "UTF-8") + "=" + URLEncoder.encode("Unassigned", "UTF-8");
            data += "&" + URLEncoder.encode("summary", "UTF-8") + "=" + URLEncoder.encode(description, "UTF-8");
            data += "&" + URLEncoder.encode("description", "UTF-8") + "=" + URLEncoder.encode(extraInformation, "UTF-8");
            data += "&" + URLEncoder.encode("priority", "UTF-8") + "=" + URLEncoder.encode("4", "UTF-8");
            data += "&" + URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("Exception", "UTF-8");
            //  data += "&" + URLEncoder.encode("subsystem", "UTF-8") + "=" + URLEncoder.encode("", "UTF-8");
            //  data += "&" + URLEncoder.encode("state", "UTF-8") + "=" + URLEncoder.encode("", "UTF-8");
            //   data += "&" + URLEncoder.encode("affectsVersion", "UTF-8") + "=" + URLEncoder.encode("", "UTF-8");
            //   data += "&" + URLEncoder.encode("fixedVersions", "UTF-8") + "=" + URLEncoder.encode("", "UTF-8");
            //   data += "&" + URLEncoder.encode("attachments", "UTF-8") + "=" + URLEncoder.encode("", "UTF-8");
            //   data += "&" + URLEncoder.encode("fixedInBuild", "UTF-8") + "=" + URLEncoder.encode("", "UTF-8");

//            data += "&" + URLEncoder.encode("ScoutArea", "UTF-8") + "=" + URLEncoder.encode(area, "UTF-8");
//            data += "&" + URLEncoder.encode("ScoutUserName", "UTF-8") + "=" + URLEncoder.encode(userName, "UTF-8");
////            data += "&" + URLEncoder.encode("ScoutDefaultMessage", "UTF-8") + "=" + URLEncoder.encode(DEFAULT_RESPONSE, "UTF-8");
            //if (extraInformation != null)
            //   data += "&" + URLEncoder.encode("summary", "UTF-8") + "=" + URLEncoder.encode(description, "UTF-8");
//            if (email != null)
//                data += "&" + URLEncoder.encode("Email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");


            // Send Data To Page
            url = new URL(SERVER_URL);

            conn = url.openConnection();
            conn.setDoOutput(true);
            cm.setCookies(conn);

//                    String myCookie = "userId=igbrown";


            wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // Get The Response
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            line =  "";
            while ((line = rd.readLine()) != null) {
                response += line;
            }


        } catch (Exception
                e) {
            e.printStackTrace();
        }

        return response;
    }


    @Override
    public String getReportActionText
            () {
        return "Action Text";
    }

    @Override
    public SubmittedReportInfo submit
            (IdeaLoggingEvent[] ideaLoggingEvents, Component
                    component) {
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

    private SubmittedReportInfo submit
            (IdeaLoggingEvent[] ideaLoggingEvents, String
                    description, String
                    user, Component
                    component) {
        this.description = ideaLoggingEvents[0].getThrowableText().substring(0, Math.min(Math.max(80, ideaLoggingEvents[0].getThrowableText().length()), 80));
        this.email = user;

        if (user == null) user = "<none>";
        if (description == null) description = "<none>";

        this.extraInformation = "\n\nDescription: " + description + "\n\n" + "User: " + user;

        for (IdeaLoggingEvent e : ideaLoggingEvents)
            this.extraInformation += "\n\n" + e.toString();

        String result = submit();
        log.info("Error submitted, response: " + result);

        if (result == null)
            return new SubmittedReportInfo(SERVER_URL, "", FAILED);

        String ResultString = null;
        try {
            Pattern regex = Pattern.compile("id=\"([^\"]+)\"", Pattern.DOTALL | Pattern.MULTILINE);
            Matcher regexMatcher = regex.matcher(result);
            if (regexMatcher.find()) {
                ResultString = regexMatcher.group(1);
            }
        } catch (PatternSyntaxException ex) {
            // Syntax error in the regular expression
        }



        SubmittedReportInfo.SubmissionStatus status = NEW_ISSUE;

        if (ResultString == null)
             return new SubmittedReportInfo(SERVER_URL, "", FAILED);
//        else {
//            if (ResultString.trim().length() > 0)
//                status = DUPLICATE;
//        }

        return new SubmittedReportInfo("http://192.168.128.199:8082/issue/"+ResultString, "Submitted", status);
    }
}
