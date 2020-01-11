package com.gfo.gfo_meesterproef.admin.link;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gfo.gfo_meesterproef.admin.viewAccount.ViewAccountActivity;
import com.gfo.gfo_meesterproef.support.MasterBackgroundWorker;
import com.gfo.gfo_meesterproef.R;
import com.gfo.gfo_meesterproef.support.ConnectionCheck;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.gfo.gfo_meesterproef.admin.viewAccount.ViewAccountActivity.contextOfViewAccount;

public class CoupleToAccountActivity extends AppCompatActivity {

    String username;
    List<String> totalList, alreadyCoupled, toCouple, toUncouple;
    ListView list;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
//        connect views with id
        progressBar = findViewById(R.id.progressBar);
//        reset toCouple and toUncouple
        toCouple = new ArrayList<>();
        toCouple.clear();
        toUncouple = new ArrayList<>();
        toUncouple.clear();
//        get selected username
        SharedPreferences selectedAccountPref = getSharedPreferences("selectedAccountPreference", contextOfViewAccount.MODE_PRIVATE);
        username = selectedAccountPref.getString("selectedUsername", "");
//        change label
        setTitle("Couple to " + username);

//        get all products (in group)
        MasterBackgroundWorker allProducts = new MasterBackgroundWorker(this, totalListener);
        allProducts.setProgressBar(progressBar);
        allProducts.execute("allProducts");}//        end method

//    create totalListener to wait for AsyncTask to finish
    MasterBackgroundWorker.OnTaskCompleted totalListener = new MasterBackgroundWorker.OnTaskCompleted() {
        @Override
        public void onTaskCompleted(List<String> splitResultList) {
//            get already coupled products
            totalList = splitResultList;
            MasterBackgroundWorker coupledProduct = new MasterBackgroundWorker(CoupleToAccountActivity.this, coupledListener);
            coupledProduct.setProgressBar(progressBar);
            coupledProduct.execute("coupledProduct", username);
        }//        end totalListener
    };

//    create coupledListener to wait for AsyncTask to finish
    MasterBackgroundWorker.OnTaskCompleted coupledListener = new MasterBackgroundWorker.OnTaskCompleted() {
    @Override
    public void onTaskCompleted(List<String> splitResultList) {
        alreadyCoupled = splitResultList;

        //        display all products
        list = findViewById(R.id.list);
        list.setBackgroundResource(R.color.white);
        final ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(CoupleToAccountActivity.this, android.R.layout.simple_list_item_1, totalList){
            @NonNull @Override
//            create view correctly (even when recycled)
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View row = convertView;
//                Check if an existing view is being reused, otherwise inflate the view
                if (row==null){ row = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false); }
//              handle already coupled items and toUncouple items
                if (alreadyCoupled.contains(totalList.get(position))){
//                  compare row with toUncouple. If it contains, make red
                    if (toUncouple.contains(totalList.get(position))){
                        row.setBackgroundResource(R.color.red);
                        row.setTag(R.color.red);
//                  ... if not, make blue
                    } else { row.setBackgroundResource(R.color.blue);
                        row.setTag(R.color.blue); }
//              handle not yet coupled items and toCouple items
                } else {
//                  compare row with toCouple. If it contains, make green
                    if (toCouple.contains(totalList.get(position))){
                        row.setBackgroundResource(R.color.green);
                        row.setTag(R.color.green);
//                  ... if not, make white
                    } else { row.setBackgroundResource(R.color.white);
                        row.setTag(R.color.white); }
                }
//                set text to each row
                String currentProduct = getItem(position);
                TextView productTextView = (TextView) row;
                productTextView.setText(currentProduct);
                return row; }
        };
        list.setAdapter(listAdapter);

        registerProductClickCallBack();
    }
};

    private void registerProductClickCallBack() {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View viewClicked, int position, long id) {
//                get productname of clicked view
                TextView textView = (TextView) viewClicked;
                String product = textView.getText().toString();
//                add or delete from to (un)couple list
                    int ColorId = Integer.parseInt(viewClicked.getTag().toString());
                    if (ColorId==R.color.blue){
                        viewClicked.setBackgroundResource(R.color.red);
                        viewClicked.setTag(R.color.red);
                        toUncouple.add(product);
                    } else if (ColorId==R.color.red){
                        viewClicked.setBackgroundResource(R.color.blue);
                        viewClicked.setTag(R.color.blue);
                        toUncouple.remove(product);
                    } else if (ColorId==R.color.green){
                        viewClicked.setBackgroundResource(R.color.white);
                        viewClicked.setTag(R.color.white);
                        toCouple.remove(product);
                    } else if (ColorId==R.color.white){
                        viewClicked.setBackgroundResource(R.color.green);
                        viewClicked.setTag(R.color.green);
                        toCouple.add(product);
                    }
            }
        });
    }

    @Override
//    needed for icon in toolbar
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.link, menu);
        return true;
    }
    @Override
//    set onClick to icons in toolbar
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_item_link) {
            couple();
            uncouple();
            Intent i = new Intent(CoupleToAccountActivity.this, ViewAccountActivity.class);
            CoupleToAccountActivity.this.finish();
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void couple() {
        //        check for internet connection
        boolean connection = new ConnectionCheck().test(getApplicationContext());
        if (!connection){return;}
//        couple all selected products
        for (String product : toCouple) {
            CouplerBackgroundWorker couple = new CouplerBackgroundWorker(this);
            couple.setProgressBar(progressBar);
            try {
                List<String> echo = couple.execute("couple", username, product).get();
            }
            catch (InterruptedException e) { e.printStackTrace(); }
            catch (ExecutionException e) { e.printStackTrace(); }
        }
    }
    private void uncouple() {
        //        check for internet connection
        boolean connection = new ConnectionCheck().test(getApplicationContext());
        if (!connection){return;}
//        uncouple all selected products
        for (String product : toUncouple) {
            CouplerBackgroundWorker uncouple = new CouplerBackgroundWorker(this);
            uncouple.setProgressBar(progressBar);
            try {
                List<String> echo = uncouple.execute("uncouple", username, product).get();
            }
            catch (InterruptedException e) { e.printStackTrace(); }
            catch (ExecutionException e) { e.printStackTrace(); }
        }
    }
    @Override
    public void onBackPressed() {
        //        check for internet connection
        boolean connection = new ConnectionCheck().test(getApplicationContext());
        if (!connection){return;}
        Intent i = new Intent(CoupleToAccountActivity.this, ViewAccountActivity.class);
        CoupleToAccountActivity.this.finish();
        startActivity(i);
    }
}