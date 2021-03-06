package com.gfo.gfo_meesterproef.support;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

public class MasterBackgroundWorker extends AsyncTask<String, Void, String> {

    private String result;

    private String php_url;
    private String[] inputKeys;
    private boolean feedbackToast;
    private boolean useType;
    private PhpBranch phpBranch;

    Context context;
    private OnTaskCompleted listener;
    public MasterBackgroundWorker(Context ctx, OnTaskCompleted listener){
        context = ctx;
        this.listener = listener;
    }
    //    get access to ProgressBar in activity
    @SuppressLint("StaticFieldLeak") private ProgressBar progressBar;
    public void setProgressBar(ProgressBar progressBar) { this.progressBar = progressBar; }
    //    create interface to communicate with Activity
    public interface OnTaskCompleted{ void onTaskCompleted(String result) throws JSONException;}

    @Override
    protected String doInBackground(String... params) {
        String type = params[0];

//        define phpBranch and determine its variables in processType
        phpBranch = new PhpBranch();
        phpBranch.processType(type);

//        determine input keys for php etc. , based on type, stored in phpBranch
        php_url         = phpBranch.getUrl();
        inputKeys       = phpBranch.getInputKeys();
        feedbackToast   = phpBranch.getFeedbackToast();
        useType         = phpBranch.getUseType();

//        copy params[] to inputValues[], with (/if) or without (/else) the type parameter at index 0
        String[] inputValues;
        if (useType){ inputValues = params; }
        else { inputValues = Arrays.copyOfRange(params,1,params.length); }

        if (php_url != null){
            try {
//                connect to database
                URL url = new URL(php_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

//                send data
                httpURLConnection.setRequestMethod("POST");
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
//                format data
                StringBuilder post_dataBuilder = new StringBuilder();
                for (int i = 0; i < inputKeys.length; i++) {
                    post_dataBuilder.append(URLEncoder.encode(inputKeys[i], "UTF-8")).append("=").append(URLEncoder.encode(inputValues[i], "UTF-8"));
//                    last iteration shouldn't append "&", all before should
                    if (i < (inputKeys.length - 1)) {
                        post_dataBuilder.append("&");
                    }
                }
                String post_data = post_dataBuilder.toString();
//                post data
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
//                close output
                bufferedWriter.close();
                outputStream.close();

//                receive data
                httpURLConnection.setRequestMethod("GET");
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
//                format data
                StringBuilder read_dataBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    //appending it to string builder
                    read_dataBuilder.append(line).append("\n");
                }
                result = read_dataBuilder.toString().trim();

//                close input
                bufferedReader.close();
                inputStream.close();

//                disconnect to database
                httpURLConnection.disconnect();
                return result;
            }
            catch (MalformedURLException e) { e.printStackTrace(); }
            catch (IOException e) { e.printStackTrace(); }
        } return result;
    }

    @Override
    protected void onPreExecute(){
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostExecute(String result){
        progressBar.setVisibility(View.GONE);
        if (feedbackToast) { Toast.makeText(context, result, Toast.LENGTH_LONG).show(); }
//        notify Activity that AsyncTask is finished
        try { listener.onTaskCompleted(result); }
        catch (JSONException e) { e.printStackTrace(); }
    }
}