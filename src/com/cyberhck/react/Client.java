package com.cyberhck.react;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Client implements LoggingClient {
    private String tag;
    private String endpont;
    Client(String tag, String endpoint) {
        this.tag = tag;
        this.endpont = endpoint;
    }
    @Override
    public void debug(String message) {
        this.log(LoggingClient.DEBUG, message);
    }

    @Override
    public void info(String message) {
        this.log(LoggingClient.INFO, message);
    }

    @Override
    public void notice(String message) {
        this.log(LoggingClient.NOTICE, message);
    }

    @Override
    public void warn(String message) {
        this.log(LoggingClient.WARNING, message);
    }

    @Override
    public void error(String message) {
        this.log(LoggingClient.ERROR, message);
    }

    @Override
    public void critical(String message) {
        this.log(LoggingClient.CRITICAL, message);
    }

    @Override
    public void alert(String message) {
        this.log(LoggingClient.ALERT, message);
    }

    @Override
    public void emergency(String message) {
        this.log(LoggingClient.EMERGENCY, message);
    }

    private void log(int level, String message) {
        JSONObject json = new JSONObject();
        try {
            HttpPost post = new HttpPost(this.endpont);
            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), 1000);
            json.put("level", level);
            json.put("message", message);
            json.put("tag", this.tag);
            StringEntity se = new StringEntity(json.toString());
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(se);
            HttpResponse response = client.execute(post);
            if (response != null) {
                int code = response.getStatusLine().getStatusCode();
                if (code >= 300) {
                    this.sendNotification("something wrong in http");
                }
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
    private void _log(int level, String message) {
        try {
            URL endpoint = new URL(this.endpont);
            HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();
            connection.setDoOutput(true); // sets request method to POST
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Method", "POST");
            message = message.replaceAll("\"", "\\\"");
            tag = this.tag.replaceAll("\"", "\\\"");
            String jsonData  = "{\"message\": \"" + message +"\", \"level\": " + level + ", \"tag\": \""+ tag +"\"}";
            this.sendNotification(jsonData);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonData.getBytes(StandardCharsets.UTF_8));
            outputStream.close();
            outputStream.flush();
        } catch (IOException e) {
            this.sendNotification(e.getMessage());
            e.printStackTrace();
        }
    }
    private void sendNotification(String body) {
        Notification notification = new Notification("group", null, NotificationType.ERROR);
        notification.setTitle(this.tag);
        notification.setContent(body);
        Notifications.Bus.notify(notification);
    }
}
