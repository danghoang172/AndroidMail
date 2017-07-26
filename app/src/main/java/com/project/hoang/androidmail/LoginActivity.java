package com.project.hoang.androidmail;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    public final static String USER_ID = "com.project.hoang.AndroidMail.USER_ID";
    public final static String MESSAGE_ID = "com.project.hoang.AndroidMail.MESSAGE_ID";
    public final static String RECIPIENT_ID = "com.project.hoang.AndroidMail.RECIPIENT_ID";
    public final static String SENDER = "com.project.hoang.AndroidMail.SENDER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void newUser(View view) {
        EditText userText = (EditText) findViewById(R.id.user_name);
        String userName = userText.getText().toString();
        EditText passwordText = (EditText) findViewById(R.id.password);
        String password = passwordText.getText().toString();
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new NewUserTask(userName,password).execute();
        } else {
            userMessage(getResources().getString(R.string.message_no_network));
        }
    }

    private class NewUserTask extends AsyncTask<String, Void, String> {
        private String uri;
        private String user, password;
        NewUserTask(String userName,String password) {
            uri="http://"+URIHandler.hostName+"/cake/users/add.json";
            this.user = userName;
            this.password = password;
        }

        @Override
        protected String doInBackground(String... urls) {

            return URIHandler.doPost(uri,"{\"name\":\""+user+"\",\"password\":\""+password+"\"}");
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if("0".equals(result))
                userMessage(getResources().getString(R.string.message_login_failed));
            else{
                try {
                    JSONObject user = (new JSONObject(result)).getJSONObject("user");
                    String id = user.getString("id");
                    goToMain(id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void logIn(View view) {
        // Fetch the user name and password from the GUI
        EditText userText = (EditText) findViewById(R.id.user_name);
        String userName = userText.getText().toString();
        EditText passwordText = (EditText) findViewById(R.id.password);
        String password = passwordText.getText().toString();

        // Create and launch the AsyncTask to check the user name and password.
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new LogInTask(userName,password).execute();
        } else {
            userMessage(getResources().getString(R.string.message_no_network));
        }

    }

    private class LogInTask extends AsyncTask<String, Void, String> {
        private String uri;
        private String user, password;

        LogInTask(String userName,String password) {
            uri="http://"+URIHandler.hostName+"/cake/users/login.json";
            this.user = userName;
            this.password = password;
        }

        @Override
        protected String doInBackground(String... urls) {
            return URIHandler.doPost(uri,"{\"name\":\""+user+"\",\"password\":\""+password+"\"}");
        }

        @Override
        protected void onPostExecute(String result) {
            if("0".equals(result))
                userMessage(getResources().getString(R.string.message_login_failed));
            else{
                try {
                    JSONObject user = new JSONObject((new JSONObject(result)).getString("user"));
                    String id = user.getString("id");
                    goToMain(id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    private void userMessage(String message) {
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void goToMain(String id) {
        Intent intent = new Intent(this, MessagePickerActivity.class);
        intent.putExtra(USER_ID, id);
        startActivity(intent);
    }

}
