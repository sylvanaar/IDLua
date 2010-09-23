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
//
//    private static final String ERROR_SUBMITTER_PROPERTIES_PATH = "errorReporter.properties";
//    @NonNls
//    private static final String PLUGIN_ID_PROPERTY_KEY = "plugin.id";
//    @NonNls
//    private static final String PLUGIN_NAME_PROPERTY_KEY = "plugin.name";
//    @NonNls
//    private static final String PLUGIN_VERSION_PROPERTY_KEY = "plugin.version";
//    @NonNls
//    private static final String EMAIL_TO_PROPERTY_KEY = "email.to";
//    @NonNls
//    private static final String EMAIL_CC_PROPERTY_KEY = "email.cc";
//    @NonNls
//    private static final String SERVER_PROPERTY_KEY = "server.address";


    private String userName=null;
    private String project = "Lua for IDEA";
    private String area = "Main";
    private String description = null;
    private String extraInformation = null;
//    private String customerEmail = null;
//    private boolean forceNewBug = false;
//    private String fogBugzUrl = SERVER_URL;
//    private String defaultMsg = null;


    /* BugReport
        Example:
        BugReport rep = new BugReport("http://localhost/fogbugz/scoutSubmit.asp", "Test User");
        rep.Description = "Problem running program";
        rep.Email = "someuser@domain.com";
        rep.Submit();
    */

    public BugzReport(String url, String username) {
        if (url == null || url.length() == 0) throw new RuntimeException("url");
        if (username == null || username.length() == 0) throw new RuntimeException("username");
//        fogBugzUrl = url;
        userName = username;
    }


    // Submit: Submits a new bug report to the FogBugz submission page, which in turn puts it into the database

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
         this.description = description;
         this.userName = user;

         this.extraInformation = ideaLoggingEvents.toString();

         return new SubmittedReportInfo("", "test", SubmittedReportInfo.SubmissionStatus.NEW_ISSUE);
    }


