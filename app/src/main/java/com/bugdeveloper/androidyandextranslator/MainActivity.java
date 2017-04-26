package com.bugdeveloper.androidyandextranslator;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.EditText;

import com.bugdeveloper.androidyandextranslator.fragments.History;
import com.bugdeveloper.androidyandextranslator.fragments.Bookmarks;
import com.bugdeveloper.androidyandextranslator.fragments.Translate;

public class MainActivity extends AppCompatActivity implements History.HistoryEventListener {

    private Fragment translate, bookmarks, settings;

    private BottomNavigationView navigation;

    private int curId = R.id.navigation_translate;

    private void SwitchFragmentTo(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content, fragment);
        ft.commit();
    }

    private void InitializeContent() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content, translate);
        ft.commit();
    }

    private void InitializeFragments() {
        translate = new Translate();
        bookmarks = new History();
        settings = new Bookmarks();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitializeFragments();
        InitializeContent();
        SwitchFragmentTo(translate);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);

        navigation.setOnNavigationItemSelectedListener(item -> {

            if (curId == item.getItemId()) return true;

            switch (item.getItemId()) {
                case R.id.navigation_translate:
                    curId = R.id.navigation_translate;
                    SwitchFragmentTo(translate);
                    return true;
                case R.id.navigation_bookmarks:
                    curId = R.id.navigation_bookmarks;
                    SwitchFragmentTo(bookmarks);
                    return true;
                case R.id.navigation_settings:
                    curId = R.id.navigation_settings;
                    SwitchFragmentTo(settings);
                    return true;
            }

            return false;
        });
    }

    @Override
    public void SwitchToTranslateWithWord(String word) {
        navigation.getMenu().getItem(0).setChecked(true);
        ((EditText)findViewById(R.id.InputField)).setText(word);
    }
}
