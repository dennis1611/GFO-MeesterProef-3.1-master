package com.gfo.gfo_meesterproef.admin.viewProductFile;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.gfo.gfo_meesterproef.admin.AdminActivity;
import com.gfo.gfo_meesterproef.admin.link.CoupleToProductActivity;
import com.gfo.gfo_meesterproef.support.FolderAdapter;
import com.gfo.gfo_meesterproef.support.MasterBackgroundWorker;
import com.gfo.gfo_meesterproef.R;
import com.gfo.gfo_meesterproef.support.ConnectionCheck;

import java.util.List;

public class ViewProductActivity extends AppCompatActivity {

    GridView adminProductGrid;
    String selectedProduct;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid);

//        setup ProgressBar
        progressBar = findViewById(R.id.progressBar);

//        contact database
        MasterBackgroundWorker viewProduct = new MasterBackgroundWorker(this, listener);
        viewProduct.setProgressBar(progressBar);
        viewProduct.execute("viewProduct");}//        end method

//    create listener to wait for AsyncTask to finish
    MasterBackgroundWorker.OnTaskCompleted listener = new MasterBackgroundWorker.OnTaskCompleted() {
//    code below won't get executed until AsyncTask is finished
        @Override
        public void onTaskCompleted(List<String> products) {
//        fill gridView with (array)List
        adminProductGrid = findViewById(R.id.grid);
        FolderAdapter productAdapter = new FolderAdapter(ViewProductActivity.this, products);
        adminProductGrid.setAdapter(productAdapter);

        registerProductClickCallback();
        }
    };


//    select product
    private void registerProductClickCallback() {
        adminProductGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                //        check for internet connection
                boolean connection = new ConnectionCheck().test(getApplicationContext());
                if (!connection){return;}
//                get selected product
                selectedProduct = (String) adminProductGrid.getItemAtPosition(position);
//                choice to couple product or view files
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewProductActivity.this);
                builder.setTitle(selectedProduct);
//                builder.setMessage("What do you want to do?");
                builder.setPositiveButton("View Files", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                start ViewFileActivity
                        Intent i = new Intent(ViewProductActivity.this, ViewFileActivity.class);
                        i.putExtra("adminProduct", selectedProduct);
                        startActivity(i);}
                });
                builder.setNegativeButton("Couple Product to Accounts", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                save selected product for couple to product
                        SharedPreferences selectedProductPref = getSharedPreferences("selectedProductPreference", MODE_PRIVATE);
                        selectedProductPref.edit().putString("selectedProduct", selectedProduct).apply();
//                        start couple activity
                        Intent i = new Intent(ViewProductActivity.this, CoupleToProductActivity.class);
                        startActivity(i);
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
            }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(ViewProductActivity.this, AdminActivity.class);
        ViewProductActivity.this.finish();
        startActivity(i);
    }
}