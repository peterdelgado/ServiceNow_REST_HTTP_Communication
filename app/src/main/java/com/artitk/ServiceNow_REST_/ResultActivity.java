package com.artitk.ServiceNow_REST_;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.Authenticator;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Proxy;

public class ResultActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private RadioButton radioHeader;
    private RadioButton radioBody;
    private ProgressBar progressBar;
    private TextView textResult;

    private String dataHeader;
    private String dataBody;


    String mtable;

    TextView mTableName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);



        Intent intent = getIntent();
        int menuIndex = intent.getIntExtra("menu index", 0);
        String menuTitle = getResources().getStringArray(R.array.menu_list)[menuIndex];




        radioHeader = (RadioButton) findViewById(R.id.radioHeader);
        radioBody   = (RadioButton) findViewById(R.id.radioBody);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textResult  = (TextView)    findViewById(R.id.textResult);

        setTitle(menuTitle);

        radioHeader.setOnCheckedChangeListener(this);
        radioBody.setOnCheckedChangeListener(this);

        switch (menuIndex) {
            case 0: callSyncGet();

            Toast.makeText(getApplicationContext(),
                    "Deleting Record", Toast.LENGTH_LONG).show();
            break;
            case 1:

                Toast.makeText(getApplicationContext(),
                        "Getting Records", Toast.LENGTH_LONG).show();
                callASyncGet();


                break;  // Asynchronous Get
            case 2: updatingRecord();

                Toast.makeText(getApplicationContext(),
                        "Updating Record", Toast.LENGTH_LONG).show();

                break;  // Accessing Headers
            case 3: callPostString();

                Toast.makeText(getApplicationContext(),
                        "Posting String", Toast.LENGTH_LONG).show();

                break;  // Posting a String
            // TODO : Add other case
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isChecked) return;

        String result = null;

        int viewId = buttonView.getId();
        switch (viewId) {
            case R.id.radioHeader:  result = dataHeader;    break;
            case R.id.radioBody:    result = dataBody;      break;
        }



        textResult.setText(result);



    }

    private void callSyncGet() {
        new AsyncTask<Void, Void, Message>() {
            @Override
            protected void onPreExecute() {
                resetView();

                super.onPreExecute();
            }

            @Override
            protected Message doInBackground(Void... voids) {

                String cadena = getIntent().getStringExtra("CAJA");
                String sysid = getIntent().getStringExtra("SYSID");


                OkHttpClient okHttpClient = new OkHttpClient();



                Request.Builder builder = new Request.Builder();
                Request request = builder.url("https://dev19007.service-now.com/api/now/table/" + cadena + "/"+ sysid).delete().build();




                okHttpClient.setAuthenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Proxy proxy, Response response) throws IOException {
                        String credential = Credentials.basic("admin", "Kingbde85!!!");
                        return response.request().newBuilder().header("Authorization", credential).build();


                    }

                    @Override
                    public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                        return null;
                    }
                });




                Message message = new Message();

                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        message.what = 1;
                        message.obj  = response;
                    } else {
                        message.what = 0;
                        message.obj  = "Not Success\ncode : " + response.code();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    message.what = 0;
                    message.obj  = "Error\n" + e.getMessage();
                }

                return message;
            }

            @Override
            protected void onPostExecute(Message message) {
                super.onPostExecute(message);

                switch (message.what) {
                    case 0:
                        dataBody = (String) message.obj;
                        break;
                    case 1:
                        getResponseData((Response) message.obj);
                        break;
                }

                showView();

                message.recycle();
            }
        }.execute();
    }

    private void callASyncGet() {
        resetView();






        String cadena = getIntent().getStringExtra("CAJA");




        OkHttpClient okHttpClient = new OkHttpClient();

        Request.Builder builder = new Request.Builder();
        Request request = builder.url("https://dev19007.service-now.com/api/now/table/" + cadena +"?sysparm_limit=10").build();



        okHttpClient.setAuthenticator(new Authenticator() {
            @Override
            public Request authenticate(Proxy proxy, Response response) throws IOException {
                String credential = Credentials.basic("admin", "Kingbde85!!!");
                return response.request().newBuilder().header("Authorization", credential).build();


            }

            @Override
            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                return null;
            }
        });





        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                dataBody = "Error\n" + e.getMessage();

                updateView();
            }

            @Override
            public void onResponse(Response response) {
                if (response.isSuccessful()) {

                    getResponseData(response);
                } else {
                    dataBody = "Not Success\ncode : " + response.code();
                }

                updateView();
            }

            public void updateView() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showView();
                    }
                });
            }
        });
    }

    private void updatingRecord() {
        resetView();

        OkHttpClient okHttpClient = new OkHttpClient();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");


        //String postBody = "{budget:1234.00}";
        String postBody = getIntent().getStringExtra("POST");

        String cadena = getIntent().getStringExtra("CAJA");
        String sysid = getIntent().getStringExtra("SYSID");

        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url("https://dev19007.service-now.com/api/now/table/" + cadena +"/"+ sysid)
                .put(RequestBody.create(JSON, postBody))
                .build();

        okHttpClient.setAuthenticator(new Authenticator() {
            @Override
            public Request authenticate(Proxy proxy, Response response) throws IOException {
                String credential = Credentials.basic("admin", "Kingbde85!!!");
                return response.request().newBuilder().header("Authorization", credential).build();


            }

            @Override
            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                return null;
            }
        });



        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                dataBody = "Error\n" + e.getMessage();

                updateView();
            }

            @Override
            public void onResponse(Response response) {
                if (response.isSuccessful()) {
                    getResponseData(response);
                } else {
                    dataBody = "Not Success\ncode : " + response.code();
                }

                updateView();
            }

            public void updateView() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showView();
                    }
                });
            }
        });
    }

    private void callPostString() {
        resetView();

        OkHttpClient okHttpClient = new OkHttpClient();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");


        String postBody = getIntent().getStringExtra("POST");

        String cadena = getIntent().getStringExtra("CAJA");

        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url("https://dev19007.service-now.com/api/now/table/" + cadena)
                .post(RequestBody.create(JSON, postBody))
                .build();

        okHttpClient.setAuthenticator(new Authenticator() {
            @Override
            public Request authenticate(Proxy proxy, Response response) throws IOException {
                String credential = Credentials.basic("admin", "Kingbde85!!!");
                return response.request().newBuilder().header("Authorization", credential).build();


            }

            @Override
            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                return null;
            }
        });



        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                dataBody = "Error\n" + e.getMessage();

                updateView();
            }

            @Override
            public void onResponse(Response response) {
                if (response.isSuccessful()) {
                    getResponseData(response);
                } else {
                    dataBody = "Not Success\ncode : " + response.code();
                }

                updateView();
            }

            public void updateView() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showView();
                    }
                });
            }
        });
    }

    private void getResponseData(Response response) {
        Headers headers = response.headers();
        for (String header : headers.names()) {
            dataHeader += "name : " + header + "\n+ value : " + headers.get(header) + "\n";
        }

        try {

            //String jsonData


            JSONObject json = null;
            try {
                json = new JSONObject(response.body().string());
            } catch (JSONException e) {
                e.printStackTrace();
            }


            try {
                dataBody = json.toString(1);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
            dataBody = "Error !\n\n" + e.getMessage();

            e.printStackTrace();
        }
    }

    private void resetView() {
        dataHeader = "";
        dataBody   = "";

        radioHeader.setEnabled(false);
        radioHeader.setChecked(false);
        radioBody.setEnabled(false);
        radioBody.setChecked(false);
        progressBar.setVisibility(View.VISIBLE);
        textResult.setVisibility(View.GONE);
        textResult.setText("");
    }

    private void showView() {
        radioHeader.setEnabled(true);
        radioBody.setEnabled(true);
        radioBody.setChecked(true);
        progressBar.setVisibility(View.GONE);
        textResult.setVisibility(View.VISIBLE);
    }
}
