package com.project.hoang.androidmail;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MessageReaderActivity extends AppCompatActivity {

    private String messageId;
    private String recipientId;
    private String sender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            messageId = savedInstanceState.getString(LoginActivity.MESSAGE_ID);
            recipientId = savedInstanceState.getString(LoginActivity.RECIPIENT_ID);
            sender = savedInstanceState.getString(LoginActivity.SENDER);
        } else {
            Intent intent = getIntent();
            messageId = intent.getStringExtra(LoginActivity.MESSAGE_ID);
            recipientId = intent.getStringExtra(LoginActivity.RECIPIENT_ID);
            sender = intent.getStringExtra(LoginActivity.SENDER);
        }

        setContentView(R.layout.activity_message_reader);
        new MessageTask().execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle arg0) {
        super.onSaveInstanceState(arg0);
        arg0.putString(LoginActivity.MESSAGE_ID, messageId);
        arg0.putString(LoginActivity.RECIPIENT_ID, recipientId);
        arg0.putString(LoginActivity.SENDER, sender);
    }

    private class MessageTask extends AsyncTask<String, Void, String> {
        private String uri;

        MessageTask() {
            uri = "http://" + URIHandler.hostName + "/cake/messages/view/" + messageId + ".json";
        }

        @Override
        protected String doInBackground(String... urls) {
            return URIHandler.doGet(uri, "");
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            loadMessage(result);
        }
    }

    private void loadMessage(String json) {
        JSONObject m;
        try {
            m = (new JSONObject(json)).getJSONObject("message");
            TextView senderText = (TextView) this.findViewById(R.id.sender_text);
            senderText.setText(sender);
            TextView subjectText = (TextView) this.findViewById(R.id.subject_field);
            subjectText.setText(m.getString("subject"));
            TextView bodyText = (TextView) this.findViewById(R.id.body_text);
            bodyText.setText(m.getString("body"));
        } catch (JSONException ex) {
            Log.d("AndroidMail", "Exception in loadMessage: " + ex.getMessage());
        }
    }

    public void closeMessage(View view) {
        finish();
    }

    public void deleteMessage(View view) {
        new DeleteTask().execute();
    }

    private class DeleteTask extends AsyncTask<String, Void, Void> {
        private String uri;
        private String uriMessage;
        // Need to delete both in recipients table and messages table
        DeleteTask() {
            uri = "http://" + URIHandler.hostName + "/cake/recipients/delete/" + recipientId + ".json";
            //uriMessage = "http://" + URIHandler.hostName + "/cake/messages/delete/" + messageId + ".json";
        }

        @Override
        protected Void doInBackground(String... urls) {
            URIHandler.doDelete(uri);
            //URIHandler.doDelete(uriMessage);
            return null;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Void result) {
            finish();
        }
    }
}