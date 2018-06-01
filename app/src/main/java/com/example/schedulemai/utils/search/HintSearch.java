package com.example.schedulemai.utils.search;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;


import com.example.schedulemai.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IO.Novikov on 03.04.2018.
 */

public class HintSearch {
    ArrayList<String> items;
    ArrayList<String> listItems;
    ArrayAdapter<String> adapter;
    ListView listView;
    EditText editText;
    Context context;
    int clickedPosition = 0;

    public HintSearch(EditText editText, ListView listView, Context context, List<String> data) {
        this.listView = listView;
        this.editText = editText;
        this.context = context;
        listItems = new ArrayList<>(data);
        items = new ArrayList<>(data);
        adapter=new ArrayAdapter<>(context, R.layout.list_item, R.id.txtitem, listItems);
        listView.setAdapter(adapter);
    }


    public void setUp() {
        editText.setFocusable(true);
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
                clickedPosition = position;
            }
        });

        if (editText.getText().length() > 0) {
            listView.setVisibility(View.GONE);
        }

    }

    public void searchItem(String textToSearch){
        listItems.clear();
        for(String item : items){
            if(!"".equals(item) && item.toLowerCase().contains(textToSearch)){
                listItems.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public int getClickedPosition() {
        //return clickedPosition;
        int result = 0;
        String text = editText.getText().toString();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).equals(text)) {
                result = i;
                break;
            }
        }
        Log.e("RESULT", String.valueOf(result));
        return result;
    }

    public String getClickedValue() {
        return items.get(getClickedPosition());
    }
}
