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

import com.intellij.diagnostic.DiagnosticBundle;
import com.intellij.diagnostic.ErrorReportConfigurable;
import com.intellij.diagnostic.IdeErrorsDialog;
import com.intellij.diagnostic.ReportMessages;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.DataManager;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
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

    private static final Logger log              = Logger.getInstance(YouTrackBugReporter.class.getName());
    @NonNls
    private static final String SERVER_URL       = "http://sylvanaar.myjetbrains.com/youtrack/";
    private static final String SERVER_REST_URL  = SERVER_URL + "rest/";
    private static final String SERVER_ISSUE_URL = SERVER_REST_URL + "issue";
    private static final String LOGIN_URL        = SERVER_REST_URL + "user/login";


    private              String userName         = "autosubmit";
    private              String project          = "IDLua";
    private              String area             = "Main";
    private              String description      = null;
    private              String extraInformation = "";
    private              String email            = null;
    private              String affectedVersion  = null;
    private static final String DEFAULT_RESPONSE = "Thank you for your report.";

    private final CookieManager cookieManager = new CookieManager();


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
            URL url = new URL(LOGIN_URL);
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

            log.info(response);

            response = "";

            // getting cookies:

            // setting cookies
            cookieManager.storeCookies(conn);

            // project=TST&assignee=beto&summary=new issue&description=description of new issue
            // #&priority=show-stopper&type=feature&subsystem=UI&state=Reopened&affectsVersion=2.0,
            // 2.0.1&fixedVersions=2.0&fixedInBuild=2.0.1
            // POST /rest/issue?{project}&{assignee}&{summary}&{description}&{priority}&{type}&{subsystem}&{state
            // }&{affectsVersion}&{fixedVersions}&{attachments}&{fixedInBuild}

            data = URLEncoder.encode("project", "UTF-8") + "=" + URLEncoder.encode(project, "UTF-8");
            data += "&" + URLEncoder.encode("assignee", "UTF-8") + "=" + URLEncoder.encode("Unassigned", "UTF-8");
            data += "&" + URLEncoder.encode("summary", "UTF-8") + "=" + URLEncoder.encode(description, "UTF-8");
            data += "&" + URLEncoder.encode("description", "UTF-8") + "=" +
                    URLEncoder.encode(extraInformation, "UTF-8");
            data += "&" + URLEncoder.encode("priority", "UTF-8") + "=" + URLEncoder.encode("4", "UTF-8");
            data += "&" + URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("Exception", "UTF-8");

            if (this.affectedVersion != null) data += "&" + URLEncoder.encode("affectsVersion", "UTF-8") + "=" +
                                                      URLEncoder.encode(this.affectedVersion, "UTF-8");

            // Send Data To Page
            url = new URL(SERVER_ISSUE_URL);

            conn = url.openConnection();
            conn.setDoOutput(true);
            cookieManager.setCookies(conn);

            wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // Get The Response
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            line = "";
            while ((line = rd.readLine()) != null) {
                response += line;
            }



        } catch (Exception e) {
            log.info("Error creating issue", e);
        }

        return response;
    }

    // POST /rest/issue/{issue}/execute?{command}&{comment}&{group}&{disableNotifications}&{runAs}
    private void runCommand(String issueID, String command) {
        try {
            log.debug(String.format("Run Command <%s> on issue <%s>", command, issueID));
            URL url = new URL(SERVER_ISSUE_URL + "/" + issueID + "/execute");

            String data = URLEncoder.encode("command", "UTF-8") + "=" + URLEncoder.encode(command, "UTF-8");
            data += "&" + URLEncoder.encode("disableNotifications", "UTF-8") + "=" + URLEncoder.encode("true", "UTF-8");

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            cookieManager.setCookies(conn);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            
            // Get The Response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            String response = "";
            while ((line = rd.readLine()) != null) {
                response += line;
            }
            
            log.debug(response);
            
        } catch (IOException e) {
            log.info("Command Failed", e);
        }
    }
    

    @Override
    public String getReportActionText() {
        return "Report Error";
    }

    @Override
    public SubmittedReportInfo submit(IdeaLoggingEvent[] ideaLoggingEvents, Component component) {

        return submit(ideaLoggingEvents, this.description,
                StringUtil.notNullize(ErrorReportConfigurable.getInstance().ITN_LOGIN, "<anonymous>"), component);
    }

    @Override
    public void submitAsync(IdeaLoggingEvent[] events, String additionalInfo, Component parentComponent,
                            Consumer<SubmittedReportInfo> consumer) {

        this.description = additionalInfo;
        super.submitAsync(events, additionalInfo, parentComponent, consumer);
    }

    private SubmittedReportInfo submit(IdeaLoggingEvent[] ideaLoggingEvents, String description, String user,
                                       Component component) {

        final IdeaLoggingEvent ideaLoggingEvent = ideaLoggingEvents[0];
        final String throwableText = ideaLoggingEvent.getThrowableText();
        this.description = throwableText.substring(0, Math.min(Math.max(80, throwableText.length()), 80));
        this.email = user;

        @NonNls StringBuilder descBuilder = new StringBuilder();

        String platformBuild = ApplicationInfo.getInstance().getBuild().asString();

        descBuilder.append("Platform Version: ").append(platformBuild).append('\n');

        Throwable t = ideaLoggingEvent.getThrowable();
        if (t != null) {
            final PluginId pluginId = IdeErrorsDialog.findPluginId(t);
            if (pluginId != null) {
                final IdeaPluginDescriptor ideaPluginDescriptor = PluginManager.getPlugin(pluginId);
                if (ideaPluginDescriptor != null && !ideaPluginDescriptor.isBundled()) {
                    descBuilder.append("Plugin ").append(ideaPluginDescriptor.getName()).append(" version: ")
                               .append(ideaPluginDescriptor.getVersion()).append("\n");
                    this.affectedVersion = ideaPluginDescriptor.getVersion();
                }
            }
        }

        if (description == null) description = "<none>";

        descBuilder.append("\n\nDescription: ").append(description);

        for (IdeaLoggingEvent e : ideaLoggingEvents)
            descBuilder.append("\n\n").append(e.toString());

        this.extraInformation = descBuilder.toString();

        String result = submit();
        log.info("Error submitted, response: " + result);

        if (result == null) return new SubmittedReportInfo(SERVER_ISSUE_URL, "", FAILED);

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

        if (ResultString == null) return new SubmittedReportInfo(SERVER_ISSUE_URL, "", FAILED);


        final SubmittedReportInfo reportInfo = new SubmittedReportInfo(SERVER_URL + "issue/" + ResultString, ResultString, status);

        final DataContext dataContext = DataManager.getInstance().getDataContext(component);
        final Project project = PlatformDataKeys.PROJECT.getData(dataContext);

        /* Now try to set the autosubmit user using a custom command */
        if (user != null)
            runCommand(ResultString, "Autosubmit User " + user);

        ApplicationManager.getApplication().invokeLater(new Runnable() {
          @Override
          public void run() {
            StringBuilder text = new StringBuilder("<html>");
            final String url = IdeErrorsDialog.getUrl(reportInfo, true);
            IdeErrorsDialog.appendSubmissionInformation(reportInfo, text, url);
            text.append(".");
            if (reportInfo.getStatus() != SubmittedReportInfo.SubmissionStatus.FAILED) {
              text.append("<br/>").append(DiagnosticBundle.message("error.report.gratitude"));
            }
            text.append("</html>");
            NotificationType type = reportInfo.getStatus() == SubmittedReportInfo.SubmissionStatus.FAILED
                                    ? NotificationType.ERROR
                                    : NotificationType.INFORMATION;
            NotificationListener listener = url != null ? new NotificationListener() {
              @Override
              public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
                BrowserUtil.launchBrowser(url);
                notification.expire();
              }
            } : null;
            ReportMessages.GROUP.createNotification(ReportMessages.ERROR_REPORT,
                                                    text.toString(),
                                                    type, listener).notify(project);
          }
        });

        return reportInfo;
    }
}
