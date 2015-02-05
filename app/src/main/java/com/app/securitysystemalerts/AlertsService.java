/*
 * Copyright (c) 2015 Valery Sigalov (valery.sigalov@gmail.com)
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

package com.app.securitysystemalerts;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Process;
import android.support.v4.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AlertsService extends IntentService {

    String USER_AGENT = "Mozilla/5.0";

    private boolean running = true;
    private String cookies = null;
    private String postParams = null;
    private HttpsURLConnection conn;

    private String event_log_url = null;

    private static String newEventLog;
    private static String oldEventLog;

    private String ssHost;
    private String ssLoginUrl;
    private String ssUsername;
    private String ssPassword;

    private String smtpHost;
    private String smtpPort;

    private String emailUsername;
    private String emailPassword;

    private String toEmail;
    private String ccEmail;

    private String operator;
    private String toNumber;
    private String ccNumber;

    private enum returnCode {
        AUTHENTICATION_FAILURE,
        CONNECTION_RETRY,
        SERVICE_EXIT
    }

    private returnCode status;

    private Map<String, String> smsMap;

    public AlertsService() {
        super("AlertsService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        Bundle extras = workIntent.getExtras();
        ssLoginUrl = extras.getString("ssLoginUrl");
        ssUsername = extras.getString("ssUsername");
        ssPassword = extras.getString("ssPassword");
        smtpHost = extras.getString("smtpHost");
        smtpPort = extras.getString("smtpPort");
        emailUsername = extras.getString("emailUsername");
        emailPassword = extras.getString("emailPassword");
        toEmail = extras.getString("toEmail");
        ccEmail = extras.getString("ccEmail");
        operator = extras.getString("operator");
        toNumber = extras.getString("toNumber");
        ccNumber = extras.getString("ccNumber");

        int start = "https://".length();
        ssHost = ssLoginUrl.substring(start, ssLoginUrl.indexOf("/", start));

        newEventLog = System.getProperty("java.io.tmpdir") + "/new_event_log.log";
        oldEventLog = System.getProperty("java.io.tmpdir") + "/old_event_log.log";

        status = returnCode.AUTHENTICATION_FAILURE;

        createSmsMap();

        if (toNumber.length() > 0)
            toNumber = toNumber + smsMap.get(operator);
        if (ccNumber.length() > 0)
            ccNumber = ccNumber + smsMap.get(operator);

        // Turn on the cookies.
        CookieHandler.setDefault(new CookieManager());

        try {
            checkConnection();
        } catch (MessagingException e) {
            DebugLog.writeLog(e.toString());
            sendBroadcast("Authentication Failure", "Connection to SMTP server failed, please check your credentials.");
            return;
        }

        for (;;) {
            cookies = null;
            run(ssLoginUrl);
            if (status == returnCode.AUTHENTICATION_FAILURE) {
                DebugLog.writeLog("Authentication Failure - AlertsService Exit.");
                sendBroadcast("Authentication Failure", "Connection to Security System failed, please check your credentials.");
                break;
            }
            else if (status == returnCode.SERVICE_EXIT) {
                DebugLog.writeLog("Logout or Exit Pressed - AlertsService Exit.");
                sendBroadcast("Service Exit", null);
                break;
            }
            else {
                try {
                    DebugLog.writeLog("Offline Mode - AlertsService Sleep 10 Seconds.");
                    sendBroadcast("Offline Mode", null);
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    DebugLog.writeLog(e.toString());
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        DebugLog.writeLog("AlertsService onDestroy().");
    }

    private void sendBroadcast(String status, String message) {

        Intent intent = new Intent("AlertsService");
        intent.putExtra("Status", status);
        if (message != null)
            intent.putExtra("EventLog", message);
        android.support.v4.content.LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void run(String url)
    {
        if (postParams == null) {

            // Send "GET" request and create post parameters for login request.
            String page;
            try {
                page = getPageContent(url, null, false);
            } catch (IOException e) {
                DebugLog.writeLog(e.toString());
                return;
            }

            try {
                postParams = getFormParams(page, ssUsername, ssPassword);
            } catch (UnsupportedEncodingException | NullPointerException e) {
                DebugLog.writeLog(e.toString());
                return;
            }
        }

        // Send "POST" request for authentication.
        try {
            url = sendPost(url, postParams);
        } catch (IOException e) {
            DebugLog.writeLog(e.toString());
            return;
        }

        String result;
        try {
            result = getPageContent(url, "#event-log", true);
        } catch (IOException e) {
            DebugLog.writeLog(e.toString());
            return;
        }

        try {
            parseString(result);
        } catch (IOException e) {
            DebugLog.writeLog(e.toString());
            return;
        }

        try {
            compareFiles(newEventLog, oldEventLog);
        } catch (IOException | MessagingException e) {
            DebugLog.writeLog(e.toString());
            return;
        }

        while (running) {
            try {
                result = getPageContent(event_log_url, null, false);
                parseString(result);
                compareFiles(newEventLog, oldEventLog);
                Thread.sleep(5000);
            } catch (IOException | MessagingException | InterruptedException e) {
                DebugLog.writeLog(e.toString());
                status = returnCode.CONNECTION_RETRY;
                return;
            }
        }

        status = returnCode.SERVICE_EXIT;
    }

    private void createSmsMap() {

        smsMap = new HashMap<>();

        smsMap.put("TMobile", "@tmomail.net");
        smsMap.put("Virgin", "@vmobl.com");
        smsMap.put("Cingular", "@cingularme.com");
        smsMap.put("Sprint", "@messaging.sprintpcs.com");
        smsMap.put("Verizon", "@vtext.com");
        smsMap.put("Nextel", "@messaging.nextel.com");
        smsMap.put("US Cellular", "@email.uscc.net");
        smsMap.put("SunCom", "@tms.suncom.com");
        smsMap.put("PowerTel", "@ptel.net");
        smsMap.put("AT&T", "@txt.att.net");
        smsMap.put("Alltel", "@message.alltel.com");
        smsMap.put("Metro PCS", "@MyMetroPcs.com");
    }

    private void parseString(String xml) throws IOException {

        int i = 0;
        String body = xml.substring(xml.indexOf("<tbody>"), xml.indexOf("</tbody>")+"</tbody>".length());
        Pattern ptrn = Pattern.compile("<td>(.*?)</td>");
        Matcher match = ptrn.matcher(body);
        PrintWriter writer = new PrintWriter(newEventLog, "UTF-8");
        while(match.find()) {
            writer.print(match.group(1) + " ");
            if (++i%3==0) writer.println();
        }
        writer.close();
    }

    private String sendPost(String url, String postParams) throws IOException {

        URL obj = new URL(url);

        HttpsURLConnection.setFollowRedirects(false);
        conn = (HttpsURLConnection)obj.openConnection();

        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Host", ssHost);
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8,ru;q=0.6");
        if (cookies != null) {
            conn.setRequestProperty("Cookie", cookies);
        }
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Referer", ssLoginUrl);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));

        conn.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(postParams);
        wr.flush();
        wr.close();

        int response = conn.getResponseCode();
        DebugLog.writeLog("Sending 'POST' request to URL : " + url);
        DebugLog.writeLog("Post parameters : " + postParams);
        DebugLog.writeLog("Response code : " + response);

        return conn.getHeaderField("Location");
    }

    private String getPageContent(String url, String ref, boolean setCookies)
            throws  IOException, StringIndexOutOfBoundsException {

        int HTTP_FOUND = 302;

        URL obj = new URL(url);

        if (ref != null)
            HttpsURLConnection.setFollowRedirects(false);
        else
            HttpsURLConnection.setFollowRedirects(true);
        conn = (HttpsURLConnection) obj.openConnection();

        conn.setUseCaches(false);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8,ru;q=0.6");
        if (cookies != null) {
            conn.setRequestProperty("Cookie", cookies);
        }

        int responseCode = conn.getResponseCode();
        DebugLog.writeLog("Sending 'GET' request to URL : " + url);
        DebugLog.writeLog("Response Code : " + responseCode);

        if (responseCode == HTTP_FOUND) {
            String loc = conn.getHeaderField("Location");
            return getPageContent(loc, ref, setCookies);
        }
        if (ref != null) {
            String tag = conn.getHeaderField("ETag");
            event_log_url = url + "/utility/tables?_=" + tag + "&&table=event-log-table";
            return getPageContent(event_log_url, null, setCookies);
        }

        if (setCookies)
            setCookies(conn.getHeaderField("Set-Cookie"));

        BufferedReader in =
                new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine).append('\n');
        }
        in.close();

        return response.toString();
    }

    private String getFormParams(String html, String username, String password)
            throws UnsupportedEncodingException, NullPointerException {

        Document doc = Jsoup.parse(html);

        Element loginform = doc.getElementById("user-login");
        Elements inputElements = loginform.getElementsByTag("input");
        List<String> paramList = new ArrayList<>();
        for (Element inputElement : inputElements) {
            String key = inputElement.attr("name");
            String value = inputElement.attr("value");

            if (key.equals("name"))
                value = username;
            else if (key.equals("pass"))
                value = password;

            paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
        }

        StringBuilder result = new StringBuilder();
        for (String param : paramList) {
            if (result.length() == 0) {
                result.append(param);
            } else {
                result.append("&").append(param);
            }
        }
        return result.toString();
    }

    private void setCookies(String cookie) {

        if (cookie != null) {
            if (this.cookies != null) {
                this.cookies += "; ";
                this.cookies += cookie;
            }
            else {
                this.cookies = cookie;
            }
        }
        DebugLog.writeLog("Cookies :" + this.cookies);
    }

    private void compareFiles(String newEventLog, String oldEventLog) throws IOException, MessagingException {

        File oldFile = new File(oldEventLog);
        StringWriter logStringWriter = new StringWriter();
        PrintWriter logPrintWriter = new PrintWriter(logStringWriter, true);
        if (oldFile.exists()) {
            LineNumberReader newReader = new LineNumberReader(new FileReader(newEventLog));
            LineNumberReader oldReader = new LineNumberReader(new FileReader(oldEventLog));
            String newLine = newReader.readLine();
            String oldLine = oldReader.readLine();
            boolean sendMail = false;
            while (newLine != null) {
                if (!newLine.equals(oldLine)) {
                    sendMail = true;
                    logPrintWriter.println(newLine);
                }
                else
                    break;
                newLine = newReader.readLine();
            }
            if (sendMail) {
                String sendLine = logStringWriter.toString();
                sendNotification("Security System Alert!", sendLine);
                if (toEmail.length() > 0)
                    sendMail(toEmail, ccEmail, "Security System Alert!", sendLine);
                if (toNumber.length() > 0)
                    sendMail(toNumber, ccNumber, "Security System Alert!", sendLine);
            }
            while (newLine != null) {
                logPrintWriter.println(newLine);
                newLine = newReader.readLine();
            }
            newReader.close();
            oldReader.close();
            oldFile.delete();
        }
        File newFile = new File(newEventLog);
        newFile.renameTo(oldFile);
        sendBroadcast("Running", logStringWriter.toString());
    }

    private void checkConnection() throws MessagingException {

        if (toEmail.length() > 0 || toNumber.length() > 0) {
            DebugLog.writeLog("Check connection to SMTP server");
            Properties props = System.getProperties();
            props.setProperty("mail.smtp.host", smtpHost);
            props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.smtp.port", smtpPort);
            props.setProperty("mail.smtp.socketFactory.port", smtpPort);
            props.setProperty("mail.smtp.timeout", "10000");

            Session session;
            session = Session.getInstance(props, null);

            Transport transport = session.getTransport("smtp");
            transport.connect(smtpHost, emailUsername, emailPassword);
            transport.close();
        }
    }

    public void sendMail(String toEmail, String ccEmail, String title, String message) throws MessagingException {

        Properties props = System.getProperties();
        props.setProperty("mail.smtp.host", smtpHost);
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.port", smtpPort);
        props.setProperty("mail.smtp.socketFactory.port", smtpPort);
        props.setProperty("mail.smtp.timeout", "10000");

        Session session;
        if (!emailPassword.equals("")) {
            props.setProperty("mail.smtp.auth", "true");

            session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new javax.mail.PasswordAuthentication(emailUsername, emailPassword);
                }
            });
        }
        else
            session = Session.getInstance(props, null);

        final MimeMessage msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress(emailUsername));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));

        if (ccEmail.length() > 0) {
            msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccEmail, false));
        }

        msg.setSubject(title);
        msg.setText(message, "utf-8");
        msg.setSentDate(new Date());

        Transport.send(msg);
    }

    public void sendNotification(String title, String message) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setDefaults(0)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setColor(Color.RED)
                        .setPriority(2);

        Intent intent = new Intent(this, EventLog.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, mBuilder.build());
    }
}
