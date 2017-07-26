package com.project.hoang.androidmail;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MessageComposerActivity extends AppCompatActivity {

    private String userId;
    private int recipientsToPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_composer);
        if (savedInstanceState != null) {
            userId = savedInstanceState.getString(LoginActivity.USER_ID);
        } else {
            Intent intent = getIntent();
            userId = intent.getStringExtra(LoginActivity.USER_ID);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle arg0) {
        super.onSaveInstanceState(arg0);
        arg0.putString(LoginActivity.USER_ID, userId);
    }

    public void cancelSend(View view) {
        finish();
    }

    public void doSend(View view) {
        EditText subjectText = (EditText) findViewById(R.id.subject_field);
        String subject = subjectText.getText().toString();
        EditText bodyText = (EditText) findViewById(R.id.body_field);
        String body = bodyText.getText().toString();
        JSONObject newMessage = new JSONObject();
        try {
            newMessage.put("subject", subject);
            newMessage.put("body", body);
            newMessage.put("user_id",Integer.parseInt(userId));
            new NewMessageTask(newMessage).execute();
        } catch (JSONException ex) {
            Log.d("AndroidMail", "Exception in doSend: " + ex.getMessage());
        }
    }

    // Task to post a new message
    private class NewMessageTask extends AsyncTask<String, Void, String> {
        private String uri;
        private JSONObject toSend;
        NewMessageTask(JSONObject toSend) {
            uri = "http://" + URIHandler.hostName + "/cake/messages/add.json";
            this.toSend = toSend;
        }

        @Override
        protected String doInBackground(String... urls) {
            return URIHandler.doPost(uri, toSend.toString());
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            // After posting the message, we will want to post the individual recipients.
            try {
                JSONObject msg = (new JSONObject(result)).getJSONObject("msg");
                String messageId = msg.getString("id");
                processRecipients(messageId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void processRecipients(String messageId) {
        EditText recipientsText = (EditText) findViewById(R.id.to_field);
        String recipients = recipientsText.getText().toString();

        // Get a list of recipient names and post a series of background
        // tasks to process each recipient in turn.
        String[] tos = recipients.split(",");
        recipientsToPost = tos.length;
        for(int n = 0;n < tos.length;n++) {
            new LookupRecipientTask(tos[n],messageId).execute();
        }
    }

    // Task to look up the id number for a recipient.
    private class LookupRecipientTask extends AsyncTask<String, Void, String> {
        private String uri;
        private String id;
        LookupRecipientTask(String recipient,String id) {
            uri = "http://" + URIHandler.hostName + "/cake/users/findID.json?recipient=" + recipient;
            this.id = id;
        }

        @Override
        protected String doInBackground(String... urls) {
            return URIHandler.doGet(uri, "");
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            // Once the task concludes and we have a recipient number, we
            // can launch another task to post this recipient.
            String userId;
            try {
                userId = (new JSONObject(result)).getString("id");
                addRecipient(userId ,id);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void addRecipient(String recipientId,String messageId) {
        new PostRecipientTask(recipientId,messageId).execute();
    }

    // Task to post a new recipient entry to the table of recipients.
    private class PostRecipientTask extends AsyncTask<String, Void, String> {
        private String uri;
        private String json;
        PostRecipientTask(String recipient,String message) {
            uri = "http://" + URIHandler.hostName + "/cake/recipients/add.json";
            json = "{\"recipient\":" + recipient +
                    ",\"message_id\":" + message +
                    "}";
        }

        @Override
        protected String doInBackground(String... urls) {
            return URIHandler.doPost(uri, json);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            // When this task concludes, we mark off another recipient
            recipientsToPost--;
            // When the last recipient has been posted, we call finish() to
            // exit this activity.
            if(recipientsToPost == 0)
                finish();
        }
    }

}