package com.gfo.gfo_meesterproef.admin.viewAccount;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gfo.gfo_meesterproef.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountsFragment extends Fragment {

    ListView accountList;

    public AccountsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.list, container, false);

//        get all (either user of admin) Accounts from ViewAccountActivity
        ArrayList<Account> accounts = getArguments().getParcelableArrayList("accounts");

        //        couple accounts to layout
        accountList = rootView.findViewById(R.id.list);
        AccountAdapter accountAdapter = new AccountAdapter(getActivity(), accounts);
        accountList.setAdapter(accountAdapter);

        registerAccountClickCallBack();
        return rootView;
    }

    private void registerAccountClickCallBack() {
        accountList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View viewClicked, int position, long id) {
//                get selected object as account
                Object accountClicked = accountList.getItemAtPosition(position);
                Account selectedAccount = (Account) accountClicked;
//                call method in parent activity
                ((ViewAccountActivity)getActivity()).onSelect(selectedAccount);
            }
        });
    }
}