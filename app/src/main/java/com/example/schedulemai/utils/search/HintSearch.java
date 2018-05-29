package com.example.schedulemai.utils.search;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;


import java.util.ArrayList;

/**
 * Created by IO.Novikov on 03.04.2018.
 */

public class HintSearch {
    String[] items;
    ArrayList<String> listItems;
    ArrayAdapter<String> adapter;
    ListView listView;
    EditText editText;
    Context context;
    public HintSearch(EditText editText, ListView listView, Context context) {
        this.listView = listView;
        this.editText = editText;
        this.context = context;
    }


    public void setUp() {
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    listView.setVisibility(View.VISIBLE);
                } else {
                    listView.setVisibility(View.GONE);
                }
            }
        });


        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                listView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchItem(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editText.setText(listItems.get(position));
                listView.setVisibility(View.GONE);
            }
        });
    }

//    public void initList(){
//        //items=new String[]{"Java","JavaScript","C#","PHP", "С++", "Python", "C", "SQL", "Ruby", "Objective-C"};
//        items = new String[10000];
//        for (int i = 0; i < 1000; i++)  {
//            items[i* 10 + 0] = "Java" + i;
//            items[i* 10 + 1] = "JavaScript " + i;
//            items[i* 10 + 2] = "C#";
//            items[i* 10 + 3] = "PHP";
//            items[i* 10 + 4] = "С++";
//            items[i* 10 + 5] = "Python";
//            items[i* 10 + 6] = "C";
//            items[i* 10 + 7] = "SQL";
//            items[i* 10 + 8] = "Ruby";
//            items[i* 10 + 9] = "Objective-C";
//        }
//        listItems = new ArrayList<>();
//        adapter=new ArrayAdapter<String>(context, R.layout.list_item, R.id.txtitem, listItems);
//        listView.setAdapter(adapter);
//    }

    public void searchItem(String textToSearch){
        listItems.clear();
        for(String item : items){
            if(!"".equals(item) && item.toLowerCase().contains(textToSearch)){
                listItems.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
