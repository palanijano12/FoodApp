package com.example.mydummyapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<ItemModel> imageModelArrayList = new ArrayList<>();
    private ItemAdapter adapter;
    private String[] myImageNameList = new String[]{"Monday", "Monday", "Monday", "Monday", "Monday", "Monday", "Monday"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler);

        imageModelArrayList = mListItem();

        adapter = new ItemAdapter(this, imageModelArrayList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));


    }

    private ArrayList<ItemModel> mListItem() {

        ArrayList<ItemModel> list = new ArrayList<>();

        for (int i = 0; i < myImageNameList.length; i++) {
            ItemModel fruitModel = new ItemModel();
            fruitModel.setName(myImageNameList[i]);
            list.add(fruitModel);
        }

        return list;
    }
}

