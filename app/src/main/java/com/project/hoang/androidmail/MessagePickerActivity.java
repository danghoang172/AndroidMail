package com.project.hoang.androidmail;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MessagePickerActivity extends AppCompatActivity {

    private String userID;
    private JSONArray handles = null;
    private int selected_handle = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            userID = savedInstanceState.getString(LoginActivity.USER_ID);
        } else {
            Intent intent = getIntent();
            userID = intent.getStringExtra(LoginActivity.USER_ID);
        }
        setContentView(R.layout.activity_message_picker);
        new HandlesTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new HandlesTask().execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle arg0) {
        super.onSaveInstanceState(arg0);
        arg0.putString(LoginActivity.USER_ID, userID);
    }

    public void readMessage(View view) {
        if (selected_handle != -1) {
            try {
                JSONObject h = handles.getJSONObject(selected_handle);
                Intent intent = new Intent(this, MessageReaderActivity.class);
                intent.putExtra(LoginActivity.MESSAGE_ID, h.getString("message"));
                intent.putExtra(LoginActivity.RECIPIENT_ID, h.getString("id"));
                intent.putExtra(LoginActivity.SENDER, h.getString("sender"));
                startActivity(intent);
            } catch(JSONException ex) {
                Log.d("AndroidMail","Exception in readMessage: "+ex.getMessage());
            }
        }
    }

    public void newMessage(View view) {
        Intent intent = new Intent(this, MessageComposerActivity.class);
        intent.putExtra(LoginActivity.USER_ID, userID);
        startActivity(intent);
    }

    public void deleteMessage(View view) {

        if (selected_handle != -1) {
            try {
                new DeleteTask(handles.getJSONObject(selected_handle).getString("id")).execute();
            } catch(JSONException ex) {
                Log.d("AndroidMail","Exception in deleteMessage: "+ex.getMessage());
            }
        }
    }

    private class HandlesTask extends AsyncTask<String, Void, String> {
        private String uri;

        HandlesTask() {
            uri = "http://" + URIHandler.hostName + "/cakephp/handle/view.json?id=" + userID;
        }

        @Override
        protected String doInBackground(String... urls) {
            return URIHandler.doGet(uri, "");
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            loadHandles(result);
        }
    }

    private class DeleteTask extends AsyncTask<String, Void, Void> {
        private String uri;

        DeleteTask(String id) {
            uri = "http://" + URIHandler.hostName + "/cakephp/recipients/delete/" + id + ".json";
        }

        @Override
        protected Void doInBackground(String... urls) {
            URIHandler.doDelete(uri);
            return null;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Void result) {
            selected_handle = -1;
            new HandlesTask().execute();
        }
    }

    private void loadHandles(String json) {
        handles = null;
        String[] handleStrs = null;

        ListView handlesList = (ListView) findViewById(R.id.message_list);

        try {
            JSONObject messageList = new JSONObject(json);
            handles = messageList.getJSONArray("messages");
            handleStrs = new String[handles.length()];
            for(int n = 0;n < handleStrs.length;n++) {
                JSONObject handle = handles.getJSONObject(n);
                handleStrs[n] = handle.getString("sender") + ":" + handle.getString("subject");
            }
        } catch (JSONException ex) {
            Log.d("AndroidMail","Exception in loadHandles: "+ex.getMessage());
            handleStrs = new String[0];
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, handleStrs);
        handlesList.setAdapter(adapter);

        handlesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int i, long l) {
                // remember the selection
                selected_handle = i;
            }
        });
    }

}