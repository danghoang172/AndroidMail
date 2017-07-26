package com.project.hoang.androidmail;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;


public class URIHandler {
    public static final String hostName = "xamloz.com";

    public static String doGet(String uri,String failure) {
        InputStream is = null;

        try {
            //Log.d("AndroidMail","GET uri: " + uri);
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            if (response != 200)
                return failure;

            is = conn.getInputStream();
            // Read the response as an array of chars
            Reader reader;
            reader = new InputStreamReader(is, "UTF-8");
            char[] buffer = new char[conn.getContentLength()];
            reader.read(buffer);
            //Log.d("AndroidMail","Received: " + result);
            return new String(buffer);

        } catch(Exception ex) {
            Log.d("AndroidMail", "Exception in doGet1: " + ex.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    Log.d("AndroidMail", "Exception in doGet2: " + ex.getMessage());
                }
            }
        }
        return failure;
    }

    public static String doPost(String uri, String data) {
        InputStream is = null;

        try {
            //Log.d("AndroidMail","Post uri: " + uri);
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            //Log.d("RESTMail","Posted: " + data);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.flush();

            // Starts the query
            conn.connect();

            is = conn.getInputStream();
            // Read the response as an array of char
            Reader reader;
            reader = new InputStreamReader(is, "UTF-8");
            char[] buffer = new char[conn.getContentLength()];
            reader.read(buffer);
            // Convert the array of chars to a String and return that
            //Log.d("AndroidMail","Received: " + result);
            return new String(buffer);
        } catch(Exception ex) {
            Log.d("AndroidMail", "Exception in doPost1: " + ex.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    Log.d("AndroidMail", "Exception in doPost2: " + ex.getMessage());
                }
            }
        }
        return "";
    }

    public static void doDelete(String uri) {
        try {
            Log.d("AndroidMail","DELETE uri: " + uri);
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");

            // Starts the query
            conn.connect();
            int responseCode = conn.getResponseCode();
        } catch (Exception ex) {
            Log.d("AndroidMail", "Exception in doDelete: " + ex.getMessage());
        }
    }
}