//        // Prepare request
//        URLConnection req = WebRequest.Create(fogBugzUrl);
//        req.ContentType = "application/x-www-form-urlencoded";
//        req.Method = "POST";
//
//        string parameters = "";
//        parameters += "Description=" + HttpUtility.UrlEncode(this.description);
//        if (this.extraInformation != null && this.extraInformation.Length > 0)
//            parameters += "&Extra=" + HttpUtility.UrlEncode(this.extraInformation);
//        if (this.customerEmail != null && this.customerEmail.Length > 0)
//            parameters += "&Email=" + HttpUtility.UrlEncode(this.customerEmail);
//        parameters += "&ScoutUserName=" + HttpUtility.UrlEncode(this.userName);
//        parameters += "&ScoutProject=" + HttpUtility.UrlEncode(this.project);
//        parameters += "&ScoutArea=" + HttpUtility.UrlEncode(this.area);
//        parameters += "&ForceNewBug=" + (this.forceNewBug ? "1" : "0");
//
//        byte[] bytes = System.Text.Encoding.ASCII.GetBytes(parameters);
//        req.ContentLength = bytes.Length;
//        Stream os = req.GetRequestStream();
//        os.Write(bytes, 0, bytes.Length);
//        os.Close();
//
//        WebResponse resp = req.GetResponse();
//        if (resp == null) return null;
//        StreamReader sr = new StreamReader(resp.GetResponseStream());
//
//        string responseText = sr.ReadToEnd().Trim();
//        responseText = ParseResult(responseText);
//        return (responseText == "") ? this.defaultMsg : responseText;
//    }
//
//
//    /* SetException
//             This method will copy information from the specified exception object into the BugReport
//             object, specifying how version numbers will be formatted.
//
//             ex: The Exception object to copy information from. This parameter cannot be null or an
//             ArgumentNullException exception will be thrown.
//
//             versionFormat: A string used to format the version number. This string will be passed to String.Format,
//             and the four parameters given will be major, minor, build, and revision version numbers. This parameter
//             cannot be null or an ArgumentNullException exception will be thrown.
//
//             addUserAndMachineInformation: Set to true if you want the method to add information like machine name,
//             username, ip address and so on; otherwise, set to false.
//         */
//
//    public void SetException(Exception ex, bool addUserAndMachineInformation, string versionFormat) {
//        if (ex == null) throw new ArgumentNullException("ex");
//        if (versionFormat == null || versionFormat.Length == 0) throw new ArgumentNullException("versionFormat");
//
//        this.description = GetExceptionDescription(ex, versionFormat);
//
//        StringBuilder extra = new StringBuilder();
//        if (addUserAndMachineInformation) {
//            extra.AppendFormat(new System.Resources.ResourceManager(typeof(BugReport)).GetString("Username") + Environment.NewLine, Environment.UserName);
//            extra.AppendFormat(new System.Resources.ResourceManager(typeof(BugReport)).GetString("MachineName") + Environment.NewLine, Environment.MachineName);
//            extra.AppendFormat(new System.Resources.ResourceManager(typeof(BugReport)).GetString("IPAddress") + Environment.NewLine, System.Net.Dns.Resolve(Environment.MachineName).AddressList[0].ToString());
//            extra.Append(Environment.NewLine);
//        }
//
//        string prefix = "";
//
//        extra.Append(new System.Resources.ResourceManager(typeof(BugReport)).GetString("Stacktrace") + Environment.NewLine);
//        foreach(string line in ex.StackTrace.Split('\n', '\r'))
//        {
//            if (line != null && line.Length > 0)
//                extra.AppendFormat("{0}" + Environment.NewLine, line.Trim());
//        }
//        extra.Append(Environment.NewLine);
//
//        Regex reUnwantedProperties = new Regex(
//        @
//        "^(StackTrace|Source|TargetSite)$", RegexOptions.IgnoreCase);
//        while (ex != null) {
//            bool any = false;
//            foreach(System.Reflection.PropertyInfo pi in ex.GetType().GetProperties())
//            {
//                if (!reUnwantedProperties.Match(pi.Name).Success) {
//                    Object value = pi.GetValue(ex, new Object[]{});
//                    if (value != null) {
//                        if (IsInteger(value))
//                            extra.AppendFormat("{0}={1} (0x{1:X})" + Environment.NewLine, pi.Name, value);
//                        else
//                            extra.AppendFormat("{0}={1}" + Environment.NewLine, pi.Name, value);
//                        any = true;
//                    }
//                }
//            }
//            if (ex.InnerException != null) {
//                if (any) extra.Append(Environment.NewLine);
//                prefix = new System.Resources.ResourceManager(typeof(BugReport)).GetString("InnerException") + " ";
//            }
//            ex = ex.InnerException;
//        }
//
//        if (this.extraInformation == null || this.extraInformation.Length == 0)
//            this.extraInformation = extra.ToString();
//        else
//            this.extraInformation += Environment.NewLine + extra.ToString();
//    }
//
//
//    // AppendAssemblyList: Appends a list of assembly names and version numbers to the extra information to send in the report.
//
//    public void AppendAssemblyList() {
//        StringBuilder assemblies = new StringBuilder();
//        if (this.extraInformation == null) this.extraInformation = "";
//        else if (this.extraInformation.Length > 0) assemblies.Append(Environment.NewLine);
//
//        assemblies.Append(new System.Resources.ResourceManager(typeof(BugReport)).GetString("Assemblies") + Environment.NewLine);
//        foreach(System.Reflection.Assembly asm in AppDomain.CurrentDomain.GetAssemblies())
//        {
//            assemblies.AppendFormat("   {0}, {1}" + Environment.NewLine, asm.GetName().Name, asm.GetName().Version.ToString());
//        }
//        this.extraInformation += assemblies.ToString();
//    }
//
//    #endregion
//
//
//    #
//    region Public
//    Static Methods
//
//    /* Submit
//    This method calls the instance Submit method, which submits a new bug to FogBugz.
//
//    url: The absolute url for the FogBugz submission page. Mandatory.
//
//    username: The FogBugz user to open this bug as. Mandatory.
//
//    project: The FogBugz project to open this bug in. Mandatory.
//
//    area: The FogBugz area to open this bug in. Mandatory.
//
//    email: The email of the customer reporting the bug. Optional.
//
//    forceNewBug: If set to true, this forces FogBugz to create a new case for this bug,
//    even if a bug with the same description already exists.
//
//    defaultMsg: The message to return if no message is found for an existing duplicate case. Optional.
//
//    description: The description of the bug. If the description field matches exactly to
//    an existing bug in FogBugz, this bug submission will be APPENDED to the history of the
//    existing bug, and a new bug will NOT be created (unless you check Force New Bug below).
//    The occurrences field for this bug will then increase by 1. Mandatory.
//
//    extraInformation: Extra descriptive information. Optional.
//
//    */
//
//    public static string Submit(string url, string username, string project, string area, string email, bool forceNewBug, string defaultMsg, string description, string extraInformation) {
//        if (url == null || url.Length == 0) throw new ArgumentNullException("url");
//        if (username == null || username.Length == 0) throw new ArgumentNullException("username");
//        if (project == null || project.Length == 0) throw new ArgumentNullException("project");
//        if (area == null || area.Length == 0) throw new ArgumentNullException("area");
//        if (description == null || description.Length == 0) throw new ArgumentNullException("description");
//
//        BugReport report = new BugReport(url, username);
//        report.Project = project;
//        report.Area = area;
//        report.Description = description;
//        report.Email = (email == null) ? "" : email;
//        report.ForceNewBug = forceNewBug;
//        report.ExtraInformation = (extraInformation == null) ? "" : extraInformation;
//        report.DefaultMsg = (defaultMsg == null) ? "" : defaultMsg;
//        return report.Submit();
//    }
//
//    /* Submit
//    addUserAndMachineInformation: Set to true if user and machine information (username, machine name, machine ip address) is to be
//    added to the bug report; set to false otherwise.
//
//    appendAssemblyList: Set to true to append a list of loaded assemblies and their versions; otherwise set to false.
//
//    forceNewBug: Set to true to force the system to open a new bug entry for this submission; or set to false
//    if the system should try to append this submission as an additional occurence to an existing bug entry.
//
//    versionFormat: A string used to format the version number. This string will be passed to String.Format,
//    and the four parameters given will be major, minor, build, and revision version numbers. This parameter
//    cannot be null or an ArgumentNullException exception will be thrown.
//
//    returns: Returns the string returned by the scout submission page, perhaps including information to the user about
//    how to work around the bug.
//    */
//
//    public static string Submit(string url, string username, string project, string area, string email, bool forceNewBug, string defaultMsg, Exception ex, bool addUserAndMachineInformation, string versionFormat, bool appendAssemblyList) {
//        if (url == null || url.Length == 0) throw new ArgumentNullException("url");
//        if (username == null || username.Length == 0) throw new ArgumentNullException("username");
//        if (project == null || project.Length == 0) throw new ArgumentNullException("project");
//        if (area == null || area.Length == 0) throw new ArgumentNullException("area");
//        if (ex == null) throw new ArgumentNullException("ex");
//        if (versionFormat == null || versionFormat.Length == 0) throw new ArgumentNullException("versionFormat");
//
//        BugReport report = new BugReport(url, username);
//        report.Project = project;
//        report.Area = area;
//        report.SetException(ex, addUserAndMachineInformation, versionFormat);
//        if (appendAssemblyList) report.AppendAssemblyList();
//        report.Email = (email == null) ? String.Empty : email;
//        report.ForceNewBug = forceNewBug;
//        report.DefaultMsg = (defaultMsg == null) ? "" : defaultMsg;
//        return report.Submit();
//    }
//
//    #endregion
//
//
//    #
//    region Private
//    Methods
//
//    // ParseResult: Deciphers the xml result returned by the scout page and throws an exception in the case of a failure notice.
//
//    private string ParseResult(string result) {
//        // Check for a success result first and just return in that case
//        Match ma = Regex.Match(result, "<Success>(?<message>.*)</Success>", RegexOptions.IgnoreCase);
//        if (ma.Success) return ma.Groups["message"].Value;
//
//        // Check for a failure result second, and throw an exception in that case
//        ma = Regex.Match(result, "<Error>(?<message>.*)</Error>", RegexOptions.IgnoreCase);
//        if (ma.Success) throw new BugReportSubmitException(ma.Groups["message"].Value);
//
//        // Unknown format, so throw an InvalidOperationException to note the fact
//        throw new InvalidOperationException(new System.Resources.ResourceManager(typeof(BugReport)).GetString("UnableToProcessResult"));
//    }
//
//
//    /* GetExceptionDescription
//         Formats the description of the exception into a unique identifiable string.
//         ex: The Exception object to copy information from. This parameter cannot be null or an
//         ArgumentNullException exception will be thrown.
//
//         versionFormat: A string used to format the version number. This string will be passed to String.Format,
//         and the four parameters given will be major, minor, build, and revision version numbers. This parameter
//         cannot be null or an ArgumentNullException exception will be thrown.
//
//         returns: The formatted description string.
//
//         Remarks: The reason for this method and not just a simpler way of producing the description is that this
//         string will be used to find existing bugs in the database to add occurances to, instead of adding
//         new bugs for each occurance.
//         */
//
//    private string GetExceptionDescription(Exception ex, string versionFormat) {
//        if (ex == null) throw new ArgumentNullException("ex");
//        if (versionFormat == null || versionFormat.Length == 0) throw new ArgumentNullException("versionFormat");
//
//        StringBuilder desc = new StringBuilder();
//
//        // We first want the class name of the exception that occured
//        desc.Append(ex.GetType().Name);
//
//        // If the exception has a property called ErrorCode, add the value of it to the desc
//        Regex rePropertyName = new Regex("^(ErrorCode|HResult)$", RegexOptions.IgnoreCase);
//        foreach(System.Reflection.PropertyInfo property in ex.GetType().GetProperties())
//        {
//            if (rePropertyName.Match(property.Name).Success) {
//                // Only deal with readable properties
//                if (property.CanRead) {
//                    // Only deal with properties that aren't indexed
//                    if (property.GetIndexParameters().Length == 0) {
//                        // Only add property values that are not null
//                        Object propertyValue = property.GetValue(ex, new Object[]{});
//                        if (propertyValue != null) {
//                            // If the property value converted to a string yields the same name as the class
//                            // name of the value, it is uninteresting
//                            string propertyValueString = propertyValue.ToString();
//                            if (propertyValueString != propertyValue.GetType().FullName)
//                                desc.AppendFormat(" {0}={1}", property.Name, propertyValueString);
//                        }
//                    }
//                }
//            }
//        }
//
//        // Work out the first source code reference in the stacktrace and add the unique value for it
//        Regex reSourceReference = new Regex("at\\s+.+\\.(?<methodname>[^)]+)\\(.*\\)\\s+in\\s+.+\\\\(?<filename>[^:\\\\]+):line\\s+(?<linenumber>[0-9]+)", RegexOptions.IgnoreCase);
//        bool gotReference = false;
//        if (ex.StackTrace != null) {
//            foreach(string line in ex.StackTrace.Split('\n', '\r'))
//            {
//                Match ma = reSourceReference.Match(line);
//                if (ma.Success) {
//                    desc.AppendFormat(" ({0}:{1}:{2})",
//                            ma.Groups["filename"].Value,
//                            ma.Groups["methodname"].Value,
//                            ma.Groups["linenumber"].Value);
//                    gotReference = true;
//                    break;
//                }
//            }
//        }
//
//
//        // If we didn't get a source reference (release compile ?), try to find a non-System.* reference
//        if (!gotReference) {
//            Regex reMethodReference = new Regex("at\\s+(?<methodname>[^(]+)\\(.*\\)", RegexOptions.IgnoreCase);
//            if (ex.StackTrace != null) {
//                foreach(string line in ex.StackTrace.Split('\n', '\r'))
//                {
//                    Match ma = reMethodReference.Match(line);
//                    if (ma.Success) {
//                        if (!ma.Groups["methodname"].Value.ToUpper().StartsWith("SYSTEM.")) {
//                            desc.AppendFormat(" ({0})",
//                                    ma.Groups["methodname"].Value);
//                            gotReference = true;
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//
//        // If we can get the entry assembly, add the version number of it
//        System.Reflection.Assembly entryAssembly = System.Reflection.Assembly.GetEntryAssembly();
//        if (entryAssembly != null) {
//            System.Reflection.AssemblyName entryAssemblyName = entryAssembly.GetName();
//            if (entryAssemblyName != null) {
//                if (entryAssemblyName.Version != null) {
//                    string version = String.Format(versionFormat,
//                            entryAssemblyName.Version.Major,
//                            entryAssemblyName.Version.Minor,
//                            entryAssemblyName.Version.Build,
//                            entryAssemblyName.Version.Revision);
//                    desc.AppendFormat(" V{0}", version);
//                }
//            }
//        }
//
//        // Return result
//        return desc.ToString();
//    }
//
//
//    //little helper
//
//    private bool IsInteger(Object x) {
//        try {
//            Convert.ToInt32(x);
//            return true;
//        }
//        catch
//        {
//            return false;
//        }
//    }
//
//    #endregion
//
//
//    #
//    region Public
//    Properties
//
//    // Gets the url to the submit page that the bug will be reported to.
//    public string FogBugzUrl{
//        get
//        {
//            return this.fogBugzUrl;
//        }
//    }
//
//    // Gets the username used when logging in to the FogBugz database.
//    public string FogBugzUsername{
//        get
//        {
//            return this.userName;
//        }
//    }
//
//    // The description to post for the bug.
//    public string Description{
//        get
//        {
//            return this.description;
//        }
//        set
//        {
//            if (value == null || value.Length == 0) throw new ArgumentNullException("Description");
//            this.description = value;
//        }
//    }
//
//    // Any extra information to provide for the bug report.
//    public string ExtraInformation{
//        get
//        {
//            return this.extraInformation;
//        }
//        set
//        {
//            if (value == null) throw new ArgumentNullException("ExtraInformation");
//            this.extraInformation = value;
//        }
//    }
//
//    // The name of the project to report the bug for.
//    public string Project{
//        get
//        {
//            return this.project;
//        }
//        set
//        {
//            if (value == null || value.Length == 0) throw new ArgumentNullException("Project");
//            this.project = value;
//        }
//    }
//
//    // The area to report the bug in.
//    public string Area{
//        get
//        {
//            return this.area;
//        }
//        set
//        {
//            if (value == null || value.Length == 0) throw new ArgumentNullException("Area");
//            this.area = value;
//        }
//    }
//
//    // The email to attach to the bug report.
//    public string Email{
//        get
//        {
//            return this.customerEmail;
//        }
//        set
//        {
//            if (value == null || value.Length == 0) throw new ArgumentNullException("Email");
//            this.customerEmail = value;
//        }
//    }
//
//    // Whether to force a new bug or try to locate an existing one to append to.
//    public bool ForceNewBug{
//        get
//        {
//            return this.forceNewBug;
//        }
//        set
//        {
//            this.forceNewBug = value;
//        }
//    }
//
//    public string DefaultMsg{
//        get
//        {
//            return this.defaultMsg;
//        }
//        set
//        {
//            this.defaultMsg = value;
//        }
//    }
//
//    #endregion
//
//
//}
//}

}
