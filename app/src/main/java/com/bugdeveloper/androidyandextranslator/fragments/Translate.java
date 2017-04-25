package com.bugdeveloper.androidyandextranslator.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bugdeveloper.androidyandextranslator.DataStorage;
import com.bugdeveloper.androidyandextranslator.JsonAdapter;
import com.bugdeveloper.androidyandextranslator.R;
import com.bugdeveloper.androidyandextranslator.SelectLanguage;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static com.bugdeveloper.androidyandextranslator.DataStorage.LANGUAGES_API;
import static com.bugdeveloper.androidyandextranslator.DataStorage.TRANSLATE_KEY;

public class Translate extends Fragment {

    private final static String TAG = "TRANSLATE";

    private static String uiLanguageCode;

    private View fragmentView;

    private static BiMap<String, String> languageMap;

    private EditText inputText;
    private TextView translation;
    private ListView dictionaryList;
    private AsyncTask translationThread;
    private TextView languageToChange;

    private TextView sourceLang, destLang;

    private boolean autoDetection = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.translate_fragment, container, false);
        FileStorage.CreateDir(getActivity());
        new DictionaryDownloader().execute(uiLanguageCode);

        return fragmentView;
    }

    public static BiMap<String, String> GetDictionary() {
        return languageMap;
    }

    public Translate() {
        uiLanguageCode = Locale.getDefault().getLanguage();
    }

    private String getSourceLang() {
        return sourceLang.getText().toString();
    }

    private String getDestLang() {
        return destLang.getText().toString();
    }

    private String getLangCode(String lang) {
        return languageMap.inverse().get(lang);
    }

    private void initializeLanguageSwitcher() {
        sourceLang = (TextView) fragmentView.findViewById(R.id.SourceLang);
        destLang = (TextView) fragmentView.findViewById(R.id.DestLang);

        sourceLang.setText(languageMap.get("en"));
        destLang.setText(languageMap.get("ru"));

        destLang.setOnClickListener(v -> {
            languageToChange = destLang;
            startLanguageSelection();
        });

        sourceLang.setOnClickListener(v -> {
            languageToChange = sourceLang;
            startLanguageSelection();
        });

        ImageView switcher = (ImageView) fragmentView.findViewById(R.id.LangSwap);

        switcher.setOnClickListener(v -> {
            animateView(v);
            CharSequence temp = sourceLang.getText();
            sourceLang.setText(destLang.getText());
            destLang.setText(temp);
        });
    }

    private void startLanguageSelection() {
        Intent intent = new Intent(getActivity(), SelectLanguage.class);
        startActivityForResult(intent, 1);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        String lang = data.getStringExtra("lang");

        if (!lang.equals(""))
            languageToChange.setText(lang);
    }

    private void initializeView() {
        initializeLanguageSwitcher();
        initializeInputField();
        initializeButtons();
        initializeTranslationSection();
        initializeThreads();
    }

    private void initializeButtons() {
        initializeClear();
    }

    private void animateView(View v) {
        v.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.image_animation));
    }

    private void initializeThreads() {
        translationThread = new TranslationProcessor();
    }

    private void initializeClear() {
        ImageView imageView = (ImageView) fragmentView.findViewById(R.id.ClearButton);
        imageView.setOnClickListener(v -> {
            animateView(v);
            clear();
        });
    }

    private void clear() {
        inputText.setText("");
        translation.setText("");
        dictionaryList.setAdapter(null);
    }

    private void initializeTranslationSection() {
        dictionaryList = (ListView) fragmentView.findViewById(R.id.DictionaryList);
        translation = (TextView) fragmentView.findViewById(R.id.DicTranslation);
    }

    private void initializeInputField() {
        inputText = (EditText) fragmentView.findViewById(R.id.InputField);
        inputText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN || keyCode != KeyEvent.KEYCODE_ENTER)
                return false;

            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

            return true;
        });
        inputText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    translation.setText("");
                    dictionaryList.setAdapter(null);
                    return;
                }

                if (translationThread.getStatus() == AsyncTask.Status.RUNNING)
                    translationThread.cancel(false);

                String lang;

                if (autoDetection) {
                    lang = getLangCode(getDestLang());
                } else {
                    lang = getLangCode(getSourceLang()) + '-' + getLangCode(getDestLang());
                }

                Log.i(TAG, "lang before async: " + lang);
                translationThread = new TranslationProcessor().execute(lang, s.toString());
            }
        });
    }

    private void setTranslation(JSONObject json) {

        Log.i(TAG, json.toString());
        try {
            translation.setText(json.getJSONArray("text").get(0).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setDictionaryList(JSONObject json) {

        Log.i(TAG, json.toString());

        try {
            ListAdapter jsonAdapter = new JsonAdapter(getActivity(), json.getJSONArray("def"));
            dictionaryList.setAdapter(jsonAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class DictionaryDownloader extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {

            String pathToCache = getActivity().getFilesDir().getPath();

            String json;

            languageMap = HashBiMap.create();

            if (FileStorage.FileExists(pathToCache, FileStorage.LANGUAGES)) {
                json = (String) FileStorage.Load(pathToCache, FileStorage.LANGUAGES);
            } else {
                List<BasicNameValuePair> args = new ArrayList<>(2);
                args.add(new BasicNameValuePair("key", TRANSLATE_KEY));
                args.add(new BasicNameValuePair("ui", params[0]));

                json = QuerySender.PostQuery(LANGUAGES_API, args);

                FileStorage.Save(pathToCache, FileStorage.LANGUAGES, json);

                Log.i(TAG, json);
            }

            JSONObject jsonObj = null;

            try {
                jsonObj = new JSONObject(json).getJSONObject("langs");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObj;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {

            if (jsonObject == null) return;

            Iterator<?> keys = jsonObject.keys();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                try {
                    languageMap.put(key, jsonObject.getString(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            initializeView();
        }
    }

    private class TranslationProcessor extends AsyncTask<String, Void, JSONObject[]> {

        private String lang;

        @Override
        protected void onPostExecute(JSONObject[] jsonArray) {

            if (jsonArray == null || jsonArray[0] == null || jsonArray[1] == null) return;

            setTranslation(jsonArray[0]);
            setDictionaryList(jsonArray[1]);

            if (!languageMap.inverse().get(getSourceLang()).equals(lang.substring(0, 2))) {
                sourceLang.setText(languageMap.get(lang.substring(0, 2)));
            }
        }

        @Override
        protected JSONObject[] doInBackground(String... params) {

            Log.i(TAG, "lang: " + params[0]);
            Log.i(TAG, "text: " + params[1]);

            JSONObject dictionaryJson = null;
            JSONObject translateJson = null;

            try {
                List<BasicNameValuePair> args = new ArrayList<>(4);
                args.add(new BasicNameValuePair("key", TRANSLATE_KEY));
                args.add(new BasicNameValuePair("lang", params[0]));
                args.add(new BasicNameValuePair("text", params[1]));
                args.add(new BasicNameValuePair("ui", uiLanguageCode));

                translateJson = new JSONObject(QuerySender.PostQuery(DataStorage.TRANSLATE_API, args));

                lang = translateJson.getString("lang");

                args = new ArrayList<>(3);
                args.add(new BasicNameValuePair("key", DataStorage.DICTIONARY_KEY));
                args.add(new BasicNameValuePair("lang", lang));
                args.add(new BasicNameValuePair("text", params[1]));

                dictionaryJson = new JSONObject(QuerySender.PostQuery(DataStorage.DICTIONARY_API, args));

            } catch (JSONException e) {
                e.printStackTrace();
                cancel(false);
            }


            return new JSONObject[]{translateJson, dictionaryJson};
        }

    }
}

