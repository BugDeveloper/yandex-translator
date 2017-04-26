package com.bugdeveloper.androidyandextranslator;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.bugdeveloper.androidyandextranslator.fragments.Translate;

import java.util.Set;

public class SelectLanguage extends AppCompatActivity {

    private static String TAG = "SELECTOR";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_language);
        ListView languageList = (ListView) findViewById(R.id.HistoryList);

        if (Translate.GetDictionary() == null || Translate.GetDictionary().size() == 0) return;

        Set<String> keySet = Translate.GetDictionary().values();
        String[] languages = keySet.toArray(new String[keySet.size()]);

        ListAdapter listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, languages);
        languageList.setAdapter(listAdapter);

        languageList.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent();
            intent.putExtra("lang", String.valueOf(parent.getItemAtPosition(position)));
            setResult(RESULT_OK, intent);
            finish();
        });
    }
}
