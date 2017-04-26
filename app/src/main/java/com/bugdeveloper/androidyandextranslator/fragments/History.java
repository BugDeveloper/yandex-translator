package com.bugdeveloper.androidyandextranslator.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bugdeveloper.androidyandextranslator.R;
import com.bugdeveloper.androidyandextranslator.adapters.TranslationAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class History extends Fragment {

    HistoryEventListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        listener = (HistoryEventListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.history_fragment, container, false);

        if (Translate.GetTranslations() == null)
            return view;

        ArrayList<Translation> translations = new ArrayList<>();

        ListView listView = (ListView) view.findViewById(R.id.HistoryList);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            TextView word = (TextView) ((View) parent.getItemAtPosition(position)).findViewById(R.id.wordField);
            listener.SwitchToTranslateWithWord(word.getText().toString());
        });

        HashMap<String, HashMap<String, Translation>> dictionary = Translate.GetTranslations();

        /*
         * It's really heavy, but I have no time ;(
         */
        for (String languageKey : dictionary.keySet()) {
            translations.addAll(dictionary.get(languageKey).keySet().stream()
                    .map(wordKey -> dictionary.get(languageKey).get(wordKey))
                    .collect(Collectors.toList()));
        }

        ListAdapter listAdapter = new TranslationAdapter(getActivity(), translations);

        listView.setAdapter(listAdapter);
        
        return view;
    }

    public interface HistoryEventListener {
        void SwitchToTranslateWithWord(String word);
    }
}
