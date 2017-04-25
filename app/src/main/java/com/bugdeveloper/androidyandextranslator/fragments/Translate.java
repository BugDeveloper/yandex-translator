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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    private static HashMap<String, HashMap<String, String[]>> translationMap;

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

        initializeLanguageMap();
        initializeTranslationMap();

        return fragmentView;
    }

    /**
     * The method is responsible for getting all available
     * languages from Yandex Api or from cached data.
     */
    private void initializeLanguageMap() {
        try {
            languageMap = (BiMap<String, String>) FileStorage.Load(FileStorage.LANGUAGES);
            initializeView();
        } catch (IOException e) {
            languageMap = HashBiMap.create();
            new LanguagesDownloader().execute(uiLanguageCode);
        }
    }

    /**
     * Method initializeTranslationMap serves for getting HashMap of translations from cache.
     */
    private void initializeTranslationMap() {
        try {
            translationMap = (HashMap<String, HashMap<String, String[]>>) FileStorage.Load(FileStorage.TRANSLATIONS);
        } catch (IOException e) {
            e.printStackTrace();
            translationMap = new HashMap<>();
        }
    }

    public static BiMap<String, String> GetDictionary() {
        return languageMap;
    }


    /**
     * Class constructor only initializes uiLanguageCode variable with
     * value of default system language.
     */
    public Translate() {
        uiLanguageCode = Locale.getDefault().getLanguage();
    }

    /**
     * A wrapper for easy getting current source language.
     * @return Current language from which translation is going to be done.
     */
    private String getSourceLang() {
        return sourceLang.getText().toString();
    }

    /**
     * A wrapper for easy getting current destination language.
     * @return Current language to which translation is going to be done.
     */
    private String getDestLang() {
        return destLang.getText().toString();
    }

    /**
     * Method for getting language code for certain full language name.
     * @param lang Full name of language.
     * @return Language code.
     */
    private String getLangCode(String lang) {
        return languageMap.inverse().get(lang);
    }

    /**
     * The method initializes upper panel for language switching: look for button references,
     * set default translate direction and set click handlers.
     */
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

    /**
     * Starts new activity with list of possible languages to select and wait for new value.
     */
    private void startLanguageSelection() {
        Intent intent = new Intent(getActivity(), SelectLanguage.class);
        startActivityForResult(intent, 1);
    }

    /**
     * Sets chosen language.
     * @param requestCode
     * @param resultCode
     * @param data Returned Intent data with set language parameter.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        String lang = data.getStringExtra("lang");

        if (!lang.equals(""))
            languageToChange.setText(lang);
    }

    /**
     * Calls all needed initialization methods.
     */
    private void initializeView() {
        initializeLanguageSwitcher();
        initializeInputField();
        initializeButtons();
        initializeTranslationSection();
        initializeThreads();
    }

    /**
     * Call to all button initialization methods.
     */
    private void initializeButtons() {
        initializeClear();
    }

    /**
     * Makes view animated.
     * @param v View to animate.
     */
    private void animateView(View v) {
        v.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.image_animation));
    }

    /**
     * Initializes thread to avoid possible NullReferenceExceptions.
     */
    private void initializeThreads() {
        translationThread = new TranslationProcessor();
    }

    /**
     * Initializes clear button: look for it and set click handler.
     */
    private void initializeClear() {
        ImageView imageView = (ImageView) fragmentView.findViewById(R.id.ClearButton);
        imageView.setOnClickListener(v -> {
            animateView(v);
            clear();
        });
    }


    /**
     * Method to clear all translation views.
     */
    private void clear() {
        inputText.setText("");
        translation.setText("");
        dictionaryList.setAdapter(null);
    }

    /**
     * Finds references for setting translation.
     */
    private void initializeTranslationSection() {
        dictionaryList = (ListView) fragmentView.findViewById(R.id.DictionaryList);
        translation = (TextView) fragmentView.findViewById(R.id.DicTranslation);
    }

    /**
     * Finds references to input field, set key listener to hide keyboard when enter key was pressed,
     * add text changed listener to translate.
     */
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

                String lang;

                if (autoDetection) {
                    lang = getLangCode(getDestLang());
                } else {
                    lang = getLangCode(getSourceLang()) + '-' + getLangCode(getDestLang());
                }

                translate(lang, s.toString());
            }
        });
    }


    /**
     * Method which determines translation presence in cache. If it is just gets it from cache and call setters.
     * Otherwise call TranslationProcessor to get it from the Internet.
     * @param lang Language to translate into.
     * @param text Text which is needed to be translated.
     */
    private void translate(String lang, String text) {
        try {
            String translation = translationMap.get(lang).get(text)[0];
            String dictionary = translationMap.get(lang).get(text)[1];
            setTranslation(translation);
            setDictionaryList(dictionary);
        } catch (NullPointerException e) {
            if (translationThread.getStatus() == AsyncTask.Status.RUNNING)
                translationThread.cancel(false);
            translationThread = new TranslationProcessor().execute(lang, text);
        }
    }

    /**
     * Setter for translation field.
     * @param text Text to set.
     */
    private void setTranslation(String text) {
        translation.setText(text);
    }

    /**
     * Saves cached translation on hard drive.
     */
    @Override
    public void onPause() {
        FileStorage.Save(FileStorage.TRANSLATIONS, translationMap);
        Log.i(TAG, "onPause");
        super.onPause();
    }

    /**
     * Setter for dictionary field.
     * @param dictionary Dictionary field in JSON format.
     */
    private void setDictionaryList(String dictionary) {

        JSONObject json;
        try {
            json = new JSONObject(dictionary);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        try {
            ListAdapter jsonAdapter = new JsonAdapter(getActivity(), json.getJSONArray("def"));
            dictionaryList.setAdapter(jsonAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Async task to download available languages from the internet.
     */
    private class LanguagesDownloader extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {

            String json = null;

            List<BasicNameValuePair> args = new ArrayList<>(2);
            args.add(new BasicNameValuePair("key", TRANSLATE_KEY));
            args.add(new BasicNameValuePair("ui", params[0]));

            try {
                json = QuerySender.PostQuery(LANGUAGES_API, args);
            } catch (IOException e) {
                cancel(false);
                e.printStackTrace();
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

            FileStorage.Save(FileStorage.LANGUAGES, languageMap);
            initializeView();
        }
    }

    /**
     * Async task to download translation from the internet.
     */
    private class TranslationProcessor extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPostExecute(String[] result) {

            for (String string : result) {
                if (string == null || string.length() == 0)
                    return;
            }

            String lang = result[0].substring(result[0].length() - 2, result[0].length()), text = result[1], translation = result[2], dictionary = result[3];

            translationMap.putIfAbsent(lang, new HashMap<>());

            translationMap.get(lang).putIfAbsent(text, new String[]{translation, dictionary});

            setTranslation(translation);
            setDictionaryList(dictionary);

            String sourceLangText = getSourceLang();

            String curLang = languageMap.inverse().get(sourceLangText);

            if (!curLang.equals(lang)) {
                sourceLang.setText(languageMap.get(lang));
            }
        }

        @Override
        protected String[] doInBackground(String... params) {

            String lang = params[0], text = params[1];

            String dictionary = null;
            String translation = null;

            try {
                List<BasicNameValuePair> args = new ArrayList<>(4);
                args.add(new BasicNameValuePair("key", TRANSLATE_KEY));
                args.add(new BasicNameValuePair("lang", lang));
                args.add(new BasicNameValuePair("text", text));
                args.add(new BasicNameValuePair("ui", uiLanguageCode));

                JSONObject response = null;
                try {
                    response = new JSONObject(QuerySender.PostQuery(DataStorage.TRANSLATE_API, args));
                } catch (IOException e) {
                    cancel(false);
                }

                translation = response.getJSONArray("text").getString(0);

                lang = response.getString("lang");

                args = new ArrayList<>(3);
                args.add(new BasicNameValuePair("key", DataStorage.DICTIONARY_KEY));
                args.add(new BasicNameValuePair("lang", lang));
                args.add(new BasicNameValuePair("text", text));

                dictionary = QuerySender.PostQuery(DataStorage.DICTIONARY_API, args);
            } catch (JSONException e) {
                e.printStackTrace();
                cancel(false);
            } catch (IOException e) {
                e.printStackTrace();
                cancel(false);
            }

            return new String[]{lang, text, translation, dictionary};
        }

    }
}

