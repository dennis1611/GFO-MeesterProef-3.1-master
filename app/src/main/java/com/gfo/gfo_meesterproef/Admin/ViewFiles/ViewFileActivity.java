package com.gfo.gfo_meesterproef.Admin.ViewFiles;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gfo.gfo_meesterproef.MasterBackgroundWorker;
import com.gfo.gfo_meesterproef.R;
import com.gfo.gfo_meesterproef.Support.ConnectionCheck;

import java.util.List;

public class ViewFileActivity extends AppCompatActivity {

    public static Context fileContext;

    String clickedFile;
    ListView adminProductList;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        fileContext = getApplicationContext();

//        setup ProgressBar
        progressBar = findViewById(R.id.progressBar);

//        get selected product from ViewProductActivity
        String product = getIntent().getExtras().getString("adminProduct", "");

//        set label
        setTitle("View Files from "+product);

//        contact database for files
        MasterBackgroundWorker viewFile = new MasterBackgroundWorker(this, listener);
        viewFile.setProgressBar(progressBar);
        viewFile.execute("viewFile", product);}//        end method

//    create listener to wait for AsyncTask to finish
    MasterBackgroundWorker.OnTaskCompleted listener = new MasterBackgroundWorker.OnTaskCompleted() {
//    code below won't get executed until AsyncTask is finished
        @Override
        public void onTaskCompleted(List<String> files) {
            // fill listView with List
            adminProductList = findViewById(R.id.list);
            ArrayAdapter<String> productAdapter = new ArrayAdapter<>(ViewFileActivity.this, android.R.layout.simple_list_item_1, files);
            adminProductList.setAdapter(productAdapter);

            registerFileClickCallBack();
        }
    };

    private void registerFileClickCallBack() {
        adminProductList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View viewClicked, int position, long id) {
                //        check for internet connection
                boolean connection = new ConnectionCheck().test(getApplicationContext());
                if (!connection){return;}
//                get selected file
                TextView textView = (TextView) viewClicked;
                clickedFile = textView.getText().toString();

//                contact database for url
                MasterBackgroundWorker openFileBackgroundWorker = new MasterBackgroundWorker(ViewFileActivity.this, listener);
                openFileBackgroundWorker.setProgressBar(progressBar);
                openFileBackgroundWorker.execute("openFile", clickedFile);}//                end method

            MasterBackgroundWorker.OnTaskCompleted listener = new MasterBackgroundWorker.OnTaskCompleted() {
                @Override
                public void onTaskCompleted(List<String> resultList) {
                    //                        open file
                    String url = resultList.get(0);
                    Intent web = new Intent(Intent.ACTION_VIEW);
                    web.setData(Uri.parse(url));
                    startActivity(web); }
            };
        });
    }
    @Override
    public void onBackPressed() {
        //        check for internet connection
        boolean connection = new ConnectionCheck().test(getApplicationContext());
        if (!connection){return;}
        Intent i = new Intent(ViewFileActivity.this, ViewProductActivity.class);
        ViewFileActivity.this.finish();
        startActivity(i); }
}